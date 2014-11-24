package org.aslak.github.merge.model;

import java.io.Serializable;
import java.util.UUID;

import javax.enterprise.context.SessionScoped;

@SessionScoped
public class CurrentUser implements Serializable {

    private static final long serialVersionUID = 1L;

    private String accessToken = null;
    private String state;
    private String code;
    private String[] scopes;
    private String originalRequest;

    public CurrentUser() {
        this.state = UUID.randomUUID().toString();
    }

    public boolean isAuthorized() {
        return accessToken != null;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getState() {
        return state;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setOriginalRequest(String originalRequest) {
        this.originalRequest = originalRequest;
    }

    public String getOriginalRequest() {
        return originalRequest;
    }

    public void setScopes(String[] scopes) {
        this.scopes = scopes;
    }

    public boolean isAuthorized(String auth) {
        for(String scope : scopes) {
            if(scope.startsWith(auth)) {
                return true;
            }
        }
        return false;
    }
}
