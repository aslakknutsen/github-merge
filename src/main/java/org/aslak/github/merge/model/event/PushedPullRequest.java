package org.aslak.github.merge.model.event;

import java.util.List;

import org.aslak.github.merge.model.PullRequest;

public class PushedPullRequest {

    private PullRequest pullRequest;
    private List<String> commits;
    
    public PushedPullRequest(PullRequest pullRequest, List<String> commits) {
        this.pullRequest = pullRequest;
        this.commits = commits;
    }
    
    public List<String> getCommits() {
        return commits;
    }
    
    public PullRequest getPullRequest() {
        return pullRequest;
    }
}
