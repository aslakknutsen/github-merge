package org.aslak.github.merge.service;

import org.aslak.github.merge.model.PullRequest;
import org.aslak.github.merge.model.PullRequestKey;
import org.junit.Assert;
import org.junit.Test;

public class PullRequestServiceTest {

    @Test
    public void test() {
        PullRequestService service = new PullRequestService();
        PullRequest request = service.get(new PullRequestKey("arquillian", "arquillian-cube", 36));
        
        Assert.assertEquals("arquillian", request.getTarget().getUser());
        Assert.assertEquals("arquillian-cube", request.getTarget().getRepository());
    }

}
