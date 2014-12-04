package org.aslak.github.merge.service;

import java.io.IOException;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.aslak.github.merge.model.Commit;
import org.aslak.github.merge.model.CurrentUser;
import org.aslak.github.merge.model.PullRequest;
import org.aslak.github.merge.model.event.PushedPullRequest;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GitHub;

public class GitHubNotificationService {

    @Inject
    private CurrentUser user;

    @Inject
    private NotificationService notification;

    public void sendReadyMessage(PullRequest pull) {

    }

    public void sendPushedMessage(@Observes PushedPullRequest pull) {
        try {
            GHPullRequest request = GitHub.connectUsingOAuth(user.getAccessToken())
                .getUser(pull.getPullRequest().getTarget().getUser())
                .getRepository(pull.getPullRequest().getTarget().getRepository())
                .getPullRequest(pull.getPullRequest().getNumber());

            request.comment(formatPushedMessage(pull));
            notification.sendMessage(pull.getPullRequest().getKey(), "Added push message to GitHub pull request");

            request.close();
            notification.sendMessage(pull.getPullRequest().getKey(), "Closing GitHub pull request");

        } catch(IOException e) {
            notification.sendMessage(pull.getPullRequest().getKey(), "Failed to update GitHub pull request: " + e.getMessage());
            throw new RuntimeException("Could not update Pull Request " + pull);
        }
    }

    private String formatPushedMessage(PushedPullRequest pull) {
        StringBuilder sb = new StringBuilder();

        sb.append("Pushed upstream\n\n");
        for(Commit commit : pull.getCommits()) {
            sb.append("* ").append(commit.getId()).append('\n');
        }
        return sb.toString();
    }
}
