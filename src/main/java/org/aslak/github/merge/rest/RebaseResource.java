package org.aslak.github.merge.rest;

import java.util.List;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.aslak.github.merge.model.Commit;
import org.aslak.github.merge.model.CurrentUser;
import org.aslak.github.merge.model.LocalStorage;
import org.aslak.github.merge.model.PullRequest;
import org.aslak.github.merge.model.PullRequestKey;
import org.aslak.github.merge.model.event.PushedPullRequest;
import org.aslak.github.merge.service.GitService;
import org.aslak.github.merge.service.PullRequestService;
import org.aslak.github.merge.service.RepositoryService;
import org.aslak.github.merge.service.model.Result;

@Path("rebase")
public class RebaseResource {

    @Inject
    private GitService gitService;

    @Inject
    private PullRequestService pullRequestService;

    @Inject
    private RepositoryService repositoryService;

    @Inject
    private CurrentUser currentUser;

    @Inject
    private Event<Object> events;

    @GET
    @Path("{user}/{repo}/{pull}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response status(@PathParam("user") String user, @PathParam("repo") String repository, @PathParam("pull") int number) {
        PullRequestKey key = new PullRequestKey(user, repository, number);
        PullRequest request = pullRequestService.get(key);
        if(request == null) {
            return Response.status(Status.NOT_FOUND).build();
        }

        LocalStorage storage = repositoryService.get(request);
        if(storage == null) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }

        return buildResultResponse(gitService.perform(storage, request).doStatus());
    }

    @POST
    @Path("{user}/{repo}/{pull}")
    public Response rebase(@PathParam("user") String user, @PathParam("repo") String repository, @PathParam("pull") int number, List<Commit> commits) {
        PullRequestKey key = new PullRequestKey(user, repository, number);
        PullRequest request = pullRequestService.get(key);
        if(request == null) {
            return Response.status(Status.NOT_FOUND).build();
        }

        LocalStorage storage = repositoryService.get(request);
        if(storage == null) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }

        return buildResultResponse(gitService.perform(storage, request).doRebase(commits));
    }

    @POST
    @Path("{user}/{repo}/{pull}/push")
    public Response push(@PathParam("user") String user, @PathParam("repo") String repository, @PathParam("pull") int number) {
        PullRequestKey key = new PullRequestKey(user, repository, number);
        PullRequest request = pullRequestService.get(key);
        if(request == null) {
            return Response.status(Status.NOT_FOUND).build();
        }

        LocalStorage storage = repositoryService.get(request);
        if(storage == null) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }

        Result<List<Commit>> commits = gitService.perform(storage, request).doPush(currentUser);
        if(commits.wasSuccess()) {
            events.fire(new PushedPullRequest(request, commits.getResult()));
        }
        return buildResultResponse(commits);
    }

    private Response buildResultResponse(Result<?> result) {
        if(result.wasSuccess()) {
            Object value = result.getResult();
            if(value instanceof Boolean) {
                return Response.ok().build();
            }
            return Response.ok(value).build();
        } else {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(result.getException()).build();
        }
    }
}
