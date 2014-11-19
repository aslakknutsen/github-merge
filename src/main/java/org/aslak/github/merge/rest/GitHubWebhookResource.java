package org.aslak.github.merge.rest;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.aslak.github.merge.model.PullRequest;
import org.aslak.github.merge.service.GitHubNotificationService;
import org.aslak.github.merge.service.PullRequestService;

@Path("github")
public class GitHubWebhookResource {

    @Inject
    private PullRequestService pullRequestService;

    @Inject
    private GitHubNotificationService notificationService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response webhook(JsonObject object) {
        if(object.containsKey("pull_request")) {
            PullRequest pull = GithubUtil.toPullRequest(object.getJsonObject("pull_request"));
            pullRequestService.store(pull);
            notificationService.sendReadyMessage(pull);
            return Response.ok().build();
            
        } else {
            return Response.ok("Nothing to do, not a pull_request").build();
        }
    }
}
