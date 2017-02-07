package jabs;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;

public class GitWatcher {

    private final Repository repository;
    private String lastCommit = "";

    public GitWatcher(String gitDir) {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        try {
            repository = builder.setGitDir(new File(gitDir))
                    .readEnvironment() // scan environment GIT_* variables
                    .findGitDir() // scan up the file system tree
                    .build();

            lastCommit = noteLastCommit();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    boolean hasLastCommitChanged() throws Exception {
        String sha = noteLastCommit();
        return !sha.equals(lastCommit);
    }

    private String noteLastCommit() throws IOException {
        Ref master = repository.exactRef("refs/heads/master");
        ObjectId objectId = master.getObjectId();
        return objectId.getName();
    }
}
