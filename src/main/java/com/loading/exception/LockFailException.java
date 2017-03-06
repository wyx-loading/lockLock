package com.loading.exception;

/**
 * Created by wuyuxiang on 2017/3/6.
 */
public class LockFailException extends RuntimeException {
    public LockFailException() {
    }

    public LockFailException(String message) {
        super(message);
    }

    public LockFailException(String message, Throwable cause) {
        super(message, cause);
    }

    public LockFailException(Throwable cause) {
        super(cause);
    }
}
