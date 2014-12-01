package org.aslak.github.merge.service;

import org.aslak.github.merge.model.PullRequestKey;
import org.eclipse.jgit.lib.ProgressMonitor;

public class NotificationProgressMonitor implements ProgressMonitor {
    
    private PullRequestKey key;
    private NotificationService service;
    
    public NotificationProgressMonitor(PullRequestKey key, NotificationService service) {
        this.key = key;
        this.service = service;
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
    }

    @Override
    public void endTask() {
    }

    @Override
    public boolean isCancelled() {
        return false;
    }
}
