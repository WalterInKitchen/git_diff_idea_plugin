package top.walterInKitchen.gitdiff.component;

import org.eclipse.jgit.transport.RemoteConfig;

import java.util.ArrayList;
import java.util.List;

public class RemoteFactory {
    public static List<Remote> buildRemotes(List<RemoteConfig> remoteConfigs) {
        List<Remote> res = new ArrayList<>();
        res.add(buildLocal());
        if (remoteConfigs == null) {
            return res;
        }
        for (RemoteConfig config : remoteConfigs) {
            res.add(buildFromConfig(config));
        }
        return res;
    }

    private static Remote buildFromConfig(RemoteConfig config) {
        return Remote.builder().setName(config.getName()).setLocal(false).build();
    }

    private static Remote buildLocal() {
        return Remote.builder().setName("Local").setLocal(true).build();
    }
}
