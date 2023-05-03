package top.walterInKitchen.gitdiff.git;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import top.walterInKitchen.gitdiff.component.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CmdUnCommitChangesProvider implements UnCommitChangesProvider {
    private final String basePath;

    @Override
    public DiffStat getUnCommittedChanged() {
        List<String> cmd = new ArrayList<>();
        cmd.add("git");
        cmd.add("diff");
        cmd.add("HEAD");
        cmd.add("--stat");
        ProcessBuilder builder = new ProcessBuilder(cmd);
        builder.directory(new File(Objects.requireNonNull(this.basePath)));

        try {
            final Process process = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String s = null;
            String last = null;
            while ((s = reader.readLine()) != null) {
                last = s;
            }
            return Util.parseDiffStat(last);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
