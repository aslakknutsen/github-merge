package org.aslak.github.merge.model;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;

@Singleton @Startup
public class CurrentApp {

    private static final String GITHUB_CLIENT_SECRET = "GITHUB_CLIENT_SECRET";
    private static final String GITHUB_CLIENT_ID = "GITHUB_CLIENT_ID";
    private static final String MERGER_CALLBACK_URL = "MERGER_CALLBACK_URL";

    private String clientId;
    private String clientSecret;
    private String baseCallbackUrl;

    public CurrentApp() {
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getBaseCallbackUrl() {
        return baseCallbackUrl;
    }

    @PostConstruct
    private void init() {
        String clientId = System.getenv(GITHUB_CLIENT_ID);
        String clientSecret = System.getenv(GITHUB_CLIENT_SECRET);
        String baseCallbackUrl = System.getenv(MERGER_CALLBACK_URL);

        if(clientId == null || clientId.isEmpty()) {
            throw new IllegalStateException(GITHUB_CLIENT_ID + " env variable is missing");
        }
        if(clientSecret == null || clientSecret.isEmpty()) {
            throw new IllegalStateException(GITHUB_CLIENT_SECRET + " env variable is missing");
        }
        if(baseCallbackUrl == null || baseCallbackUrl.isEmpty()) {
            throw new IllegalStateException(MERGER_CALLBACK_URL + " env variable is missing");
        }
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.baseCallbackUrl = baseCallbackUrl;
    }
}
