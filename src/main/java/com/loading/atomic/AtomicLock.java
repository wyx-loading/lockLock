package com.loading.atomic;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by wuyuxiang on 2017/3/6.
 */
public class AtomicLock {
    private final AtomicInteger lock;

    public AtomicLock() {
        this.lock = new AtomicInteger();
    }

    public boolean lock() {
        return lock.compareAndSet(0, 1);
    }

    public boolean unlock() {
        return lock.compareAndSet(1, 0);
    }
}
