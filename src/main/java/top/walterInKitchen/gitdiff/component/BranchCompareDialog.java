package top.walterInKitchen.gitdiff.component;

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
import top.walterInKitchen.gitdiff.persist.TwoBranchDiffPersistService;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @Author: walter
 * @Date: 2021/11/14
 **/
public class BranchCompareDialog extends DialogWrapper {
    private final String PREFIX = "top.walterInKitchen.gitdiff.dialog.BranchCompareDialog";
    private final Git git;
    private final TwoBranchDiffPersistService persistService;
    private String firstPre = null;
    private String secondPre = null;
    private JComboBox<String> comboBox1;
    private JComboBox<String> comboBox2;
    private JLabel diffLabel;
    private JPanel mainPanel;
    private final List<String> branches = new ArrayList<>();

    public BranchCompareDialog(@Nullable Project project) {
        super(project);
        assert project != null;
        this.git = GitFactory.getGitInstance(project.getBasePath());
        this.persistService = TwoBranchDiffPersistService.getInstance(project);
        initComponent();
        loadGitBranches();
        loadPreviouslySelectedBranches(project);
        addChangeListener();
        branchSelectChanged();
        init();
    }

    private void addChangeListener() {
        this.comboBox1.addItemListener(it -> branchSelectChanged());
        this.comboBox2.addItemListener(it -> branchSelectChanged());
    }

    private void loadPreviouslySelectedBranches(@Nullable Project project) {
        if (project == null) {
            return;
        }
        final TwoBranchDiffPersistService.Branches state = persistService.getState();
        String branch1 = state.getBranch1();
        String branch2 = state.getBranch2();
        if (!this.branches.contains(branch1) && this.branches.size() > 0) {
            branch1 = this.branches.get(0);
        }
        if (!this.branches.contains(branch2) && this.branches.size() > 0) {
            branch2 = this.branches.get(0);
        }
        this.comboBox1.setSelectedItem(branch1);
        this.comboBox2.setSelectedItem(branch2);
    }

    private void initComponent() {
        setTitle("Compare Two Branches");
        final TwoBranchCompare frame = new TwoBranchCompare();
        this.mainPanel = frame.getMainPanel();
        this.diffLabel = frame.getDiffLabel();
        this.comboBox1 = frame.getComboBox1();
        this.comboBox2 = frame.getComboBox2();
    }

    private void loadGitBranches() {
        try {
            List<Ref> list = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
            final List<String> branches = list.stream().map(Ref::getName).collect(Collectors.toList());
            this.branches.addAll(branches);
            this.branches.forEach(bh -> {
                this.comboBox1.addItem(bh);
                this.comboBox2.addItem(bh);
            });
        } catch (GitAPIException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return this.mainPanel;
    }

    private void branchSelectChanged() {
        String branch1 = String.valueOf(this.comboBox1.getSelectedItem());
        String branch2 = String.valueOf(this.comboBox2.getSelectedItem());
        if (branch1.equals(firstPre) && branch2.equals(secondPre)) {
            return;
        }
        this.firstPre = branch1;
        this.secondPre = branch2;

        updateGitDiff(branch1, branch2);
        persistSelected(branch1, branch2);
    }

    private void persistSelected(String branch1, String branch2) {
        final TwoBranchDiffPersistService.Branches state = persistService.getState();
        state.setBranch1(branch1);
        state.setBranch2(branch2);
        persistService.loadState(state);
    }

    private void updateGitDiff(String branch1, String branch2) {
        final Repository repository = this.git.getRepository();
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
            DiffStat diffStat = Util.parseDiffStat(last);
            showDiff(diffStat);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showDiff(DiffStat diffStat) {
        if (diffStat == null) {
            this.diffLabel.setText("0");
            return;
        }
        int files = Optional.ofNullable(diffStat.getFileChanged()).orElse(0);
        int ins = Optional.ofNullable(diffStat.getInsertions()).orElse(0);
        int dls = Optional.ofNullable(diffStat.getDeletions()).orElse(0);

        String changes = (ins + dls) + "(+" + ins + "|-" + dls + ")  "
                + "[" + diffStat.getFileChanged() + " file" + ((files > 1) ? "s" : "") + "]";
        this.diffLabel.setText(changes);
    }

    @Override
    protected Action @NotNull [] createActions() {
        return new Action[]{getOKAction()};
    }
}
