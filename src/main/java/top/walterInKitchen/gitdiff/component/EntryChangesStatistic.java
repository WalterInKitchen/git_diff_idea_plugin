package top.walterInKitchen.gitdiff.component;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.patch.HunkHeader;
import org.eclipse.jgit.util.io.NullOutputStream;

import java.io.IOException;
import java.util.List;

/**
 * statistic entries changes
 *
 * @Author: catch
 * @Date: 2022/5/22
 **/
public class EntryChangesStatistic {
    /**
     * statistic changes
     *
     * @param repository repo
     * @param entries    entries
     * @return diff
     * @throws IOException exception
     */
    public static DiffStat statistic(Repository repository, List<DiffEntry> entries) throws IOException {
        DiffFormatter df = new DiffFormatter(NullOutputStream.INSTANCE);
        df.setDiffComparator(RawTextComparator.WS_IGNORE_ALL);
        df.setRepository(repository);

        int addSize = 0;
        int subSize = 0;
        int files = 0;
        for (DiffEntry entry : entries) {
            FileHeader fileHeader = df.toFileHeader(entry);
            files += 1;
            List<? extends HunkHeader> hunks = fileHeader.getHunks();
            for (HunkHeader hunk : hunks) {
                for (Edit edit : hunk.toEditList()) {
                    subSize += edit.getEndA() - edit.getBeginA();
                    addSize += edit.getEndB() - edit.getBeginB();
                }
            }
        }

        return DiffStat.builder().setDeletions(subSize)
                .setInsertions(addSize)
                .setFileChanged(files)
                .build();
    }
}
