package top.walterInKitchen.gitdiff.component;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.openapi.wm.CustomStatusBarWidget;
import com.intellij.openapi.wm.StatusBar;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import top.walterInKitchen.gitdiff.git.DiffStat;
import top.walterInKitchen.gitdiff.git.JgitUnCommitChangesProvider;
import top.walterInKitchen.gitdiff.git.UnCommitChangesProvider;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Idea状态栏小插件
 *
 * @Author: walter
 * @Date: 2021/12/11
 **/
public class DiffStatusWidget implements CustomStatusBarWidget, Runnable, MouseListener {
    private static final Integer DELAY = 5;
    private final Project project;
    private final Component component;
    private final ScheduledExecutorService schedulePool = Executors.newScheduledThreadPool(1);
    private final ExecutorService pendingPool = Executors.newFixedThreadPool(1);
    private final UnCommitChangesProvider unCommitChangesProvider;


    public DiffStatusWidget(Project project) {
        this.project = project;
        this.component = new Component();
        this.component.addMouseListener(this);

        monitorFileChangedEvt(this);
        schedulePool.scheduleWithFixedDelay(this, DELAY, DELAY, TimeUnit.SECONDS);
        this.unCommitChangesProvider = JgitUnCommitChangesProvider.build(this.project.getBasePath());
    }

    private void monitorFileChangedEvt(Runnable runnable) {
        project.getMessageBus().connect().subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener() {
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
        try {
            return this.unCommitChangesProvider.getUnCommittedChanged();
        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
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
            return String.format("%d files changed, %d insertions(+), %d deletions(-)",
                    stat.getFileChanged(), stat.getInsertions(), stat.getDeletions());
        }

        public Component() {
            showChanges(null);
        }
    }
}
