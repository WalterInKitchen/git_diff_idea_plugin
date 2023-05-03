package top.walterInKitchen.gitdiff.component;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import top.walterInKitchen.gitdiff.git.DiffStat;

/**
 * @Author: walter
 * @Date: 2021/12/13
 **/
class UtilTest {
    @Test
    public void test_parseDiffStat_given_source_when_stringIsEmpty_then_returnDefault() {
        final DiffStat res = Util.parseDiffStat("");
        Assertions.assertNotNull(res);
        Assertions.assertEquals(0, res.getFileChanged());
        Assertions.assertEquals(0, res.getInsertions());
        Assertions.assertEquals(0, res.getDeletions());
    }

    @Test
    public void test_parseDiffStat_given_source_when_stringIsNull_then_returnNull() {
        final DiffStat res = Util.parseDiffStat(null);
        Assertions.assertNull(res);
    }

    @Test
    public void test_parseDiffStat_given_source_when_onlyAddFile_then_returnWithNonZeroAdd() {
        final DiffStat res = Util.parseDiffStat("1 file changed....");
        Assertions.assertNotNull(res);
        Assertions.assertEquals(1, res.getFileChanged());
        Assertions.assertEquals(0, res.getInsertions());
        Assertions.assertEquals(0, res.getDeletions());
    }

    @Test
    public void test_parseDiffStat_given_source_when_onlyAddFiles_then_returnWithNonZeroAdd() {
        final DiffStat res = Util.parseDiffStat("2 files changed....");
        Assertions.assertNotNull(res);
        Assertions.assertEquals(2, res.getFileChanged());
        Assertions.assertEquals(0, res.getInsertions());
        Assertions.assertEquals(0, res.getDeletions());
    }

    @Test
    public void test_parseDiffStat_given_source_when_containsOnlyInsertions_then_returnWithNonZeroInsertions() {
        final DiffStat res = Util.parseDiffStat("3 insertions(+)");
        Assertions.assertNotNull(res);
        Assertions.assertEquals(0, res.getFileChanged());
        Assertions.assertEquals(3, res.getInsertions());
        Assertions.assertEquals(0, res.getDeletions());
    }

    @Test
    public void test_parseDiffStat_given_source_when_containsOnlyOneInsertion_then_returnWithNonZeroInsertions() {
        final DiffStat res = Util.parseDiffStat("1 insertion(+)");
        Assertions.assertNotNull(res);
        Assertions.assertEquals(0, res.getFileChanged());
        Assertions.assertEquals(1, res.getInsertions());
        Assertions.assertEquals(0, res.getDeletions());
    }

    @Test
    public void test_parseDiffStat_given_source_when_containsOnlyOneDeletions_then_returnWithNonZeroDeletions() {
        final DiffStat res = Util.parseDiffStat(" 1 deletion(-)");
        Assertions.assertNotNull(res);
        Assertions.assertEquals(0, res.getFileChanged());
        Assertions.assertEquals(0, res.getInsertions());
        Assertions.assertEquals(1, res.getDeletions());
    }

    @Test
    public void test_parseDiffStat_given_source_when_containsMultiDeletions_then_returnWithNonZeroDeletions() {
        final DiffStat res = Util.parseDiffStat(" 10 deletions(-)");
        Assertions.assertNotNull(res);
        Assertions.assertEquals(0, res.getFileChanged());
        Assertions.assertEquals(0, res.getInsertions());
        Assertions.assertEquals(10, res.getDeletions());
    }

    @Test
    public void test_parseDiffStat_given_source_when_containsInsertAndDeletion_then_returnWithNonZeroRes() {
        final DiffStat res = Util.parseDiffStat(" 2 files changed, 11 insertions(+), 20 deletions(-)");
        Assertions.assertNotNull(res);
        Assertions.assertEquals(2, res.getFileChanged());
        Assertions.assertEquals(11, res.getInsertions());
        Assertions.assertEquals(20, res.getDeletions());
    }
}