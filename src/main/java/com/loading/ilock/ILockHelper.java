package com.loading.ilock;

import com.loading.LockSorter;
import com.loading.exception.LockFailException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * Created by wuyuxiang on 2017/3/6.
 *
 * BUG Found 1:
 * ILockHelperTest中，先构造locks, locksRevert，然后传入ILockHelper.tryLock等方法中，
 * 由于Arrays.asList返回的列表会以传入的数组为内部存储
 * 任何对原数组做的修改都会对Arrays.asList的结果生效
 * 然后会导致在解锁时，直接调用locks[0],locks[1]来解锁，有可能这两次操作的都是同一个锁对象，
 * 导致对同一个锁对象执行两次unlock而抛异常
 * 考虑在tryLock等方法中复制数组
 */
public class ILockHelper {

    /**
     * 设置超时时间的上锁操作
     * @param expireMillis
     * @param originLocks
     * @return
     */
    public static boolean tryLock(long expireMillis, Lock... originLocks) {
        Lock[] locks = new Lock[originLocks.length];
        System.arraycopy(originLocks, 0, locks, 0, originLocks.length);
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
     * @param originLocks
     * @return
     */
    public static List<Lock> interruptiblyLock(Lock... originLocks) {
        Lock[] locks = new Lock[originLocks.length];
        System.arraycopy(originLocks, 0, locks, 0, originLocks.length);
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
     * @param originLocks
     * @return
     */
    public static List<Lock> syncLock(Lock... originLocks) {
        Lock[] locks = new Lock[originLocks.length];
        System.arraycopy(originLocks, 0, locks, 0, originLocks.length);
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
