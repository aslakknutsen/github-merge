package org.aslak.github.merge.model;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;

@Singleton @Startup
public class CurrentApp {

    private String clientId;
    private String clientSecret;
    
    public CurrentApp() {
    }
    
    public String getClientId() {
        return clientId;
    }
    
    public String getClientSecret() {
        return clientSecret;
    }
    
    @PostConstruct
    private void init() {
        String clientId = System.getenv("GITHUB_CLIENT_ID");
        String clientSecret = System.getenv("GITHUB_CLIENT_SECRET");

        if(clientId == null || clientId.isEmpty()) {
            throw new IllegalStateException("GITHUB_CLIENT_ID env variable is missing");
        }
        if(clientSecret == null || clientSecret.isEmpty()) {
            throw new IllegalStateException("GITHUB_CLIENT_SECRET env variable is missing");
        }
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }
}
