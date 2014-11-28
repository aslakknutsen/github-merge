package org.aslak.github.merge.service;

import java.util.HashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.aslak.github.merge.model.CurrentUser;
import org.aslak.github.merge.model.PullRequest;
import org.aslak.github.merge.model.PullRequestKey;
import org.aslak.github.merge.rest.GithubUtil;
import org.kohsuke.github.GitHub;

@ApplicationScoped
public class PullRequestService {

    private HashMap<PullRequestKey, PullRequest> store;

    @Inject
    private NotificationService notification;

    @Inject
    private CurrentUser currentUser;
    
    public PullRequestService() {
        this.store = new HashMap<>();
    }

    public PullRequest get(PullRequestKey key) {
        PullRequest request = locate(key);
        if(request == null) {
            notification.sendMessage(key, "Requesting pull request data from GitHub");
            request = fetch(key);
            store(request);
        }
        return request;
    }

    private PullRequest fetch(PullRequestKey key) {
        try {
            GitHub github;
            if(currentUser== null) {
                github = GitHub.connectAnonymously();
            } else {
                github = GitHub.connectUsingOAuth(currentUser.getAccessToken());
            }
            return GithubUtil.toPullRequest(
                        github.getOrganization(key.getUser())
                            .getRepository(key.getRepository())
                            .getPullRequest(key.getNumber()));

        } catch (Exception e) {
            notification.sendMessage(key, "Could not fetch pull request data from GitHub: " + e.getMessage());
            throw new RuntimeException("Could not fetch PullRequest from GitHub for " + key, e);
        }
    }

    private PullRequest locate(PullRequestKey key) {
        return store.get(key);
    }
    
    public void store(PullRequest pullrequest) {
        this.store.put(pullrequest.getKey(), pullrequest);
    }

    public void delete(PullRequest pullRequest) {
        this.store.remove(pullRequest.getKey());
    }
}
