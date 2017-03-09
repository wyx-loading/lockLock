package com.loading.ilock;

import com.loading.LockSorter;
import com.loading.exception.LockCrossThreadException;
import com.loading.exception.LockFailException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;

/**
 * Created by wuyuxiang on 2017/3/9.
 */
public class RelockWrapper {
    private final ILockImpl lockImpl;
    protected List<Lock> locked;
    protected List<Lock> waitToLock;
    private volatile long lockThreadId;

    public RelockWrapper(ILockImpl lockImpl) {
        this.lockImpl = lockImpl;
        this.locked = null;
        this.waitToLock = null;
        this.lockThreadId = 0;
    }

    /**
     * 上锁操作
     * 每次调用都会清空两次lock之间add的锁对象
     * 无论上锁是否成功，都可以调用unlock方法。
     * 如果lock()抛出异常，可以不调用unlock方法，锁已被释放才抛出异常。
     * @return
     */
    public boolean lock() {
        long curThreadId = Thread.currentThread().getId();
        if(lockThreadId != 0 && lockThreadId != curThreadId) {
            throw new LockCrossThreadException(String.format("lock cross, now: %d, owner: %d", curThreadId, lockThreadId));
        }
        // 没有需要获取的锁资源，直接返回成功
        if(waitToLock == null || waitToLock.size() < 1) {
            return true;
        }
        if(lockThreadId == 0) {
            // 没有已获得的锁资源
            // 对waitToLock上锁，上锁成功则将waitToLock赋给locked，再置空
            LockSorter.sort(waitToLock);
            boolean suc = lockImpl.lock(waitToLock);
            if(suc) {
                this.lockThreadId = Thread.currentThread().getId();
                this.locked = waitToLock;
                removeWaitToLock();
            }
            return suc;
        } else {
            // 有已获得的锁资源
            List<Lock> allLocks = new ArrayList<>(locked.size() + waitToLock.size());
            allLocks.addAll(locked);
            allLocks.addAll(waitToLock);
            LockSorter.sort(allLocks);

            // 计算加上新锁后的排序结果，可以保留的锁的下标
            int stay = -1;
            for(int i = 0; i < locked.size(); i++) {
                if(locked.get(i) != allLocks.get(i)) {
                    break;
                }
                stay = i;
            }
            // 释放顺序外的锁
            for(int i = stay + 1; i < locked.size(); i++) {
                locked.get(i).unlock();
            }
            // 对剩余的锁上锁
            List<Lock> needLockList = allLocks.subList(stay+1, allLocks.size());
            boolean suc = false;
            try {
                suc = lockImpl.lock(needLockList);
                if(suc) {
                    // 成功则更新locked，清空waitToLock
                    this.locked = allLocks;
                    removeWaitToLock();
                }
                return suc;
            } catch(Throwable t) {
                // 重新上锁失败
                // 此时的处理，选择释放所有锁。因为想恢复到原来的状态，需要对之前释放的部分或全部锁重新上锁，不能保证成功。
                for(int i = 0; i <= stay; i++) {
                    locked.get(i).unlock();
                }
                this.locked = null;
                removeWaitToLock();
                this.lockThreadId = 0;
                throw new LockFailException("relock fail, error", t);
            }
        }
    }

    /**
     * 释放当前已获取的锁资源
     * 会清空所有保存的锁对象
     */
    public void unlock() {
        long curThreadId = Thread.currentThread().getId();
        if(lockThreadId != 0 && lockThreadId != curThreadId) {
            throw new LockCrossThreadException(String.format("unlock cross, now: %d, owner: %d", curThreadId, lockThreadId));
        }
        removeWaitToLock();
        if(lockThreadId == 0) {
            return;
        }
        if(locked != null) {
            for(Lock lock : locked) {
                lock.unlock();
            }
            locked = null;
        }
        this.lockThreadId = 0;
    }

    private void removeWaitToLock() {
        if(waitToLock != null) {
            waitToLock = null;
        }
    }

    public RelockWrapper add(Lock lock) {
        long curThreadId = Thread.currentThread().getId();
        if(lockThreadId != 0 && lockThreadId != curThreadId) {
            throw new LockCrossThreadException(String.format("add to locked, now: %d, owner: %d", curThreadId, lockThreadId));
        }
        if(waitToLock == null) {
            waitToLock = new ArrayList<>();
        }
        if(!waitToLock.contains(lock)) {
            waitToLock.add(lock);
        }
        return this;
    }

    public RelockWrapper add(Lock... locks) {
        long curThreadId = Thread.currentThread().getId();
        if(lockThreadId != 0 && lockThreadId != curThreadId) {
            throw new LockCrossThreadException(String.format("add to locked, now: %d, owner: %d", curThreadId, lockThreadId));
        }
        if(waitToLock == null) {
            waitToLock = new ArrayList<>();
        }
        for(Lock lock : locks) {
            if(!waitToLock.contains(lock)) {
                waitToLock.add(lock);
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
