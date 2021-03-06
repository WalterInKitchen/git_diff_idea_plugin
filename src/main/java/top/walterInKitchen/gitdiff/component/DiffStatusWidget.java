package top.walterInKitchen.gitdiff.component;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
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
import java.util.Optional;
import java.util.concurrent.ExecutorService;
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
    private final ScheduledExecutorService schedulePool = Executors.newScheduledThreadPool(1);
    private final ExecutorService pendingPool = Executors.newFixedThreadPool(1);

    public DiffStatusWidget(Project project) {
        this.project = project;
        this.component = new Component();
        this.component.addMouseListener(this);

        monitorFileChangedEvt(this);
        schedulePool.scheduleWithFixedDelay(this, DELAY, DELAY, TimeUnit.SECONDS);
    }

    private void monitorFileChangedEvt(Runnable runnable) {
        project.getMessageBus().connect().subscribe(VirtualFileManager.VFS_CHANGES,
                new BulkFileListener() {
                    @Override
                    public void after(@NotNull List<? extends VFileEvent> events) {
                        runnable.run();
                    }
                });
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
        schedulePool.shutdown();
    }

    private DiffStat getDiff() {
        List<String> cmd = new ArrayList<>();
        cmd.add("git");
        cmd.add("diff");
        cmd.add("HEAD");
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
        pendingPool.submit(this::updateWidget);
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    private static class Component extends JLabel {
        public void showChanges(DiffStat stat) {
            this.setToolTipText(formatToolTip(stat));
            if (stat == null) {
                this.setText("0");
                return;
            }
            final Integer insertions = Optional.ofNullable(stat.getInsertions()).orElse(0);
            final Integer deletions = Optional.ofNullable(stat.getDeletions()).orElse(0);
            int sum = insertions + deletions;
            StringBuilder builder = new StringBuilder().append(sum);
            if (sum != 0) {
                builder.append("(").append("+").append(insertions).append("|").append("-").append(deletions).append(")");
            }
            this.setText(builder.toString());
        }

        private String formatToolTip(DiffStat stat) {
            if (stat == null) {
                return "no file changed";
            }
            return "fileChanged:" + stat.getFileChanged() + " insertions:" + stat.getInsertions() + " deletions:" + stat.getDeletions();
        }

        public Component() {
            showChanges(null);
        }
    }
}
