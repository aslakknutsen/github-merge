package org.aslak.github.merge.service;

import org.aslak.github.merge.TestUtils;
import org.aslak.github.merge.model.LocalStorage;
import org.aslak.github.merge.model.PullRequest;
import org.junit.Assert;
import org.junit.Test;

public class RepositoryServiceTest {

    
    @Test
    public void shouldBeAbleToPullCloneRepository() throws Exception {
        PullRequest request = TestUtils.getPullRequest();

        RepositoryService service = new RepositoryService();
        LocalStorage storage = service.get(request);
        
        Assert.assertTrue(storage.getLocation().exists());
    }

}
