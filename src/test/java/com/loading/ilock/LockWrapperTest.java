package com.loading.ilock;

import com.loading.Counter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by wuyuxiang on 2017/3/9.
 *
 * 测试结果：
 * 测试用例用时 tryLock ~= interruptiblyLock < syncLock
 */
public class LockWrapperTest {
    private static final int LOCK_NUM = 4;
    private static final int TEST_ROUND = 1000000;

    private Lock[] locks;

    @Before
    public void init() {
        locks = new Lock[LOCK_NUM];
        for(int i = 0; i < locks.length; i++) {
            locks[i] = new ReentrantLock();
        }
    }

    @Test
    public void testTryLock() throws InterruptedException {
        final ILockImpl lockImpl = new LockTryLock();
        testTemplate(lockImpl);
    }
    @Test
    public void testInterruptiblyLock() throws InterruptedException {
        final ILockImpl lockImpl = new LockInterruptiblyLock();
        testTemplate(lockImpl);
    }
    @Test
    public void testSyncLock() throws InterruptedException {
        final ILockImpl lockImpl = new LockSyncLock();
        testTemplate(lockImpl);
    }

    public void testTemplate(ILockImpl lockImpl) throws InterruptedException {
        final Counter counter = new Counter();
        final AtomicInteger failCounter = new AtomicInteger();
        ExecutorService es = Executors.newFixedThreadPool(10);
        for(int i = 0; i < TEST_ROUND; i++) {
            if((i & 2) == 0) {
                es.submit(new Runnable() {
                    @Override
                    public void run() {
                        LockWrapper lock = new LockWrapper(lockImpl);
                        lock.add(locks);
                        if(lock.lock()) {
                            try {
                                counter.incr();
                            } catch (Throwable t) {
                                t.printStackTrace();
                                failCounter.incrementAndGet();
                            } finally {
                                lock.unlock();
                            }
                        }
                    }
                });
            } else {
                es.submit(new Runnable() {
                    @Override
                    public void run() {
                        LockWrapper lock = new LockWrapper(lockImpl);
                        lock.add(locks);
                        if(lock.lock()) {
                            try {
                                counter.incr();
                            } catch (Throwable t) {
                                t.printStackTrace();
                                failCounter.incrementAndGet();
                            } finally {
                                lock.unlock();
                            }
                        }
                    }
                });
            }
        }

        es.shutdown();
        es.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);

        Assert.assertEquals(TEST_ROUND, counter.getCount() + failCounter.get());
        System.out.println(String.format("%s sucCount=%d, failCount=%d", lockImpl.getClass().getSimpleName(), counter.getCount(), failCounter.get()));
    }
}
