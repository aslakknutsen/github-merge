package org.aslak.github.merge.rest;

import javax.json.JsonObject;

import org.aslak.github.merge.model.PullRequest;
import org.aslak.github.merge.model.RepositoryInfo;
import org.kohsuke.github.GHPullRequest;

public class GithubUtil {

    public static PullRequest toPullRequest(JsonObject object) {
        
        RepositoryInfo source = new RepositoryInfo(
                object.getJsonObject("head").getJsonObject("user").getString("login"),
                object.getJsonObject("head").getJsonObject("repo").getString("name"),
                object.getJsonObject("head").getString("ref")
        );
        RepositoryInfo target = new RepositoryInfo(
                object.getJsonObject("base").getJsonObject("user").getString("login"),
                object.getJsonObject("base").getJsonObject("repo").getString("name"),
                object.getJsonObject("base").getString("ref")
        );
        return new PullRequest(
                object.getInt("number"),
                source,
                target);
    }

    public static PullRequest toPullRequest(GHPullRequest object) {

        RepositoryInfo source = new RepositoryInfo(
                object.getHead().getUser().getLogin(),
                object.getHead().getRepository().getName(),
                object.getHead().getRef()
        );
        RepositoryInfo target = new RepositoryInfo(
                object.getBase().getUser().getLogin(),
                object.getBase().getRepository().getName(),
                object.getBase().getRef()
        );
        return new PullRequest(
                object.getNumber(),
                source,
                target);
    }
}
