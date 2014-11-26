package org.aslak.github.merge.rest;

import javax.inject.Inject;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.aslak.github.merge.model.PullRequestKey;

@ServerEndpoint(value = "/api/{user}/{repo}/{pull}/notification")
public class NotificationEndpoint {

    @Inject
    private NotificationCentral central;

    @OnOpen
    public void connect(Session session, EndpointConfig config, @PathParam("user") String user, @PathParam("repo") String repo, @PathParam("pull") int pull) {
        central.register(session, new PullRequestKey(user, repo, pull));
    }
    
    @OnClose
    public void disconnect(Session session, @PathParam("user") String user, @PathParam("repo") String repo, @PathParam("pull") int pull) {
        central.unregister(session, new PullRequestKey(user, repo, pull));
    }
}
