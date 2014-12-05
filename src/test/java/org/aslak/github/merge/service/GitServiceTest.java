package org.aslak.github.merge.service;

import org.aslak.github.merge.TestUtils;
import org.aslak.github.merge.model.LocalStorage;
import org.aslak.github.merge.model.PullRequest;
import org.aslak.github.merge.service.model.Result;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GitServiceTest {

    @Mock
    private NotificationService notification;
    
    @Test
    public void shouldBeAbleToPullCloneRepository() throws Exception {
        PullRequest request = TestUtils.getPullRequest();

        GitService service = new GitService(notification);
        Result<LocalStorage> storage = service.perform(request).doClone();
        
        Assert.assertTrue(storage.successful());
        Assert.assertTrue(storage.getResult().getLocation().exists());
    }

}
