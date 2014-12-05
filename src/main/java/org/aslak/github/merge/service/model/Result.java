package org.aslak.github.merge.service.model;

public class Result<T> {

    private Exception e;
    private T result;
    
    public Result(Exception e) {
        this.e = e;
    }
    
    public Result(T result) {
        this.result = result;
    }
    
    public boolean successful() {
        return e == null;
    }
    
    public boolean failed() {
        return !successful();
    }

    public Exception getException() {
        return e;
    }
    
    public T getResult() {
        return result;
    }
}
