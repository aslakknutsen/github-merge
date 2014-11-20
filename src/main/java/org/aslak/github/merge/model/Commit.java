package org.aslak.github.merge.model;

public class Commit {

    public enum State {
        PICK,
        REWORD,
        FIXUP,
        DELETE
    }

    private String id;
    private String message;
    private String author;
    private State state = State.PICK;
    
    public Commit(String sha, String message, String author) {
        this.id = sha;
        this.message = message;
        this.author = author;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public String getMessage() {
        return message;
    }
    
    public String getId() {
        return id;
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
