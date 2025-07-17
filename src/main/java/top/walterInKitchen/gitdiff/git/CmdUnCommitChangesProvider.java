package top.walterInKitchen.gitdiff.git;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import top.walterInKitchen.gitdiff.component.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CmdUnCommitChangesProvider implements UnCommitChangesProvider {
    private final String basePath;

    @Override
    public DiffStat getUnCommittedChanged() {
        List<String> cmd = List.of("git", "diff", "HEAD", "--stat");
        ProcessBuilder builder = new ProcessBuilder(cmd)
                .directory(new File(Objects.requireNonNull(basePath)));
        try {
            Process process = builder.start();
            int exitCode = process.waitFor();

            try (BufferedReader out = new BufferedReader(new InputStreamReader(process.getInputStream()));
                 BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {

                if (exitCode != 0) {
                    String errorMsg = err.lines().collect(Collectors.joining("\n"));
                    throw new IllegalStateException("Git diff failed: " + errorMsg);
                }

                // 读取所有行，最后一行是统计
                List<String> lines = out.lines().collect(Collectors.toList());
                if (lines.isEmpty()) {
                    return DiffStat.empty(); // 或者自定义一个“零改动”的实例
                }
                return Util.parseDiffStat(lines.get(lines.size() - 1));
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("无法执行 Git 命令", e);
        }
    }
}
