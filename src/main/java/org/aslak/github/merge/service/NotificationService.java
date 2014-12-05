package org.aslak.github.merge.service;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.aslak.github.merge.model.Notification;
import org.aslak.github.merge.model.Notification.Type;
import org.aslak.github.merge.model.PullRequest;
import org.aslak.github.merge.model.PullRequestKey;

public class NotificationService {

    @Inject
    private Event<Notification> notification;

    public void sendMessage(PullRequest request, String message) {
        sendMessage(request.getKey(), message);
    }

    public void sendMessage(PullRequestKey key, String message) {
        notification.fire(new Notification(Type.MESSAGE, key, message));
    }

    private void startProgress(PullRequestKey key, String message) {
        notification.fire(new Notification(Type.PROGRESS_START, key, message));
    }

    private void endProgress(PullRequestKey key, boolean sucess) {
        notification.fire(new Notification(Type.PROGRESS_END, key, String.valueOf(sucess)));
    }

    private void sendProgress(PullRequestKey key, int progress) {
        notification.fire(new Notification(Type.PROGRESS, key, String.valueOf(progress)));
    }

    public Notifier getNotifier(PullRequestKey key) {
        return new Notifier(key, this);
    }

    public NotifierProgress getNotifierProgress(PullRequestKey key) {
        return new NotifierProgress(key, this);
    }

    public static class Notifier {

        private NotificationService service;
        private PullRequestKey key;

        private Notifier(PullRequestKey key, NotificationService service) {
            this.key = key;
            this.service = service;
        }

        public void message(String message) {
            service.sendMessage(key, message);
        }
    }

    public static class NotifierProgress {
        private NotificationService service;
        private PullRequestKey key;

        private int majorSteps = 0;

        private int current = 0;
        private int currentMajor = 0;

        private NotifierProgress(PullRequestKey key, NotificationService service) {
            this.key = key;
            this.service = service;
        }

        public void start(String name, int majorSteps) {
            this.majorSteps = majorSteps;
            service.startProgress(key, name);
        }

        public void major() {
            currentMajor++;
            current = (100 / majorSteps) * currentMajor;
            service.sendProgress(key, current);
        }

        public void minor() {
            current += 1;
            service.sendProgress(key, current);
        }

        public void end(boolean status) {
            service.endProgress(key, status);
        }
    }
}
