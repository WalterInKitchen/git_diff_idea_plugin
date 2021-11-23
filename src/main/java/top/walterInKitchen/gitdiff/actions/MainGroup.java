package top.walterInKitchen.gitdiff.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import top.walterInKitchen.gitdiff.git.GitFactory;

/**
 * @Author: walter
 * @Date: 2021/11/23
 **/
public class MainGroup extends DefaultActionGroup {
    @Override
    public void update(@NotNull AnActionEvent event) {
        final Project project = event.getProject();
        assert project != null;
        try {
            GitFactory.getGitInstance(project.getBasePath());
            event.getPresentation().setEnabled(true);
            event.getPresentation().setVisible(true);
        } catch (Exception exception) {
            event.getPresentation().setEnabled(false);
            event.getPresentation().setVisible(false);
        }
    }
}
