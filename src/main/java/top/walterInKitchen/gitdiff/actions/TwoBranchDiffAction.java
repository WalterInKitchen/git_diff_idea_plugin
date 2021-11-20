package top.walterInKitchen.gitdiff.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import top.walterInKitchen.gitdiff.dialog.BranchCompareDialog;

/**
 * @Author: walter
 * @Date: 2021/11/13
 **/
public class TwoBranchDiffAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        BranchCompareDialog dialog = new BranchCompareDialog(project);
        dialog.setSize(410, 220);
        dialog.setResizable(false);
        dialog.show();
    }
}
