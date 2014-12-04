package org.aslak.github.merge.service;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.aslak.github.merge.model.Notification;
import org.aslak.github.merge.model.PullRequest;
import org.aslak.github.merge.model.PullRequestKey;

public class NotificationService {

    @Inject
    private Event<Notification> notification;

    public void sendMessage(PullRequest request, String message) {
        sendMessage(request.getKey(), message);
    }

    public void sendMessage(PullRequestKey key, String message) {
        notification.fire(new Notification(key, message));
    }
}
