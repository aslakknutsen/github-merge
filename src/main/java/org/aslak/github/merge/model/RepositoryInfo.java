package org.aslak.github.merge.model;


public class RepositoryInfo {

    private String user;
    private String repository;
    private String branch;

    public RepositoryInfo(String user, String repository, String branch) {
        this.user = user;
        this.repository = repository;
        this.branch = branch;
    }
    
    public String getUser() {
        return user;
    }
    
    public String getRepository() {
        return repository;
    }
    
    public String getBranch() {
        return branch;
    }

    public String toHttpsURL() {
        return "https://github.com/" + user + "/" + repository + ".git";
    }
}
