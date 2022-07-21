package top.walterInKitchen.gitdiff.component;

import lombok.Builder;
import lombok.Getter;

/**
 * @Author: walter
 * @Date: 2021/12/11
 **/
@Getter
@Builder(toBuilder = true, setterPrefix = "set")
public class DiffStat {
    private Integer fileChanged;
    private Integer insertions;
    private Integer deletions;
}
