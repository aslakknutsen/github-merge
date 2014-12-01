package org.aslak.github.merge.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.aslak.github.merge.model.Commit;
import org.aslak.github.merge.model.Commit.State;
import org.aslak.github.merge.model.CurrentUser;
import org.aslak.github.merge.model.LocalStorage;
import org.aslak.github.merge.model.PullRequest;
import org.aslak.github.merge.model.event.PushedPullRequest;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand.FastForwardMode;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.RebaseCommand;
import org.eclipse.jgit.api.RebaseCommand.Operation;
import org.eclipse.jgit.api.RebaseResult;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.lib.AbbreviatedObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.RebaseTodoLine;
import org.eclipse.jgit.lib.RebaseTodoLine.Action;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

public class RebaseService {

    @Inject
    private NotificationService notification;
    
    @Inject
    private CurrentUser user;

    @Inject
    private Event<Object> event;

    public List<Commit> status(LocalStorage storage, PullRequest pullRequest) {
        List<Commit> commits = new ArrayList<>();
        Git git = null;
        try {
            git = open(storage);

            ObjectId target = git.getRepository().resolve(pullRequest.getTarget().getBranch());
            ObjectId source = git.getRepository().resolve(String.valueOf(pullRequest.getNumber()));
            for(RevCommit foundCommit : git.log().addRange(target, source).call()) {
                Commit commit = new Commit(foundCommit.getName(), foundCommit.getFullMessage(), foundCommit.getAuthorIdent().getName());
                if(commit.getMessage().contains("squash!") || commit.getMessage().contains("fixup!")) {
                    commit.setState(State.FIXUP);
                }
                commits.add(commit);
            }
            Collections.reverse(commits);
            return commits;
        } catch(Exception e) {
            throw new RuntimeException("Could not open Git repository", e);
        }
        finally {
            if(git != null) {
                git.close();
            }
        }
    } 
    
