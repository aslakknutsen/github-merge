package org.aslak.github.merge.service;

import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.aslak.github.merge.model.CurrentUser;
import org.aslak.github.merge.model.PullRequest;
import org.aslak.github.merge.rest.GithubUtil;
import org.kohsuke.github.GitHub;

@ApplicationScoped
public class PullRequestService {

    private Set<PullRequest> store;

    @Inject
    private CurrentUser currentUser;
    
    public PullRequestService() {
        this.store = new HashSet<>();
    }

    public PullRequest get(String user, String repo, int pull) {
        PullRequest request = locate(user, repo, pull);
        if(request == null) {
            request = fetch(user, repo, pull);
            store(request);
        }
        return request;
    }

    private PullRequest fetch(String user, String repo, int pull) {
        try {
            GitHub github;
            if(currentUser== null) {
                github = GitHub.connectAnonymously();
            } else {
                github = GitHub.connectUsingOAuth(currentUser.getAccessToken());
            }

            return GithubUtil.toPullRequest(
                        github.getOrganization(user)
                            .getRepository(repo)
                            .getPullRequest(pull));

        } catch (Exception e) {
            throw new RuntimeException("Could not fetch PullRequest from GitHub for " + user + "/" + repo + "/" + pull, e);
        }
    }

    private PullRequest locate(String user, String repo, int pull) {
        for(PullRequest request : store) {
            if(
                    request.getTarget().getUser().equals(user) &&
                    request.getTarget().getRepository().equals(repo) &&
                    request.getNumber() == pull) {
                return request;
            }
        }
        return null;
    }
    
    public void store(PullRequest pullrequest) {
        this.store.add(pullrequest);
    }

    public void delete(PullRequest pullRequest) {
        this.store.remove(pullRequest);
    }
}
