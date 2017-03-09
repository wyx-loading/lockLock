package com.loading.ilock;

import com.loading.exception.LockFailException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;

/**
 * Created by wuyuxiang on 2017/3/9.
 *
 * Lock类的interruptiblyLock批量上锁实现
 */
public class LockInterruptiblyLock implements ILockImpl {
    public LockInterruptiblyLock() {
    }

    @Override
    public boolean lock(List<Lock> sortedLocks) {
        List<Lock> lockedList = new ArrayList<>(sortedLocks.size());
        try {
            for(Lock lock : sortedLocks) {
                lock.lockInterruptibly();
                lockedList.add(lock);
            }
            return true;
        } catch (Throwable t) {
            if(lockedList.size() > 0) {
                try {
                    for(Lock lock : lockedList) {
                        lock.unlock();
                    }
                } catch (Throwable ignored) {}
            }
            throw new LockFailException(t);
        }
    }
}
