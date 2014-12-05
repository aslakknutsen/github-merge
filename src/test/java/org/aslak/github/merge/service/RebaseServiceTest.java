package org.aslak.github.merge.service;

import java.util.List;

import org.aslak.github.merge.TestUtils;
import org.aslak.github.merge.model.Commit;
import org.aslak.github.merge.model.Commit.State;
import org.aslak.github.merge.model.LocalStorage;
import org.aslak.github.merge.model.PullRequest;
import org.aslak.github.merge.service.model.Result;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RebaseServiceTest {

    @Mock
    private NotificationService notificationService;

    @Test @Ignore
    public void shouldBeAbleToList() throws Exception {
        PullRequest request = TestUtils.getPullRequest();
        GitService git = new GitService(notificationService);

        LocalStorage storage = git.perform(request).doClone().getResult();
        
        Result<List<Commit>> result = git.perform(storage, request).doStatus();
        Assert.assertNotNull(result);
        Assert.assertTrue(result.successful());

        List<Commit> commits = result.getResult();
        Assert.assertNotNull(commits);
        commits.get(0).setState(State.REWORD);
        commits.get(0).setMessage("B");
        commits.get(1).setState(State.FIXUP);
        git.perform(storage, request).doRebase(commits);
        
        Result<List<Commit>> newResult = git.perform(storage, request).doStatus();
        Assert.assertNotNull(newResult);
    }
}
