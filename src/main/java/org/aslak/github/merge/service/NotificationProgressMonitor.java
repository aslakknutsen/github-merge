package org.aslak.github.merge.service;

import org.aslak.github.merge.model.PullRequest;
import org.aslak.github.merge.model.PullRequestKey;
import org.aslak.github.merge.service.NotificationService.NotifierProgress;
import org.eclipse.jgit.lib.ProgressMonitor;

public class NotificationProgressMonitor implements ProgressMonitor {
    
    private PullRequestKey key;
    private NotificationService service;
    private NotifierProgress progress;

    public NotificationProgressMonitor(PullRequest key, NotificationService service) {
        this(key, service, null);
    }

    public NotificationProgressMonitor(PullRequest key, NotificationService service, NotifierProgress progress) {
        this.key = key.getKey();
        this.service = service;
        this.progress = progress;
    }

    @Override
    public void beginTask(String title, int totalWork) {
        service.sendMessage(key, "Begin Task: " + title);
    }

    @Override
    public void start(int totalTasks) {
    }

    @Override
    public void update(int completed) {
        if(progress != null) {
            progress.minor();
        }
    }

    @Override
    public void endTask() {
        if(progress != null) {
            progress.minor();
        }
    }

    @Override
    public boolean isCancelled() {
        return false;
    }
}
