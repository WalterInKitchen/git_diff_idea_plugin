package top.walterInKitchen.gitdiff.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import top.walterInKitchen.gitdiff.exception.GitException;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @Author: walter
 * @Date: 2021/11/14
 **/
public class GitFactory {
    public static Git getGitInstance(String basePath) throws GitException {
        return new Git(buildRepositoryFromPath(basePath));
    }

    private static Repository buildRepositoryFromPath(String basePath) throws GitException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Path path = Paths.get(basePath, ".git");
        try {
            File file = path.toFile();
            assertIfNotValidFolder(file);
            return builder.setGitDir(file)
                    .readEnvironment()
                    .findGitDir()
                    .build();
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new GitException("open git repository error");
        }
    }

    private static void assertIfNotValidFolder(File file) {
        if (file == null || !file.exists() || !file.isDirectory()) {
            throw new GitException("git folder not exist");
        }
    }
}
