package top.walterInKitchen.gitdiff.component;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Builder(setterPrefix = "set", toBuilder = true)
@Getter
@EqualsAndHashCode
public class Remote implements TextObject {
    private String name;
    private boolean local;

    @Override
    public String getText() {
        return this.name;
    }

    public List<Branch> listBranch(Git git) {
        if (git == null) {
            return Collections.emptyList();
        }
        List<Ref> branches;
        try {
            branches = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
            List<Branch> res = new ArrayList<>();
            for (Ref ref : branches) {
                Branch branch = buildBranch(ref);
                if (notABranchInThisRemote(branch)) {
                    continue;
                }
                res.add(branch);
            }
            return res;
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    private boolean notABranchInThisRemote(Branch branch) {
        if (branch == null || branch.getRemote() == null || !branch.getRemote().getName().equals(this.getName())) {
            return true;
        }
        if (branch.getName() == null || branch.getName().length() == 0) {
            return true;
        }
        return branch.getName().toLowerCase(Locale.ENGLISH).equals("head");
    }

    private Branch buildBranch(Ref branch) {
        Branch.BranchBuilder builder = Branch.builder();
        builder.setName(parseBranchName(branch.getName()));
        builder.setRemote(buildRemote(branch.getName()));
        return builder.build();
    }

    private Remote buildRemote(String text) {
        if (text == null || text.length() == 0) {
            return null;
        }
        if (text.startsWith("refs/heads")) {
            return Remote.builder().setLocal(true).setName("Local").build();
        }
        int endIndex = text.lastIndexOf("/");
        if (endIndex < 0 || text.length() - 1 == endIndex) {
            return null;
        }
        String substring = text.substring(0, endIndex);
        int openIndex = substring.lastIndexOf("/");
        if (openIndex < 0) {
            return null;
        }
        String remoteName = text.substring(openIndex + 1, endIndex);
        return Remote.builder().setName(remoteName).setLocal(false).build();
    }

    private String parseBranchName(String text) {
        if (text == null || text.length() == 0) {
            return "";
        }
        int index = text.lastIndexOf("/");
        if (index < 0 || text.length() - 1 == index) {
            return text;
        }
        return text.substring(index + 1);
    }

    public String getFullName() {
        if (isLocal()) {
            return "refs/heads";
        }
        return "refs/remotes/" + name;
    }
}
