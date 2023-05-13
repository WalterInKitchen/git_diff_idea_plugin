package top.walterInKitchen.gitdiff.component;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class JgitTest {
    public static void main(String[] args) throws GitAPIException, IOException {
        new JgitTest().diffHeadStatsTest();
    }

    @Test
    public void diffHeadStatsTest() throws IOException, GitAPIException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repository = builder.setGitDir(new File("/Users/walter/temp/jgit/")).readEnvironment().findGitDir().build();
        Git git = new Git(repository);

        List<DiffEntry> entries = git.diff()
                .setOldTree(null)
                .setCached(true).call();
        for (DiffEntry entry : entries) {
            System.out.println(entry);
        }
    }

}
