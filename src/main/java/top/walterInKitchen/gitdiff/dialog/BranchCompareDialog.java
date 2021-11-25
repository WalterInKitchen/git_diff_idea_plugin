package top.walterInKitchen.gitdiff.dialog;

import com.intellij.openapi.project.Project;
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
    private String firstPre = null;
    private String secondPre = null;
    private JComboBox<String> comboBox1;
    private JComboBox<String> comboBox2;
    private JLabel diffLabel;
    private JPanel mainPanel;

    public BranchCompareDialog(@Nullable Project project) {
        super(project);
        assert project != null;
        this.git = GitFactory.getGitInstance(project.getBasePath());
        setTitle("Compare Two Branches");
        initComponent();
        loadGitBranches();
        updateGitDiff();
        init();
    }

    private void initComponent() {
        final TwoBranchCompare frame = new TwoBranchCompare();
        this.mainPanel = frame.getMainPanel();
        this.diffLabel = frame.getDiffLabel();

        this.comboBox1 = frame.getComboBox1();
        this.comboBox1.addItemListener(it -> updateGitDiff());

        this.comboBox2 = frame.getComboBox2();
        this.comboBox2.addItemListener(it -> updateGitDiff());
    }

    private void loadGitBranches() {
        try {
            List<Ref> list = git.branchList()
                    .setListMode(ListBranchCommand.ListMode.ALL)
                    .call();
            list.stream().map(Ref::getName)
                    .forEach(name -> {
                        this.comboBox1.addItem(name);
                        this.comboBox2.addItem(name);
                    });
        } catch (GitAPIException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return this.mainPanel;
    }

    private void updateGitDiff() {
        String branch1 = String.valueOf(this.comboBox1.getSelectedItem());
        String branch2 = String.valueOf(this.comboBox2.getSelectedItem());
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
            this.diffLabel.setText(last == null ? "changes: 0" : last);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Action @NotNull [] createActions() {
        return new Action[]{getOKAction()};
    }
}
