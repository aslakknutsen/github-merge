package org.aslak.github.merge.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.websocket.Session;

import org.aslak.github.merge.model.Notification;
import org.aslak.github.merge.model.PullRequestKey;

@ApplicationScoped
public class NotificationCentral {

    private Map<PullRequestKey, List<Session>> sessions = new HashMap<>();
    
    public void register(Session session, PullRequestKey request) {
        synchronized (sessions) {
            List<Session> registered = new ArrayList<>();
            if(sessions.containsKey(request)) {
                registered = sessions.get(request);
            }
            registered.add(session);
            sessions.put(request, registered);
        }
    }
    
    public void unregister(Session session, PullRequestKey request) {
        synchronized (sessions) {
            if(sessions.containsKey(request)) {
                List<Session> registered = sessions.get(request);
                registered.remove(session);
                if(registered.size() == 0) {
                    sessions.remove(request);
                }
            }
        }
    }
    
    public void notifyListeners(@Observes Notification notification) {
        List<Session> registered = sessions.get(notification.getKey());
        if(registered == null) {
            return;
        }
        for(Session session : registered) {
            if(session.isOpen()) {
                session.getAsyncRemote().sendText(notificationToJson(notification));
            }
        }
    }

    private String notificationToJson(Notification n) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n")
            .append("\"type\": \"").append(n.getType()).append("\",\n")
            .append("\"key\": \"").append(n.getKey()).append("\",\n")
            .append("\"message\": \"").append(n.getMessage()).append("\"\n")
        .append("}\n");
        return sb.toString();
    }
}
