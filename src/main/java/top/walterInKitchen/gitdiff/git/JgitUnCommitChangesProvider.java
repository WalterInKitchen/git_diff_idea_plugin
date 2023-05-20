package top.walterInKitchen.gitdiff.git;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
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
    private final Repository repository;

    @Override
    public DiffStat getUnCommittedChanged() {
        try {
            AbstractTreeIterator oldTree = getOldTree(repository);
            AbstractTreeIterator newTree = new FileTreeIterator(repository);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DiffFormatter diffFormatter = new DiffFormatter(out);
            diffFormatter.setDiffComparator(CrlfRawTextComparator.INSTANCE);
            diffFormatter.setRepository(repository);
            List<DiffEntry> entries = diffFormatter.scan(oldTree, newTree);

            int addLine = 0;
            int delLine = 0;
            int files = 0;

            for (DiffEntry entry : entries) {
                out.reset();
                files++;

                diffFormatter.format(entry);
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
        Repository repository = gitInstance.getRepository();
        repository.getConfig().setBoolean("core", null, "autocrlf", false);
        return JgitUnCommitChangesProvider.builder().repository(repository).build();
    }
}
