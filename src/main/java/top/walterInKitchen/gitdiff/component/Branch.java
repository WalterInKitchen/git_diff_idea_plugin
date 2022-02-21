package top.walterInKitchen.gitdiff.component;

import lombok.Builder;
import lombok.Getter;

@Builder(setterPrefix = "set", toBuilder = true)
@Getter
public class Branch implements TextObject {
    private Remote remote;
    private String name;

    @Override
    public String getText() {
        return this.name;
    }
}
