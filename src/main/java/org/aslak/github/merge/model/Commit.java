package org.aslak.github.merge.model;

public class Commit {

    public enum State {
        PICK,
        REWORD,
        FIXUP,
        DELETE
    }

    private String sha;
    private String message;
    private String author;
    private State state = State.PICK;
    
    public Commit(String sha, String message, String author) {
        this.sha = sha;
        this.message = message;
        this.author = author;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public String getMessage() {
        return message;
    }
    
    public String getSha() {
        return sha;
    }
    
    public State getState() {
        return state;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public void setState(State state) {
        this.state = state;
    }
}
