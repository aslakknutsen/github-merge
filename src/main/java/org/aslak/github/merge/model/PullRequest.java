package org.aslak.github.merge.model;

public class PullRequest {

    private int number;
    private RepositoryInfo source;
    private RepositoryInfo target;

    public PullRequest(int number, RepositoryInfo source, RepositoryInfo target) {
        this.number = number;
        this.source = source;
        this.target = target;
    }
    
    public int getNumber() {
        return number;
    }
    
    public RepositoryInfo getSource() {
        return source;
    }
    
    public RepositoryInfo getTarget() {
        return target;
    }
}
