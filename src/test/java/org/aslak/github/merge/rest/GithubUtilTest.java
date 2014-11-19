package org.aslak.github.merge.rest;

import org.aslak.github.merge.TestUtils;
import org.aslak.github.merge.model.PullRequest;
import org.junit.Assert;
import org.junit.Test;

public class GithubUtilTest {

    @Test
    public void shouldMapGitHubPullRequestToInternal() throws Exception {
        PullRequest request = TestUtils.getPullRequest();
        
        Assert.assertEquals(36, request.getNumber());

        Assert.assertEquals("smiklosovic", request.getSource().getUser());
        Assert.assertEquals("arquillian-cube", request.getSource().getRepository());
        Assert.assertEquals("method_names", request.getSource().getBranch());
        
        Assert.assertEquals("arquillian", request.getTarget().getUser());
        Assert.assertEquals("arquillian-cube", request.getTarget().getRepository());
        Assert.assertEquals("master", request.getTarget().getBranch());
    }

}
