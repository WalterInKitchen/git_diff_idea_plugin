package top.walterInKitchen.gitdiff.component;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.CustomStatusBarWidget;
import com.intellij.openapi.wm.StatusBar;
import org.eclipse.jgit.api.Git;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import top.walterInKitchen.gitdiff.git.GitFactory;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @Author: walter
 * @Date: 2021/12/11
 **/
public class DiffStatusWidget implements CustomStatusBarWidget, Runnable {
    private static final Integer DELAY = 2;
    private final Project project;
    private final Component component;
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    public DiffStatusWidget(Project project) {
        this.project = project;
        this.component = new Component(this);
        executorService.scheduleWithFixedDelay(this, DELAY, DELAY, TimeUnit.SECONDS);
    }

    @Override
    public JComponent getComponent() {
        return this.component;
    }

    @Override
    public @NonNls @NotNull String ID() {
        return this.getClass().getName();
    }

    @Override
    public void install(@NotNull StatusBar statusBar) {
    }

    @Override
    public void dispose() {
        executorService.shutdown();
    }

    private DiffStat getDiff() {
        Git git = GitFactory.getGitInstance(this.project.getBasePath());
        List<String> cmd = new ArrayList<>();
        cmd.add("git");
        cmd.add("diff");
        cmd.add("head");
        cmd.add("--stat");
        ProcessBuilder builder = new ProcessBuilder(cmd);
        builder.directory(new File(Objects.requireNonNull(project.getBasePath())));

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

    @Override
    public void run() {
        try {
            final DiffStat diff = getDiff();
            this.component.showChanges(diff);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private static class Component extends JLabel {
        public void showChanges(DiffStat stat) {
            if (stat == null) {
                this.setText("-|-");
                return;
            }
            StringBuilder builder = new StringBuilder();
            if (stat.getInsertions() == null) {
                builder.append("-");
            } else {
                builder.append("+").append(stat.getInsertions());
            }
            builder.append("|");
            if (stat.getDeletions() == null) {
                builder.append("-");
            } else {
                builder.append("-").append(stat.getDeletions());
            }
            this.setText(builder.toString());
            this.setToolTipText(formatToolTip(stat));
        }

        private String formatToolTip(DiffStat stat) {
            if (stat == null) {
                return "no file changed";
            }
            return "fileChanged:" + stat.getFileChanged() +
                    " insertions:" + stat.getInsertions() +
                    " deletions:" + stat.getDeletions();
        }

        public Component(DiffStatusWidget widget) {
            showChanges(null);
        }
    }
}
