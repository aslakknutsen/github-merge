package org.aslak.github.merge.model;


public class Notification {

    public enum Type {
        MESSAGE,
        PROGRESS_START,
        PROGRESS,
        PROGRESS_END
    }

    private Type type;
    private PullRequestKey key;
    private String message;
    
    public Notification(Type type, PullRequestKey key, String message) {
        this.type = type;
        this.key = key;
        this.message = message;
    }
    
    public Type getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }
    
    public PullRequestKey getKey() {
        return key;
    }
}
