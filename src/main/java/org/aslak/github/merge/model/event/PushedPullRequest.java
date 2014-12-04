package org.aslak.github.merge.model.event;

import java.util.List;

import org.aslak.github.merge.model.Commit;
import org.aslak.github.merge.model.PullRequest;

public class PushedPullRequest {

    private PullRequest pullRequest;
    private List<Commit> commits;
    
    public PushedPullRequest(PullRequest pullRequest, List<Commit> commits) {
        this.pullRequest = pullRequest;
        this.commits = commits;
    }
    
    public List<Commit> getCommits() {
        return commits;
    }
    
    public PullRequest getPullRequest() {
        return pullRequest;
    }
}
