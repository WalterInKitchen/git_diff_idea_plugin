package top.walterInKitchen.gitdiff.component;

import javax.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author: walter
 * @Date: 2021/12/11
 **/
public class Util {
    private static final Pattern DIFF_STAT_PATTERN = Pattern.compile(".*?(\\d+).*?(\\d+).*?(\\d+).*");

    /**
     * parse git diff last line to stat
     *
     * @param last string like this: 8 files changed, 440 insertions(+), 281 deletions(-)
     * @return the object represent
     */
    @Nullable
    public static DiffStat parseDiffStat(String last) {
        if (last == null) {
            return null;
        }
        final Matcher matcher = DIFF_STAT_PATTERN.matcher(last);
        if (!matcher.matches()) {
            return DiffStat.builder().setDeletions(0).setInsertions(0).setFileChanged(0).build();
        }
        final DiffStat.DiffStatBuilder builder = DiffStat.builder();
        builder.setFileChanged(Integer.parseInt(matcher.group(1)));
        builder.setInsertions(Integer.parseInt(matcher.group(2)));
        builder.setDeletions(Integer.parseInt(matcher.group(3)));
        return builder.build();
    }

}
