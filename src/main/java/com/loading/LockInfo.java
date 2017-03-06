package com.loading;

/**
 * Created by wuyuxiang on 2017/3/6.
 */
public class LockInfo {
    private final LockType lockType;
    private final Object lockObject;

    public LockInfo(LockType lockType, Object lockObject) {
        this.lockType = lockType;
        this.lockObject = lockObject;
    }

    public LockType getLockType() {
        return lockType;
    }

    public Object getLockObject() {
        return lockObject;
    }
}
