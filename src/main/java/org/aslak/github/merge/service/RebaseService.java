package org.aslak.github.merge.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.aslak.github.merge.model.Commit;
import org.aslak.github.merge.model.Commit.State;
import org.aslak.github.merge.model.LocalStorage;
import org.aslak.github.merge.model.PullRequest;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RebaseCommand;
import org.eclipse.jgit.api.RebaseCommand.Operation;
import org.eclipse.jgit.api.RebaseResult;
import org.eclipse.jgit.lib.AbbreviatedObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.RebaseTodoLine;
import org.eclipse.jgit.lib.RebaseTodoLine.Action;
import org.eclipse.jgit.revwalk.RevCommit;

public class RebaseService {

    @Inject
    private NotificationService notification;
    
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
            rebase.setProgressMonitor(new ProgressMonitor() {
                
                @Override
                public void update(int completed) {
                    //System.out.println("PM update " + completed);
                }
                
                @Override
                public void start(int totalTasks) {
                    //System.out.println("PM start " + totalTasks);
                }
                
                @Override
                public boolean isCancelled() {
                    //System.out.println("PM isCancelled");
                    return false;
                }
                
                @Override
                public void endTask() {
                    //System.out.println("PM endTask");
                }
                
                @Override
                public void beginTask(String title, int totalWork) {
                    notification.sendMessage(pullRequest.getKey(), "Begin Task: " + title);
                    //System.out.println("PM beginTask " + title + " " + totalWork);
                }
            });
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
                git.rebase().setOperation(Operation.ABORT).call();
                notification.sendMessage(pullRequest.getKey(), "Rebase aborted");
            }
            
        } catch(Exception e) {
            notification.sendMessage(pullRequest.getKey(), "Failed to rebase due to exception: " + e.getMessage());
            try {
                git.rebase().setOperation(Operation.ABORT).call();
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
    

    public void push() {
        
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
