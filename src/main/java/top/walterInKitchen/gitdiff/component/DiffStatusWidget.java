package top.walterInKitchen.gitdiff.component;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.CustomStatusBarWidget;
import com.intellij.openapi.wm.StatusBar;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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
public class DiffStatusWidget implements CustomStatusBarWidget, Runnable, MouseListener {
    private static final Integer DELAY = 2;
    private final Project project;
    private final Component component;
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    public DiffStatusWidget(Project project) {
        this.project = project;
        this.component = new Component(this);
        this.component.addMouseListener(this);

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
            updateWidget();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void updateWidget() {
        final DiffStat diff = getDiff();
        this.component.showChanges(diff);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        updateWidget();
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    private static class Component extends JLabel {
        public void showChanges(DiffStat stat) {
            this.setToolTipText(formatToolTip(stat));
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
