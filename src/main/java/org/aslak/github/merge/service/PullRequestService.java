package org.aslak.github.merge.service;

import java.util.HashSet;
import java.util.Set;

import org.aslak.github.merge.model.PullRequest;

public class PullRequestService {

    private Set<PullRequest> store;
    
    public PullRequestService() {
        this.store = new HashSet<>();
    }
    
    public PullRequest locate(String user, String repo, int pull) {
        for(PullRequest request : store) {
            if(
                    request.getSource().getUser().equals(user) &&
                    request.getSource().getRepository().equals(user) &&
                    request.getNumber() == pull) {
                return request;
            }
        }
        return null;
    }
    
    public void store(PullRequest pullrequest) {
        this.store.add(pullrequest);
    }
}
