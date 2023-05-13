package top.walterInKitchen.gitdiff.git;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class JgitUnCommitChangesProvider implements UnCommitChangesProvider {
    private final Git git;

    @Override
    public DiffStat getUnCommittedChanged() {
        try {
            List<DiffEntry> entries = git.diff()
                    .setCached(true)
                    .call();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DiffFormatter diffFormatter = new DiffFormatter(out);
            diffFormatter.setRepository(git.getRepository());

            int addLine = 0;
            int delLine = 0;
            int files = 0;

            for (DiffEntry entry : entries) {
                diffFormatter.format(entry);
                files++;
            }

            String diffContent = out.toString();
            boolean start = false;
            for (String line : diffContent.split("\n")) {
                if (line.startsWith("@@")) {
                    start = true;
                    continue;
                }
                if (line.startsWith("diff")) {
                    start = false;
                }
                if (!start) {
                    continue;
                }
                addLine += line.startsWith("+") ? 1 : 0;
                delLine += line.startsWith("-") ? 1 : 0;
            }

            return DiffStat.builder()
                    .setFileChanged(files)
                    .setInsertions(addLine)
                    .setDeletions(delLine)
                    .build();
        } catch (GitAPIException | IOException exc) {
            throw new RuntimeException(exc);
        }
    }

    public static JgitUnCommitChangesProvider build(String basePath) {
        Git gitInstance = GitFactory.getGitInstance(basePath);
        return JgitUnCommitChangesProvider.builder().git(gitInstance).build();
    }
}
