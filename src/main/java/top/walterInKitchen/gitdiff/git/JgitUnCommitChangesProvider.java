package top.walterInKitchen.gitdiff.git;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.FileTreeIterator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.eclipse.jgit.lib.Constants.HEAD;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class JgitUnCommitChangesProvider implements UnCommitChangesProvider {
    private final Git git;

    @Override
    public DiffStat getUnCommittedChanged() {
        try {
            Repository repository = git.getRepository();
            AbstractTreeIterator oldTree = getOldTree(repository);
            AbstractTreeIterator newTree = new FileTreeIterator(repository);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DiffFormatter diffFormatter = new DiffFormatter(out);
            diffFormatter.setRepository(repository);
            List<DiffEntry> entries = diffFormatter.scan(oldTree, newTree);

            int addLine = 0;
            int delLine = 0;
            int files = 0;

            for (DiffEntry entry : entries) {
                try {
                    diffFormatter.format(entry);
                } catch (MissingObjectException exception) {
                    continue;
                }
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
        } catch (IOException exc) {
            throw new RuntimeException(exc);
        }
    }

    private AbstractTreeIterator getOldTree(Repository repository) throws IOException {
        ObjectId head = repository.resolve(HEAD + "^{tree}"); //$NON-NLS-1$
        if (head == null) {
            throw new RuntimeException("no head found");
        }
        CanonicalTreeParser oldTree = new CanonicalTreeParser();
        try (ObjectReader reader = repository.newObjectReader()) {
            oldTree.reset(reader, head);
        }
        return oldTree;
    }

    public static JgitUnCommitChangesProvider build(String basePath) {
        Git gitInstance = GitFactory.getGitInstance(basePath);
        return JgitUnCommitChangesProvider.builder().git(gitInstance).build();
    }
}
