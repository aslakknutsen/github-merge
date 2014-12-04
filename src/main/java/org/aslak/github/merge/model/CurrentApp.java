package org.aslak.github.merge.model;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.aslak.github.merge.Config;

@Singleton @Startup
public class CurrentApp {

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
        this.clientId = Config.githubClientId();
        this.clientSecret = Config.githubClientSecret();
        this.baseCallbackUrl = Config.baseCallBackUrl();
    }
}
