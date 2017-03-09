package com.loading.ilock;

import com.loading.exception.LockFailException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * Created by wuyuxiang on 2017/3/9.
 *
 * Lock类的tryLock批量上锁实现
 */
public class LockTryLock implements ILockImpl {
    public static final long EXPIRE_MILLIS = 3000;

    private final long expireMillis;

    public LockTryLock() {
        this.expireMillis = EXPIRE_MILLIS;
    }
    public LockTryLock(long expireMillis) {
        if(expireMillis < 1) {
            throw new IllegalArgumentException("expireMillis must > 0");
        }
        this.expireMillis = expireMillis;
    }

    public long getExpireMillis() {
        return expireMillis;
    }

    @Override
    public boolean lock(List<Lock> sortedLocks) {
        List<Lock> lockedList = new ArrayList<>(sortedLocks.size());
        Throwable ex = null;
        try {
            for(Lock lock : sortedLocks) {
                if(lock.tryLock(expireMillis, TimeUnit.MILLISECONDS)) {
                    lockedList.add(lock);
                } else {
                    break;
                }
            }
        } catch (Throwable t) {
            ex = t;
        }
        if(lockedList.size() != sortedLocks.size()) {
            if(lockedList.size() > 0) {
                try {
                    for(Lock lock : lockedList) {
                        lock.unlock();
                    }
                } catch (Throwable ignored) {}
            }
            if(ex != null) {
                throw new LockFailException(ex);
            }
            return false;
        }
        return true;
    }
}
