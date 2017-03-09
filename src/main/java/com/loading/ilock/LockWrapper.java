package com.loading.ilock;

import com.loading.LockSorter;
import com.loading.exception.LockCrossThreadException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;

/**
 * Created by wuyuxiang on 2017/3/9.
 *
 * 保存锁对象，自动对锁列表排序，上锁，解锁
 * 保存获得锁的线程id，通过比对线程id来判断上锁和解锁操作是否合法
 */
public class LockWrapper {
    private final ILockImpl lockImpl;
    protected final List<Lock> locks;
    protected volatile long lockThreadId;

    public LockWrapper(ILockImpl lockImpl) {
        this.lockImpl = lockImpl;
        this.locks = new ArrayList<>();
        this.lockThreadId = 0;
    }

    public boolean lock() {
        if(locks.size() < 1) {
            // TODO warn
            return true;
        }
        long curThreadId = Thread.currentThread().getId();
        if(lockThreadId == curThreadId) {
            return true;
        } else if(lockThreadId != 0) {
            throw new LockCrossThreadException(String.format("lock cross, now: %d, owner: %d", curThreadId, lockThreadId));
        }

        // 锁排序
        LockSorter.sort(this.locks);
        // 上锁
        boolean suc = lockImpl.lock(this.locks);
        if(suc) {
            this.lockThreadId = Thread.currentThread().getId();
        }
        return suc;
    }

    public void unlock() {
        if(lockThreadId == 0) {
            return;
        } else {
            long curThreadId = Thread.currentThread().getId();
            if(lockThreadId != curThreadId) {
                throw new LockCrossThreadException(String.format("unlock cross, now: %d, owner: %d", curThreadId, lockThreadId));
            }
        }
        for(Lock lock : locks) {
            lock.unlock();
        }
        this.lockThreadId = 0;
    }

    public LockWrapper add(Lock lock) {
        if(!this.locks.contains(lock)) {
            this.locks.add(lock);
        }
        return this;
    }

    public LockWrapper add(Lock... locks) {
        for(Lock lock : locks) {
            if(!this.locks.contains(lock)) {
                this.locks.add(lock);
            }
        }
        return this;
    }

    public ILockImpl getLockImpl() {
        return lockImpl;
    }

    public long getLockThreadId() {
        return lockThreadId;
    }
}
