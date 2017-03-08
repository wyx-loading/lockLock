package com.loading.ilock;

import com.loading.LockSorter;
import com.loading.exception.LockFailException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * Created by wuyuxiang on 2017/3/6.
 */
public class ILockHelper {

    /**
     * 设置超时时间的上锁操作
     * @param expireMillis
     * @param locks
     * @return
     */
    public static boolean tryLock(long expireMillis, Lock... locks) {
        List<Lock> lockList = LockSorter.sort(locks);
        List<Lock> lockedList = new ArrayList<>(lockList.size());
        Throwable ex = null;
        try {
            for(Lock lock : lockList) {
                if(lock.tryLock(expireMillis, TimeUnit.MILLISECONDS)) {
                    lockedList.add(lock);
                } else {
                    break;
                }
            }
        } catch (Throwable t) {
            ex = t;
        }
        if(lockList.size() != lockedList.size()) {
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

    /**
     * 可以响应线程中断的上锁操作
     * @param locks
     * @return
     */
    public static List<Lock> interruptiblyLock(Lock... locks) {
        List<Lock> lockList = LockSorter.sort(locks);
        List<Lock> lockedList = new ArrayList<>(lockList.size());
        try {
            for(Lock lock : lockList) {
                lock.lockInterruptibly();
                lockedList.add(lock);
            }
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
        return lockList;
    }

    /**
     * 上锁操作
     * @param locks
     * @return
     */
    public static List<Lock> syncLock(Lock... locks) {
        List<Lock> lockList = LockSorter.sort(locks);
        List<Lock> lockedList = new ArrayList<>(lockList.size());
        try {
            for(Lock lock : lockList) {
                lock.lock();
                lockedList.add(lock);
            }
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
        return lockList;
    }

}
