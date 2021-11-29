package top.walterInKitchen.gitdiff.persist;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @Author: walter
 * @Date: 2021/11/29
 **/
@State(name = "top.walterInKitchen.gitDiff"
        , reloadable = true
        , storages = {@Storage(value = "branches.xml", roamingType = RoamingType.DISABLED)
})
@Service
public final class TwoBranchDiffPersistService implements PersistentStateComponent<TwoBranchDiffPersistService.Branches> {
    public Branches branches = new Branches();

    public static TwoBranchDiffPersistService getInstance(Project project) {
        return project.getService(TwoBranchDiffPersistService.class);
    }

    @Override
    public @Nullable Branches getState() {
        return this.branches;
    }

    @Override
    public void loadState(@NotNull Branches state) {
        XmlSerializerUtil.copyBean(state, this.branches);
    }

    /**
     * @Author: walter
     * @Date: 2021/11/29
     **/
    public static class Branches {
        private String branch1;
        private String branch2;

        public Branches() {
        }

        public String getBranch1() {
            return branch1;
        }

        public void setBranch1(String branch1) {
            this.branch1 = branch1;
        }

        public String getBranch2() {
            return branch2;
        }

        public void setBranch2(String branch2) {
            this.branch2 = branch2;
        }
    }
}
