package org.aslak.github.merge.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aslak.github.merge.model.Commit;
import org.aslak.github.merge.model.Commit.State;
import org.aslak.github.merge.model.PullRequestKey;
import org.eclipse.jgit.api.RebaseCommand;
import org.eclipse.jgit.lib.AbbreviatedObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.RebaseTodoLine;
import org.eclipse.jgit.lib.RebaseTodoLine.Action;
import org.eclipse.jgit.revwalk.RevCommit;

final class GitUtil {

    private GitUtil() { }

    public static Map<AbbreviatedObjectId, Commit> map(List<Commit> commits) {
        Map<AbbreviatedObjectId, Commit> mapped = new HashMap<>();
        for(Commit commit : commits) {
            mapped.put(AbbreviatedObjectId.fromString(commit.getId().substring(0, 7)), commit);
        }
        return mapped;
    }
    
    public static void updateTodoLog(NotificationService notification, PullRequestKey request, Map<AbbreviatedObjectId, Commit> mappedCommits, List<RebaseTodoLine> steps) {
        for(int i = 0; i < steps.size(); i++) {
            RebaseTodoLine step = steps.get(i);
            if(!mappedCommits.containsKey(step.getCommit())) {
                notification.sendMessage(request, "Perform " + step.getCommit().toString() + " Action[remove]");
                steps.remove(i);
                i--;
                continue;
            }
            Commit commit = mappedCommits.get(step.getCommit());
            if(commit.getState() == State.DELETE) {
                notification.sendMessage(request, "Perform " + step.getCommit().toString() + " " + " Action[remove]");
                steps.remove(i);
                i--;
                continue;
            }
            try {
                step.setAction(Action.parse(commit.getState().name().toLowerCase()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            notification.sendMessage(request, "Perform " + step.getCommit().toString() + " " + step.getAction());
        }
    }
    
    static List<Commit> toCommitList(ObjectId[] mergedCommits) {
        List<Commit> ids = new ArrayList<>();
        for(ObjectId objectId : mergedCommits) {
            ids.add(new Commit(objectId.getName()));
        }
        return ids;
    }
 

    static List<Commit> toCommitList(Iterable<RevCommit> log) {
        List<Commit> commits = new ArrayList<>();
        for(RevCommit foundCommit : log) {
            Commit commit = new Commit(foundCommit.getName(), foundCommit.getFullMessage(), foundCommit.getAuthorIdent().getName());
            if(commit.getMessage().contains("squash!") || commit.getMessage().contains("fixup!")) {
                commit.setState(State.FIXUP);
            }
            commits.add(commit);
        }
        Collections.reverse(commits);
        return commits;
    }

    static class InteractiveRebase implements RebaseCommand.InteractiveHandler {

        private NotificationService notification;
        private PullRequestKey key;
        private List<Commit> commits;
        private Map<AbbreviatedObjectId, Commit> mappedCommits;
        
        public InteractiveRebase(NotificationService notification, PullRequestKey key, List<Commit> commits) {
            this.notification = notification;
            this.key = key;
            this.commits = commits;
            this.mappedCommits = map(commits);
        }

        @Override
        public void prepareSteps(List<RebaseTodoLine> steps) {
            updateTodoLog(notification, key, mappedCommits, steps);
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
    }
}
