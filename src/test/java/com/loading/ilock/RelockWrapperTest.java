package com.loading.ilock;

import com.loading.Counter;
import com.loading.LockSorter;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by wuyuxiang on 2017/3/9.
 */
public class RelockWrapperTest {
    private static final int TEST_ROUND = 100000;

    @Test
    public void normalTest() {
        Lock[] locks = newLocks(10);
        Lock[] otherLocks = newLocks(5);

        int i = 0;
        RelockWrapper wrapper = new RelockWrapper(new LockInterruptiblyLock());
        wrapper.add(locks);
        boolean suc = wrapper.lock();
        if(suc) {
            // do something

            wrapper.add(otherLocks);
            if(wrapper.lock()) {
                // do something
            }
        }

        wrapper.unlock();
    }

    @Test
    public void multiNormalTest() throws InterruptedException {
        Lock[] locks = newLocks(5);
        Lock[] otherLocks = newLocks(5);

        final ILockImpl lockImpl = new LockInterruptiblyLock();
        final Counter counter = new Counter();
        final AtomicInteger failCounter = new AtomicInteger();
        ExecutorService es = Executors.newFixedThreadPool(10);
        for(int i = 0; i < TEST_ROUND; i++) {
            if((i & 2) == 0) {
                es.submit(new Runnable() {
                    @Override
                    public void run() {
                        RelockWrapper wrapper = new RelockWrapper(lockImpl);
                        wrapper.add(locks);
                        try {
                            if(wrapper.lock()) {
                                wrapper.add(otherLocks);
                                if(wrapper.lock()) {
                                    counter.incr();
                                    return;
                                }
                            }
                            failCounter.incrementAndGet();
                        } catch(Throwable t) {
                            t.printStackTrace();
                            failCounter.incrementAndGet();
                        } finally {
                            wrapper.unlock();
                        }
                    }
                });
            } else {
                es.submit(new Runnable() {
                    @Override
                    public void run() {
                        RelockWrapper wrapper = new RelockWrapper(lockImpl);
                        wrapper.add(otherLocks);
                        try {
                            if(wrapper.lock()) {
                                wrapper.add(locks);
                                if(wrapper.lock()) {
                                    counter.incr();
                                    return;
                                }
                            }
                            failCounter.incrementAndGet();
                        } catch(Throwable t) {
                            t.printStackTrace();
                            failCounter.incrementAndGet();
                        } finally {
                            wrapper.unlock();
                        }
                    }
                });
            }
        }

        es.shutdown();
        es.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);

        Assert.assertEquals(TEST_ROUND, counter.getCount() + failCounter.get());
        System.out.println(String.format("multiNormalTest sucCount=%d, failCount=%d", counter.getCount(), failCounter.get()));
    }

    @Test
    public void testBestAdd() throws InterruptedException {
        Lock[] allLocks = newLocks(15);
        List<Lock> sortedLocks = LockSorter.sort(allLocks);
        final Lock[] locks = new Lock[10];
        final Lock[] otherLocks = new Lock[5];

        for(int i = 0; i < locks.length; i++) {
            locks[i] = sortedLocks.get(i);
        }
        for(int i = 0; i < otherLocks.length; i++) {
            otherLocks[i] = sortedLocks.get(locks.length + i);
        }

        final Counter counter = new Counter();
        final AtomicInteger failCounter = new AtomicInteger();
        ExecutorService es = Executors.newFixedThreadPool(10);
        for(int i = 0; i < TEST_ROUND; i++) {
            es.submit(new Runnable() {
                @Override
                public void run() {
                    RelockWrapper wrapper = new RelockWrapper(new LockInterruptiblyLock());
                    wrapper.add(locks);
                    try {
                        if (wrapper.lock()) {
                            wrapper.add(otherLocks);
                            if (wrapper.lock()) {
                                counter.incr();
                                return;
                            }
                        }
                        failCounter.incrementAndGet();
                    } catch (Throwable t) {
                        t.printStackTrace();
                        failCounter.incrementAndGet();
                    } finally {
                        wrapper.unlock();
                    }
                }
            });
        }

        es.shutdown();
        es.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);

        Assert.assertEquals(TEST_ROUND, counter.getCount() + failCounter.get());
        System.out.println(String.format("testBestAdd sucCount=%d, failCount=%d", counter.getCount(), failCounter.get()));
    }

    @Test
    public void testWorstAdd() throws InterruptedException {
        Lock[] allLocks = newLocks(15);
        List<Lock> sortedLocks = LockSorter.sort(allLocks);
        final Lock[] locks = new Lock[10];
        Lock[] otherLocks = new Lock[5];

        for(int i = 0; i < locks.length; i++) {
            locks[i] = sortedLocks.get(i);
        }
        for(int i = 0; i < otherLocks.length; i++) {
            otherLocks[i] = sortedLocks.get(locks.length + i);
        }

        final Counter counter = new Counter();
        final AtomicInteger failCounter = new AtomicInteger();
        ExecutorService es = Executors.newFixedThreadPool(10);
        for(int i = 0; i < TEST_ROUND; i++) {
            es.submit(new Runnable() {
                @Override
                public void run() {
                    RelockWrapper wrapper = new RelockWrapper(new LockInterruptiblyLock());
                    wrapper.add(otherLocks);
                    try {
                        if (wrapper.lock()) {
                            wrapper.add(locks);
                            if (wrapper.lock()) {
                                counter.incr();
                                return;
                            }
                        }
                        failCounter.incrementAndGet();
                    } catch (Throwable t) {
                        t.printStackTrace();
                        failCounter.incrementAndGet();
                    } finally {
                        wrapper.unlock();
                    }
                }
            });
        }

        es.shutdown();
        es.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);

        Assert.assertEquals(TEST_ROUND, counter.getCount() + failCounter.get());
        System.out.println(String.format("testWorstAdd sucCount=%d, failCount=%d", counter.getCount(), failCounter.get()));
    }

    @Test
    public void testFailLockAdd() {
        // TODO
    }

    private static Lock[] newLocks(int num) {
        Lock[] retVal = new Lock[num];
        for(int i = 0; i < num; i++) {
            retVal[i] = new ReentrantLock();
        }
        return retVal;
    }

}
