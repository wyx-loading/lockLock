package com.loading;

/**
 * Created by wuyuxiang on 2017/3/6.
 */
public enum LockType {
    synchronize,
    ilock_tryLock,
    ilock_interruptiblyLock,
    ilock_syncLock,
    atomic,
    ;
}
