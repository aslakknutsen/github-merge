package org.aslak.github.merge.rest;


import static com.jayway.restassured.RestAssured.given;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.aslak.github.merge.model.Commit;
import org.aslak.github.merge.service.GitHubNotificationService;
import org.aslak.github.merge.service.PullRequestService;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.filter.log.RequestLoggingFilter;
import com.jayway.restassured.filter.log.ResponseLoggingFilter;
import com.jayway.restassured.http.ContentType;

@RunWith(Arquillian.class)
public class GitHubWebhookResourceTestCase {

    @Deployment(testable = false)
    public static WebArchive deploy() {
        return ShrinkWrap.create(WebArchive.class)
                .addClasses(
                        MergeApplication.class,
                        GitHubWebhookResource.class,
                        GitHubNotificationService.class,
                        PullRequestService.class,
                        GithubUtil.class)
                .addPackage(Commit.class.getPackage())
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @ArquillianResource
    private URL base;

    //@BeforeClass
    public static void debug() {
        RestAssured.filters(
                ResponseLoggingFilter.responseLogger(),
                new RequestLoggingFilter());
    }
    
    @Test
    public void shouldInvokeWebHook() throws Exception {
        URL resource = new URL(base, "api/github");
        
        given().
            contentType(ContentType.JSON).
            body(Files.readAllBytes(Paths.get("src", "test", "resources", "payloads", "pull_request.json"))).
        then().
            statusCode(200).
        when().
            post(resource);
    }

}
