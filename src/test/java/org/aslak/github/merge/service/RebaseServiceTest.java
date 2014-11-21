package org.aslak.github.merge.service;

import java.util.List;

import org.aslak.github.merge.TestUtils;
import org.aslak.github.merge.model.Commit;
import org.aslak.github.merge.model.Commit.State;
import org.aslak.github.merge.model.LocalStorage;
import org.aslak.github.merge.model.PullRequest;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class RebaseServiceTest {

    @Test @Ignore
    public void shouldBeAbleToList() throws Exception {
        PullRequest request = TestUtils.getPullRequest();
        RepositoryService repo = new RepositoryService();
        RebaseService rebase = new RebaseService();

        LocalStorage storage = repo.get(request);
        
        List<Commit> commits = rebase.status(storage, request);
        Assert.assertNotNull(commits);
        commits.get(0).setState(State.REWORD);
        commits.get(0).setMessage("B");
        commits.get(1).setState(State.FIXUP);
        rebase.rebase(storage, request, commits);
        
        List<Commit> newCommits = rebase.status(storage, request);
        Assert.assertNotNull(newCommits);
    }
}
