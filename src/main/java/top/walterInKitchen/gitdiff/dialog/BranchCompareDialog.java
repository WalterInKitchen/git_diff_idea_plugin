package top.walterInKitchen.gitdiff.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.walterInKitchen.gitdiff.git.GitFactory;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: walter
 * @Date: 2021/11/14
 **/
public class BranchCompareDialog extends DialogWrapper {
    private final Git git;
    private final List<String> branches = new ArrayList<>();
    private final JComboBox<String> firstBranchBox = new ComboBox<>();
    private final JComboBox<String> secondBranchBox = new ComboBox<>();
    private final JLabel changesLabel = new JLabel();
    private String firstPre = null;
    private String secondPre = null;
    private static final int WIDTH = 350;

    public BranchCompareDialog(@Nullable Project project) {
        super(project);
        assert project != null;
        this.git = GitFactory.getGitInstance(project.getBasePath());
        setTitle("Compare Two Branches");
        loadGitBranches();
        init();
    }

    private void loadGitBranches() {
        try {
            List<Ref> list = git.branchList()
                    .setListMode(ListBranchCommand.ListMode.ALL)
                    .call();
            branches.clear();
            list.forEach(b -> branches.add(b.getName()));
        } catch (GitAPIException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel();
        addBranch1Select1ToPanel(panel);
        return panel;
    }

    private void addBranch1Select1ToPanel(JPanel panel) {
        this.branches.forEach(firstBranchBox::addItem);
        firstBranchBox.addItemListener(e -> updateGitDiff());
        panel.setLayout(null);
        panel.add(firstBranchBox);
        firstBranchBox.setBounds(10, 10, WIDTH, 30);

        this.branches.forEach(secondBranchBox::addItem);
        secondBranchBox.addItemListener(e -> updateGitDiff());
        panel.add(secondBranchBox);

        secondBranchBox.setBounds(10, 50, WIDTH, 30);
        changesLabel.setBounds(10, 100, WIDTH, 30);
        panel.add(changesLabel);
        updateGitDiff();
    }

    private void updateGitDiff() {
        String branch1 = String.valueOf(this.firstBranchBox.getSelectedItem());
        String branch2 = String.valueOf(this.secondBranchBox.getSelectedItem());
        if (branch1.equals(firstPre) && branch2.equals(secondPre)) {
            return;
        }
        final Repository repository = this.git.getRepository();
        this.firstPre = branch1;
        this.secondPre = branch2;

        List<String> cmd = new ArrayList<>();
        cmd.add("git");
        cmd.add("diff");
        cmd.add("--stat");
        cmd.add(branch1);
        cmd.add(branch2);
        ProcessBuilder builder = new ProcessBuilder(cmd);
        builder.directory(repository.getDirectory());
        try {
            final Process process = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String s = null;
            String last = null;
            while ((s = reader.readLine()) != null) {
                last = s;
            }
            changesLabel.setText(last == null ? "changes: 0" : last);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Action @NotNull [] createActions() {
        return new Action[]{getOKAction()};
    }
}
