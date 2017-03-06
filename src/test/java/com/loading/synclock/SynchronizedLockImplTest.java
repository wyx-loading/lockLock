package com.loading.synclock;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by wuyuxiang on 2017/3/6.
 */
public class SynchronizedLockImplTest {

    private static final int DO_NUM = 1000000;

    @Test
    public void testSyncOne() throws InterruptedException {
        final Object[] lockObject = new Object[0];
        final Counter counter = new Counter();
        ExecutorService es = Executors.newFixedThreadPool(10);
        for(int i = 0; i < DO_NUM; i++) {
            es.submit(new Runnable() {
                @Override
                public void run() {
                    SynchronizedLockImpl.underSync(new Object[] { lockObject }, () -> {
                        counter.incr();
                    });
                }
            });
        }
        es.shutdown();
        es.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        Assert.assertEquals(DO_NUM, counter.getCount());
    }

    @Test
    public void testSyncTwo() throws InterruptedException {
        final Object[] lockObject1 = new Object[0];
        final Object[] lockObject2 = new Object[0];
        final Counter counter = new Counter();
        ExecutorService es = Executors.newFixedThreadPool(10);
        for(int i = 0; i < DO_NUM; i++) {
            Object[] locks = null;
            if((i & 2) == 0) {
                es.submit(new Runnable() {
                    @Override
                    public void run() {
                        SynchronizedLockImpl.underSync(new Object[] { lockObject1, lockObject2 }, () -> {
                            counter.incr();
                        });
                    }
                });
                locks = new Object[] { lockObject1, lockObject2 };
            } else {
                es.submit(new Runnable() {
                    @Override
                    public void run() {
                        SynchronizedLockImpl.underSync(new Object[] { lockObject2, lockObject1 }, () -> {
                            counter.incr();
                        });
                    }
                });
                locks = new Object[] { lockObject1, lockObject2 };
            }

        }
        es.shutdown();
        es.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        Assert.assertEquals(DO_NUM, counter.getCount());
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
