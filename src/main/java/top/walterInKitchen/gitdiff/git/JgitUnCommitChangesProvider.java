package top.walterInKitchen.gitdiff.git;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.patch.HunkHeader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class JgitUnCommitChangesProvider implements UnCommitChangesProvider {
    private static final Logger log = LoggerFactory.getLogger(JgitUnCommitChangesProvider.class);
    private final String basePath;

    @Override
    public DiffStat getUnCommittedChanged() {
        log.info(">> [{}] 开始计算未提交改动，仓库路径 = {}", JgitUnCommitChangesProvider.class.getSimpleName(), basePath);
        try (Repository repo = new FileRepositoryBuilder()
                .setGitDir(new File(basePath, ".git"))
                .readEnvironment()
                .findGitDir()
                .build();
             ObjectReader reader = repo.newObjectReader()) {

            // 1. 找到 HEAD 提交的 tree 对象
            CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
            try (RevWalk revWalk = new RevWalk(repo)) {
                RevCommit headCommit = revWalk.parseCommit(repo.resolve("HEAD"));
                oldTreeIter.reset(reader, headCommit.getTree().getId());
            }

            // 2. 准备新的工作区迭代器
            FileTreeIterator newTreeIter = new FileTreeIterator(repo);

            // 3. 扫描 diff entries，并统计行数
            int totalFiles = 0;
            int totalInsertions = 0;
            int totalDeletions = 0;
            try (DiffFormatter diffFormatter = new DiffFormatter(org.eclipse.jgit.util.io.DisabledOutputStream.INSTANCE)) {
                diffFormatter.setRepository(repo);
                List<DiffEntry> diffs = diffFormatter.scan(oldTreeIter, newTreeIter);
                totalFiles = diffs.size();

                for (DiffEntry entry : diffs) {
                    FileHeader header = diffFormatter.toFileHeader(entry);
                    for (HunkHeader hunk : header.getHunks()) {
                        EditList edits = hunk.toEditList();
                        for (Edit edit : edits) {
                            switch (edit.getType()) {
                                case INSERT:
                                    totalInsertions += edit.getLengthB();
                                    break;
                                case DELETE:
                                    totalDeletions += edit.getLengthA();
                                    break;
                                case REPLACE:
                                    totalInsertions += edit.getLengthB();
                                    totalDeletions += edit.getLengthA();
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                }
            }

            DiffStat diff = DiffStat.builder()
                    .setFileChanged(totalFiles)
                    .setInsertions(totalInsertions)
                    .setDeletions(totalDeletions)
                    .build();
            log.info(">> 变动 = {}", diff);
            return diff;

        } catch (IOException e) {
            throw new RuntimeException("无法使用 JGit 读取未提交改动", e);
        }
    }

    public static JgitUnCommitChangesProvider build(String basePath) {
        return JgitUnCommitChangesProvider.builder().basePath(basePath).build();
    }
}