package org.aslak.github.merge.rest;

import java.util.List;

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
import org.aslak.github.merge.model.LocalStorage;
import org.aslak.github.merge.model.PullRequest;
import org.aslak.github.merge.model.PullRequestKey;
import org.aslak.github.merge.service.PullRequestService;
import org.aslak.github.merge.service.RebaseService;
import org.aslak.github.merge.service.RepositoryService;

@Path("rebase")
public class RebaseResource {

    @Inject
    private RebaseService rebaseService;

    @Inject
    private PullRequestService pullRequestService;

    @Inject
    private RepositoryService repositoryService;

    @GET
    @Path("{user}/{repo}/{pull}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response status(@PathParam("user") String user, @PathParam("repo") String repository, @PathParam("pull") int number) {
        PullRequestKey key = new PullRequestKey(user, repository, number);
        PullRequest pullRequest = pullRequestService.get(key);
        if(pullRequest == null) {
            return Response.status(Status.NOT_FOUND).build();
        }

        LocalStorage storage = repositoryService.get(pullRequest);
        if(storage == null) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }

        List<Commit> commits = rebaseService.status(storage, pullRequest);
        if(commits == null) {
            return Response.noContent().build();
        }
        return Response.ok(commits).build();
    }

    @POST
    @Path("{user}/{repo}/{pull}")
    public Response rebase(@PathParam("user") String user, @PathParam("repo") String repository, @PathParam("pull") int number, List<Commit> commits) {
        PullRequestKey key = new PullRequestKey(user, repository, number);
        PullRequest pullRequest = pullRequestService.get(key);
        if(pullRequest == null) {
            return Response.status(Status.NOT_FOUND).build();
        }

        LocalStorage storage = repositoryService.get(pullRequest);
        if(storage == null) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }

        rebaseService.rebase(storage, pullRequest, commits);
        return Response.ok().build();
    }

    @POST
    @Path("{user}/{repo}/{pull}/push")
    public Response push(@PathParam("user") String user, @PathParam("repo") String repository, @PathParam("pull") int number) {
        PullRequestKey key = new PullRequestKey(user, repository, number);
        PullRequest pullRequest = pullRequestService.get(key);
        if(pullRequest == null) {
            return Response.status(Status.NOT_FOUND).build();
        }

        LocalStorage storage = repositoryService.get(pullRequest);
        if(storage == null) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }

        rebaseService.push(storage, pullRequest);
        return Response.ok().build();
    }
}
