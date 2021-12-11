package top.walterInKitchen.gitdiff.component;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidgetFactory;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import top.walterInKitchen.gitdiff.git.GitFactory;

/**
 * @Author: walter
 * @Date: 2021/12/11
 **/
public class StatusBarFactory implements StatusBarWidgetFactory {
    @Override
    public @NonNls @NotNull String getId() {
        return this.getClass().getName();
    }

    @Override
    public @Nls @NotNull String getDisplayName() {
        return "Diff Status";
    }

    @Override
    public boolean isAvailable(@NotNull Project project) {
        try {
            GitFactory.getGitInstance(project.getBasePath());
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    @Override
    public @NotNull StatusBarWidget createWidget(@NotNull Project project) {
        return new DiffStatusWidget(project);
    }

    @Override
    public void disposeWidget(@NotNull StatusBarWidget widget) {
        Disposer.dispose(widget);
    }

    @Override
    public boolean isEnabledByDefault() {
        return true;
    }

    @Override
    public boolean canBeEnabledOn(@NotNull StatusBar statusBar) {
        return true;
    }
}
