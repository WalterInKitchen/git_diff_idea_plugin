package top.walterInKitchen.gitdiff.exception;

/**
 * git exception
 *
 * @Author: walter
 * @Date: 2021/11/14
 **/
public class GitException extends RuntimeException {
    private final String msg;

    public GitException(String msg) {
        this.msg = msg;
    }
}
