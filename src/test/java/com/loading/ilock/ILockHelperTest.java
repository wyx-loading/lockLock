package com.loading.ilock;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by asus1 on 2017-3-8.
 */
public class ILockHelperTest {

    private static final int TEST_NUM = 1000000;
    private static final long EXPIRE_LOCK_MILLIS = 3000;

    @Test
    public void testTryLock() throws InterruptedException {
        Lock[] locks = new Lock[2];
        locks[0] = new ReentrantLock();
        locks[1] = new ReentrantLock();

        final Counter counter = new Counter();
        final Counter failCounter = new Counter();

        ExecutorService es = Executors.newFixedThreadPool(10);
        for(int i = 0; i < TEST_NUM; i++) {
            if((i & 2) == 0) {
                es.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if(ILockHelper.tryLock(EXPIRE_LOCK_MILLIS, new Lock[] { locks[0], locks[1] })) {
                                try {
                                    counter.incr();
                                } finally {
                                    locks[0].unlock();
                                    locks[1].unlock();
                                }
                            } else {
                                failCounter.incr();
                            }
                        } catch (Throwable t) {
                            t.printStackTrace();
                            failCounter.incr();
                        }
                    }
                });
            } else {
                es.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if(ILockHelper.tryLock(EXPIRE_LOCK_MILLIS, new Lock[] { locks[1], locks[0] })) {
                                try {
                                    counter.incr();
                                } finally {
                                    locks[0].unlock();
                                    locks[1].unlock();
                                }
                            } else {
                                failCounter.incr();
                            }
                        } catch (Throwable t) {
                            t.printStackTrace();
                            failCounter.incr();
                        }
                    }
                });
            }
        }

        es.shutdown();
        es.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);

        Assert.assertEquals(TEST_NUM, counter.getCount() + failCounter.getCount());
        System.out.println(String.format("tryLock sucCount=%d, failCount=%d", counter.getCount(), failCounter.getCount()));
    }

    @Test
    public void testInterruptiblyLock() throws InterruptedException {
        Lock[] locks = new Lock[2];
        locks[0] = new ReentrantLock();
        locks[1] = new ReentrantLock();

        final Counter counter = new Counter();
        final Counter failCounter = new Counter();

        ExecutorService es = Executors.newFixedThreadPool(10);
        for(int i = 0; i < TEST_NUM; i++) {
            if((i & 2) == 0) {
                es.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ILockHelper.interruptiblyLock(new Lock[] { locks[0], locks[1] });
                            counter.incr();
                            locks[0].unlock();
                            locks[1].unlock();
                        } catch (Throwable t) {
                            t.printStackTrace();
                            failCounter.incr();
                        }
                    }
                });
            } else {
                es.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ILockHelper.interruptiblyLock(new Lock[] { locks[1], locks[0] });
                            counter.incr();
                            locks[0].unlock();
                            locks[1].unlock();
                        } catch (Throwable t) {
                            t.printStackTrace();
                            failCounter.incr();
                        }
                    }
                });
            }
        }

        es.shutdown();
        es.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);

        Assert.assertEquals(TEST_NUM, counter.getCount() + failCounter.getCount());
        System.out.println(String.format("interruptiblyLock sucCount=%d, failCount=%d", counter.getCount(), failCounter.getCount()));
    }

    @Test
    public void testSyncLock() throws InterruptedException {
        Lock[] locks = new Lock[2];
        locks[0] = new ReentrantLock();
        locks[1] = new ReentrantLock();

        final Counter counter = new Counter();
        final Counter failCounter = new Counter();

        ExecutorService es = Executors.newFixedThreadPool(10);
        for(int i = 0; i < TEST_NUM; i++) {
            if((i & 2) == 0) {
                es.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ILockHelper.syncLock(new Lock[] { locks[0], locks[1] });
                            counter.incr();
                            locks[0].unlock();
                            locks[1].unlock();
                        } catch (Throwable t) {
                            t.printStackTrace();
                            failCounter.incr();
                        }
                    }
                });
            } else {
                es.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ILockHelper.syncLock(new Lock[] { locks[1], locks[0] });
                            counter.incr();
                            locks[0].unlock();
                            locks[1].unlock();
                        } catch (Throwable t) {
                            t.printStackTrace();
                            failCounter.incr();
                        }
                    }
                });
            }
        }

        es.shutdown();
        es.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);

        Assert.assertEquals(TEST_NUM, counter.getCount() + failCounter.getCount());
        System.out.println(String.format("syncLock sucCount=%d, failCount=%d", counter.getCount(), failCounter.getCount()));
    }

    private static class Counter {
        int count;
        int incr() {
            return ++count;
        }
        int getCount() {
            return count;
        }
    }

}