    public void rebase(LocalStorage storage, final PullRequest pullRequest, final List<Commit> commits) {
        Git git = null;
        try {
            git = open(storage);
            git.checkout().setName(String.valueOf(pullRequest.getNumber())).call();

            final Map<AbbreviatedObjectId, Commit> mappedCommits = map(commits);

            RebaseCommand rebase = git.rebase();
            rebase.setUpstream(pullRequest.getTarget().getBranch());
            rebase.setProgressMonitor(new NotificationProgressMonitor(pullRequest.getKey(), notification));
            rebase.runInteractively(new RebaseCommand.InteractiveHandler() {
                @Override
                public void prepareSteps(List<RebaseTodoLine> steps) {
                    for(int i = 0; i < steps.size(); i++) {
                        RebaseTodoLine step = steps.get(i);
                        if(!mappedCommits.containsKey(step.getCommit())) {
                            notification.sendMessage(pullRequest.getKey(), "Perform " + step.getCommit().toString() + " Action[remove]");
                            steps.remove(i);
                            i--;
                            continue;
                        }
                        Commit commit = mappedCommits.get(step.getCommit());
                        if(commit.getState() == State.DELETE) {
                            notification.sendMessage(pullRequest.getKey(), "Perform " + step.getCommit().toString() + " " + " Action[remove]");
                            steps.remove(i);
                            i--;
                            continue;
                        }
                        try {
                            step.setAction(Action.parse(commit.getState().name().toLowerCase()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        notification.sendMessage(pullRequest.getKey(), "Perform " + step.getCommit().toString() + " " + step.getAction());
                    }
                }

                private int messagesChanged = 0;

                @Override
                public String modifyCommitMessage(String message) {
                    int count = 0;
                    for(Commit commit : commits) {
                        if(commit.getState() == State.REWORD) {
                            if(messagesChanged == count) {
                                messagesChanged++;
                                return commit.getMessage();
                            } else {
                                count++;
                            }
                        }
                    }
                    return null;
                }
            });
            RebaseResult result = rebase.call();
            if(!result.getStatus().isSuccessful()) {
                notification.sendMessage(pullRequest.getKey(), "Failed to rebase, status " + result.getStatus());
                git.rebase().setOperation(Operation.ABORT)
                    .setProgressMonitor(new NotificationProgressMonitor(pullRequest.getKey(), notification))
                    .call();
                notification.sendMessage(pullRequest.getKey(), "Rebase aborted");
            }

        } catch(Exception e) {
            notification.sendMessage(pullRequest.getKey(), "Failed to rebase due to exception: " + e.getMessage());
            try {
                git.rebase().setOperation(Operation.ABORT)
                    .setProgressMonitor(new NotificationProgressMonitor(pullRequest.getKey(), notification))
                    .call();
                notification.sendMessage(pullRequest.getKey(), "Rebase aborted");
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            throw new RuntimeException("Failed to rebase", e);
        }
        finally {
            if(git != null) {
                git.close();
            }
        }
    }
    
    public void push(LocalStorage storage, PullRequest pullRequest) {
        Git git = null;
        try {
            git = open(storage);

            notification.sendMessage(pullRequest.getKey(), "Checking out target branch: " + pullRequest.getTarget().getBranch());
            git.checkout().setName(String.valueOf(pullRequest.getTarget().getBranch())).call();

            notification.sendMessage(pullRequest.getKey(), "Merging source " + pullRequest.getNumber() + " into target " + pullRequest.getTarget().getBranch());
            MergeResult result = git.merge()
                .setFastForward(FastForwardMode.FF_ONLY)
                .include(git.getRepository().getRef(String.valueOf(pullRequest.getNumber())))
                .call();

            if(!result.getMergeStatus().isSuccessful()) {
                notification.sendMessage(pullRequest.getKey(), "Failed to merge, status " + result.getMergeStatus());
                git.reset().setMode(ResetType.HARD)
                    .setRef("origin/" + pullRequest.getTarget().getBranch()).call();
                notification.sendMessage(pullRequest.getKey(), "Reset origin/" + pullRequest.getTarget().getBranch());
            } else {
                notification.sendMessage(pullRequest.getKey(), "Merged " + pullRequest + " : " + result.getMergeStatus());
            }

            notification.sendMessage(pullRequest.getKey(), "Pushing to  " + pullRequest.getTarget().toHttpsURL());
            Iterable<PushResult> pushResults = git.push()
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(user.getAccessToken(), new char[0]))
                .setRemote("origin")
                .setRefSpecs(new RefSpec(pullRequest.getTarget().getBranch() + ":" + pullRequest.getTarget().getBranch()))
                .setProgressMonitor(new NotificationProgressMonitor(pullRequest.getKey(), notification))
                .call();

            for(PushResult pushResult : pushResults) {
                if(pushResult.getMessages() != null && !pushResult.getMessages().isEmpty()) {
                    notification.sendMessage(pullRequest.getKey(), pushResult.getMessages());
                }
            }
            notification.sendMessage(pullRequest.getKey(), "Push success");

            event.fire(new PushedPullRequest(pullRequest, toCommitList(result.getMergedCommits())));
        } catch(Exception e) {
            notification.sendMessage(pullRequest.getKey(), "Failed to merge due to exception: " + e.getMessage());
            try {
                git.reset().setMode(ResetType.HARD)
                    .setRef("origin/" + pullRequest.getTarget().getBranch()).call();
                notification.sendMessage(pullRequest.getKey(), "Reset origin/" + pullRequest.getTarget().getBranch());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            throw new RuntimeException("Failed to merge", e);
        }
        finally {
            if(git != null) {
                git.close();
            }
        }
    }

    private List<String> toCommitList(ObjectId[] mergedCommits) {
        List<String> ids = new ArrayList<>();
        for(ObjectId objectId : mergedCommits) {
            ids.add(objectId.getName());
        }
        return ids;
    }

    public void delete() {
        
    }

    private Map<AbbreviatedObjectId, Commit> map(List<Commit> commits) {
        Map<AbbreviatedObjectId, Commit> mapped = new HashMap<>();
        for(Commit commit : commits) {
            mapped.put(AbbreviatedObjectId.fromString(commit.getId().substring(0, 7)), commit);
        }
        return mapped;
    }

    private Git open(LocalStorage storage) throws Exception {
        return Git.open(storage.getLocation());
    }
}
