package top.walterInKitchen.gitdiff.component;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * @Author: walter
 * @Date: 2021/12/11
 **/
@Getter
@Builder(toBuilder = true, setterPrefix = "set")
@ToString
public class DiffStat {
    private Integer fileChanged;
    private Integer insertions;
    private Integer deletions;
}
