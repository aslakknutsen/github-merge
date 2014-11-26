package org.aslak.github.merge.model;

public class PullRequestKey {

    private String user;
    private String repository;
    private int number;

    public PullRequestKey(String user, String repository, int number) {
        super();
        this.user = user;
        this.repository = repository;
        this.number = number;
    }

    public String getUser() {
        return user;
    }

    public String getRepository() {
        return repository;
    }

    public int getNumber() {
        return number;
    }

    @Override
    public String toString() {
        return user + "/" + repository + "/" + number;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + number;
        result = prime * result + ((repository == null) ? 0 : repository.hashCode());
        result = prime * result + ((user == null) ? 0 : user.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PullRequestKey other = (PullRequestKey) obj;
        if (number != other.number)
            return false;
        if (repository == null) {
            if (other.repository != null)
                return false;
        } else if (!repository.equals(other.repository))
            return false;
        if (user == null) {
            if (other.user != null)
                return false;
        } else if (!user.equals(other.user))
            return false;
        return true;
    }
}
