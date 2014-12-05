package org.aslak.github.merge.service;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import org.aslak.github.merge.model.Commit;
import org.aslak.github.merge.model.CurrentUser;
import org.aslak.github.merge.model.LocalStorage;
import org.aslak.github.merge.model.PullRequest;
import org.aslak.github.merge.service.NotificationService.Notifier;
import org.aslak.github.merge.service.NotificationService.NotifierProgress;
import org.aslak.github.merge.service.model.Result;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand.FastForwardMode;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.RebaseCommand;
import org.eclipse.jgit.api.RebaseCommand.Operation;
import org.eclipse.jgit.api.RebaseResult;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

public class GitService {

    private NotificationService notificationService;

    @Inject
    public GitService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
    
    public Operations perform(LocalStorage storage, PullRequest request) {
        return new Operations(storage, request, notificationService);
    }
    

    public static class Operations {
        
        private LocalStorage storage;
        private PullRequest request;
        private Notifier notifier;
        private NotifierProgress progress;
        private NotificationService notification;
        
        public Operations(LocalStorage storage, PullRequest request, NotificationService notification) {
            this.storage = storage;
            this.request = request;
            this.notification = notification;
            this.notifier = notification.getNotifier(request.getKey());
            this.progress = notification.getNotifierProgress(request.getKey());
        }

        public Result<List<Commit>> doStatus() {
            Git git = null;
            try {
                git = open();
                return new Result<>(status(git));
            } catch(Exception e) {
                notifier.message("Failed to get status due to exception: " + e.getMessage());
                return new Result<>(new RuntimeException("Could not get status for " + request, e));                
            } finally {
                close(git);
            }
        }
        
        public Result<Boolean> doRebase(List<Commit> commits) {
            Git git = null;
            progress.start("Rebase", 3);
            try {
                git = open();
                progress.major();
                checkoutPullRequestBranch(git);
                progress.major();
                rebaseOnTargetBranch(git, commits);
                progress.major();
                progress.end(true);
                return new Result<>(true);
            }
            catch(Exception e) {
                progress.end(false);
                notifier.message("Failed to rebase due to exception: " + e.getMessage());
                try {
                    abortRebase(git);
                    notifier.message("Rebase aborted");
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                return new Result<>(new RuntimeException("Failed to rebase " + request, e));
            }
            finally {
                close(git);
            }   
        }        

        public Result<List<Commit>> doPush(CurrentUser user) {
            Git git = null;
            try {
                git = open();
                checkoutTargetBranch(git);
                List<Commit> commits = mergePullRequestBranchWithTarget(git);
                pushTargetBranch(git, user);
                return new Result<>(commits);
            }
            catch(Exception e) {
                notifier.message("Failed to merge due to exception: " + e.getMessage());
                try {
                    resetTargetHard(git);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                return new Result<>(new RuntimeException("Failed to merge " + request, e));
            }
            finally {
                close(git);
            }
        }

        
        private Git open() throws IOException {
            return Git.open(storage.getLocation());
        }
        
        private void close(Git git) {
            if(git != null) {
                git.close();
            }
        }

        private void checkoutTargetBranch(Git git) throws Exception {
            notifier.message("Checking out pull request branch: " + request.getTarget().getBranch());
            git.checkout().setName(String.valueOf(request.getTarget().getBranch())).call();
        }

        private void checkoutPullRequestBranch(Git git) throws Exception {
            notifier.message("Checking out pull request branch: " + request.getTarget().getBranch());
            git.checkout().setName(String.valueOf(request.getNumber())).call();
        }
        
        private void abortRebase(Git git) throws Exception {
            git.rebase().setOperation(Operation.ABORT)
                .setProgressMonitor(new NotificationProgressMonitor(request, notification))
                .call();
        }
        
        private void rebaseOnTargetBranch(Git git, final List<Commit> commits) throws Exception {
            RebaseCommand rebase = git.rebase();
            rebase.setUpstream(request.getTarget().getBranch());
            rebase.setProgressMonitor(new NotificationProgressMonitor(request, notification, progress));
            rebase.runInteractively(new GitUtil.InteractiveRebase(notification, request.getKey(), commits));
            RebaseResult result = rebase.call();
            if(!result.getStatus().isSuccessful()) {
                throw new RuntimeException("Rebase not successful, status " + result.getStatus());
            }
        }
        
        private List<Commit> status(Git git) throws Exception {
            ObjectId target = git.getRepository().resolve(request.getTarget().getBranch());
            ObjectId source = git.getRepository().resolve(String.valueOf(request.getNumber()));
            return GitUtil.toCommitList(git.log().addRange(target, source).call());
        }
        
        private List<Commit> mergePullRequestBranchWithTarget(Git git) throws Exception {
            notifier.message("Merging source " + request.getNumber() + " into target " + request.getTarget().getBranch());
            MergeResult result = git.merge()
                .setFastForward(FastForwardMode.FF_ONLY)
                .include(git.getRepository().getRef(String.valueOf(request.getNumber())))
                .call();

            if(!result.getMergeStatus().isSuccessful()) {
                throw new RuntimeException("Merge not successfull, status " + result.getMergeStatus());
            } else {
                notifier.message("Merged " + request + " : " + result.getMergeStatus());
            }
            return GitUtil.toCommitList(result.getMergedCommits());
        }
        
        private void pushTargetBranch(Git git, CurrentUser user) throws Exception {
            notifier.message("Pushing to  " + request.getTarget().toHttpsURL());
            Iterable<PushResult> pushResults = git.push()
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(user.getAccessToken(), new char[0]))
                    .setRemote("origin")
                    .setRefSpecs(new RefSpec(request.getTarget().getBranch() + ":" + request.getTarget().getBranch()))
                    .setProgressMonitor(new NotificationProgressMonitor(request, notification))
                    .call();

            for(PushResult pushResult : pushResults) {
                if(pushResult.getMessages() != null && !pushResult.getMessages().isEmpty()) {
                    notifier.message(pushResult.getMessages());
                }
            }
            notifier.message("Push success");
        }
        
        private void resetTargetHard(Git git) throws Exception {
            git.reset().setMode(ResetType.HARD)
                .setRef("origin/" + request.getTarget().getBranch()).call();
            notifier.message("Reset origin/" + request.getTarget().getBranch());
        }
       
    } 
}
