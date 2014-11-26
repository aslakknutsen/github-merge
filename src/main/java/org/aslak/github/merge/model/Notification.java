package org.aslak.github.merge.model;

public class Notification {

    private PullRequestKey key;
    private String message;
    
    public Notification(PullRequestKey key, String message) {
        this.key = key;
        this.message = message;
    }
    
    public String getMessage() {
        return message;
    }
    
    public PullRequestKey getKey() {
        return key;
    }
}
