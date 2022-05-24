package top.walterInKitchen.gitdiff.component;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import java.io.IOException;
import java.util.List;

/**
 * compare two branch
 *
 * @Author: catch
 * @Date: 2022/5/22
 **/
public class BranchComparator {
    private final Repository repository;

    /**
     * constructor
     *
     * @param repository rep
     */
    public BranchComparator(Repository repository) {
        this.repository = repository;
    }

    /**
     * show diff
     *
     * @param branch1 branch1
     * @param branch2 branch2
     * @return diff
     */
    public DiffStat showDiff(String branch1, String branch2) {
        try {
            return parseChanges(branch1, branch2);
        } catch (IOException | GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    private DiffStat parseChanges(String b1, String b2) throws IOException, GitAPIException {
        AbstractTreeIterator oldTreeParser = prepareTreeParser(repository, b1);
        AbstractTreeIterator newTreeParser = prepareTreeParser(repository, b2);

        Git git = new Git(repository);
        List<DiffEntry> diff = git.diff().setOldTree(oldTreeParser).setNewTree(newTreeParser).call();
        return EntryChangesStatistic.statistic(repository, diff);
    }

    private static AbstractTreeIterator prepareTreeParser(Repository repository, String ref) throws IOException {
        Ref head = repository.exactRef(ref);
        try (RevWalk walk = new RevWalk(repository)) {
            RevCommit commit = walk.parseCommit(head.getObjectId());
            RevTree tree = walk.parseTree(commit.getTree().getId());

            CanonicalTreeParser treeParser = new CanonicalTreeParser();
            try (ObjectReader reader = repository.newObjectReader()) {
                treeParser.reset(reader, tree.getId());
            }
            walk.dispose();
            return treeParser;
        }
    }
}
