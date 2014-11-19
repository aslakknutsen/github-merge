package org.aslak.github.merge.service;

import java.io.File;
import java.nio.file.Paths;

import org.aslak.github.merge.model.LocalStorage;
import org.aslak.github.merge.model.PullRequest;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;

public class RepositoryService {

    private static final String WORK_FOLDER = "/tmp/mergerer/";
    
    public LocalStorage get(PullRequest request) {
        File path = calculatePath(request);
        if(path.exists() && new File(path, ".git").exists()) {
            return new LocalStorage(path);
        }
        if(!path.mkdirs()) {
            throw new RuntimeException("Could not create path " + path);
        }
        
        Git git = null;
        
        CloneCommand command = Git.cloneRepository()
                    .setBranch(request.getTarget().getBranch())
                    .setDirectory(path)
                    .setURI(request.getTarget().toHttpsURL());
        try {
            git = command.call();
        } catch(Exception e) {
            throw new RuntimeException("Could not clone source repository " + request.getTarget().toHttpsURL(), e);
        }
        try {
            
            Repository repository = git.getRepository();
            repository.getConfig().setString("remote", "origin", "fetch", "+refs/pull/" + request.getNumber() + "/head:refs/remotes/origin/pr/" + request.getNumber());
            repository.getConfig().save();
            
            git.fetch()
                .setRemote("origin")
                .call();
            
            git.branchCreate()
                .setName(String.valueOf(request.getNumber()))
                .setStartPoint("origin/pr/" + request.getNumber())
                .call();

        } catch(Exception e) {
            throw new RuntimeException("Could not setup remote", e);
        }
        return new LocalStorage(path);
    }
    

    public void delete(LocalStorage storage) {

    }
    
    private File calculatePath(PullRequest request) {
        return Paths.get(WORK_FOLDER, request.getTarget().getUser(), request.getTarget().getRepository(), String.valueOf(request.getNumber())).toFile();
    }
    
}
