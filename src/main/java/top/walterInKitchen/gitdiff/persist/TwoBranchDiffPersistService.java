package top.walterInKitchen.gitdiff.persist;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.RoamingType;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @Author: walter
 * @Date: 2021/11/29
 **/
@State(name = "top.walterInKitchen.gitDiff",
        storages = {@Storage(value = "branchesV2.xml", roamingType = RoamingType.DISABLED)
        })
@Service
public final class TwoBranchDiffPersistService implements PersistentStateComponent<TwoBranchDiffPersistService.Branches> {
    private Branches branches = new Branches();

    public static TwoBranchDiffPersistService getInstance(Project project) {
        return project.getService(TwoBranchDiffPersistService.class);
    }

    @Override
    public @Nullable Branches getState() {
        return this.branches;
    }

    @Override
    public void loadState(@NotNull Branches state) {
        this.branches = state;
    }

    /**
     * @Author: walter
     * @Date: 2021/11/29
     **/
    @Data
    public static class Branches {
        private String remote1;
        private String remote2;
        private String branch1;
        private String branch2;
    }
}
