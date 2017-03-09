package com.loading.exception;

/**
 * Created by wuyuxiang on 2017/3/9.
 *
 * 上锁解锁时，若获得锁资源的线程id和当前操作线程id不一致时，抛出本异常
 */
public class LockCrossThreadException extends RuntimeException {
    public LockCrossThreadException() {
    }

    public LockCrossThreadException(String message) {
        super(message);
    }

    public LockCrossThreadException(String message, Throwable cause) {
        super(message, cause);
    }

    public LockCrossThreadException(Throwable cause) {
        super(cause);
    }

    public LockCrossThreadException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
