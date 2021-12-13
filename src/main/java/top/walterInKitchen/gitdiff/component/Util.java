package top.walterInKitchen.gitdiff.component;

import javax.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author: walter
 * @Date: 2021/12/11
 **/
public class Util {
    private static final Pattern DIFF_FILE_CHANGED = Pattern.compile("(\\d+)\\s+file.*");
    private static final Pattern DIFF_INSERTION = Pattern.compile(".*?(\\d+)\\s+insertion.*");
    private static final Pattern DIFF_DELETION = Pattern.compile(".*?(\\d+)\\s+deletion.*");

    /**
     * parse git diff last line to stat
     *
     * @param source string like this: 8 files changed, 440 insertions(+), 281 deletions(-)
     * @return the object represent
     */
    @Nullable
    public static DiffStat parseDiffStat(String source) {
        if (source == null) {
            return null;
        }
        final String plain = source.trim();
        final DiffStat.DiffStatBuilder builder = DiffStat.builder().setDeletions(0).setInsertions(0).setFileChanged(0);
        final Matcher fileMatcher = DIFF_FILE_CHANGED.matcher(plain);
        if (fileMatcher.matches()) {
            builder.setFileChanged(Integer.parseInt(fileMatcher.group(1)));
        }
        final Matcher insertionMatcher = DIFF_INSERTION.matcher(plain);
        if (insertionMatcher.matches()) {
            builder.setInsertions(Integer.parseInt(insertionMatcher.group(1)));
        }
        final Matcher deletionMatcher = DIFF_DELETION.matcher(plain);
        if (deletionMatcher.matches()) {
            builder.setDeletions(Integer.parseInt(deletionMatcher.group(1)));
        }
        return builder.build();
    }
}
