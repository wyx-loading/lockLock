package com.loading.ilock;

import java.util.List;
import java.util.concurrent.locks.Lock;

/**
 * Created by wuyuxiang on 2017/3/9.
 */
public interface ILockImpl {
    /**
     * 对已经排好序的锁，上锁
     * @param sortedLocks
     * @return
     */
    boolean lock(List<Lock> sortedLocks);
}
