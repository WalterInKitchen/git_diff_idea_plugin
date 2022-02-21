package top.walterInKitchen.gitdiff.component;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.RemoteConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.walterInKitchen.gitdiff.git.GitFactory;
import top.walterInKitchen.gitdiff.persist.TwoBranchDiffPersistService;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author: walter
 * @Date: 2021/11/14
 **/
public class BranchCompareDialog extends DialogWrapper {
    private final Git git;
    private final TwoBranchDiffPersistService persistService;
    private String firstPre = null;
    private String secondPre = null;
    private JComboBox<Branch> branchBox1;
    private JComboBox<Branch> branchBox2;
    private JComboBox<Remote> remoteBox1;
    private JComboBox<Remote> remoteBox2;
    private JLabel diffLabel;
    private JPanel mainPanel;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final List<String> branches = new ArrayList<>();

    public BranchCompareDialog(@Nullable Project project) {
        super(project);
        assert project != null;
        this.git = GitFactory.getGitInstance(project.getBasePath());
        this.persistService = TwoBranchDiffPersistService.getInstance(project);
        initComponent();
        loadGitRemotes();
        loadGitBranches();
        loadPreviouslySelectedBranches(project);
        addChangeListener();
        branchSelectChanged();
        init();
    }

    private void addChangeListener() {
        this.branchBox1.addItemListener(it -> branchSelectChanged());
        this.branchBox2.addItemListener(it -> branchSelectChanged());
    }

    private void loadPreviouslySelectedBranches(@Nullable Project project) {
        if (project == null) {
            return;
        }
        final TwoBranchDiffPersistService.Branches state = persistService.getState();
        if (state == null) {
            return;
        }
        String branch1 = state.getBranch1();
        String branch2 = state.getBranch2();
        if (!this.branches.contains(branch1) && this.branches.size() > 0) {
            branch1 = this.branches.get(0);
        }
        if (!this.branches.contains(branch2) && this.branches.size() > 0) {
            branch2 = this.branches.get(0);
        }
        this.branchBox1.setSelectedItem(branch1);
        this.branchBox2.setSelectedItem(branch2);
    }

    private void initComponent() {
        setTitle("Compare Two Branches");
        final TwoBranchCompare frame = new TwoBranchCompare();
        this.mainPanel = frame.getMainPanel();
        this.diffLabel = frame.getDiffLabel();
        this.branchBox1 = frame.getBranchBox1();
        this.branchBox2 = frame.getBranchBox2();
        this.remoteBox1 = frame.getRemoteBox1();
        this.remoteBox2 = frame.getRemoteBox2();
    }

    private void loadGitRemotes() {
        try {
            List<RemoteConfig> remoteConfigs = git.remoteList().call();
            List<Remote> remotes = RemoteFactory.buildRemotes(remoteConfigs);
            initRemoteBox(this.remoteBox1, remotes, (evt) -> this.remote1Changed(evt, this.remoteBox1, this.branchBox1));
            initRemoteBox(this.remoteBox2, remotes, (evt) -> this.remote1Changed(evt, this.remoteBox2, this.branchBox2));
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
    }

    private void initRemoteBox(JComboBox<Remote> box, List<Remote> remotes, ItemListener listener) {
        box.setRenderer(TextObjRender.getInstance());
        remotes.forEach(box::addItem);
        box.addItemListener(listener);
    }

    private void remote1Changed(ItemEvent evt, JComboBox<Remote> remoteBox, JComboBox<Branch> branchBox) {
        branchBox.removeAllItems();
        Remote remote = (Remote) remoteBox.getSelectedItem();
        List<Branch> branches = remote == null ? Collections.emptyList() : remote.listBranch(git);
        branches.forEach(branchBox::addItem);
        branchBox.addItemListener(e -> this.branchSelectChanged());
        branchBox.setRenderer(TextObjRender.getInstance());
    }

    private void loadGitBranches() {
//        try {
//            List<Ref> list = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
//            final List<String> branches = list.stream().map(Ref::getName).collect(Collectors.toList());
//            this.branches.addAll(branches);
//            this.branches.forEach(bh -> {
//                this.branchBox1.addItem(bh);
//                this.branchBox2.addItem(bh);
//            });
//        } catch (GitAPIException exception) {
//            exception.printStackTrace();
//        }
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return this.mainPanel;
    }

    private void branchSelectChanged() {
        String branch1 = String.valueOf(this.branchBox1.getSelectedItem());
        String branch2 = String.valueOf(this.branchBox2.getSelectedItem());
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
        if (state == null) {
            return;
        }
        state.setBranch1(branch1);
        state.setBranch2(branch2);
        persistService.loadState(state);
    }

    private void updateGitDiff(String branch1, String branch2) {
        showLoading();
        executorService.submit(() -> analysisAndShowDiff(branch1, branch2));
    }

    private void analysisAndShowDiff(String branch1, String branch2) {
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

    private void showLoading() {
        this.diffLabel.setText("Loading...");
    }

    private void showDiff(DiffStat diffStat) {
        if (diffStat == null) {
            this.diffLabel.setText("0");
            return;
        }
        int files = Optional.ofNullable(diffStat.getFileChanged()).orElse(0);
        int ins = Optional.ofNullable(diffStat.getInsertions()).orElse(0);
        int dls = Optional.ofNullable(diffStat.getDeletions()).orElse(0);

        String changes = (ins + dls) + "(+" + ins + "|-" + dls + ")  " + "[" + diffStat.getFileChanged() + " file" + ((files > 1) ? "s" : "") + "]";
        this.diffLabel.setText(changes);
    }

    @Override
    protected Action @NotNull [] createActions() {
        return new Action[]{getOKAction()};
    }
}
