package com.loading.synclock;

import com.loading.LockSorter;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * Created by wuyuxiang on 2017/3/6.
 */
public class SynchronizedLockImpl {

    public static void underSync(Object lockObject, Runnable jobUnderSync) {
        syncOne(lockObject, jobUnderSync);
    }
    public static void underSync(Object[] lockObjects, Runnable jobUnderSync) {
        List<Object> lockObjectList = LockSorter.sort(lockObjects);
        BiConsumer<List, Runnable> method = null;
        switch(lockObjectList.size()) {
        case 1:
            method = SynchronizedLockImpl::syncOne;
            break;
        case 2:
            method = SynchronizedLockImpl::syncTwo;
            break;
        default:
            throw new UnsupportedOperationException(String.format("Unsupported synchronized %d locks", lockObjectList.size()));
        }
        method.accept(lockObjectList, jobUnderSync);
    }

    private static void syncOne(Object lockObject, Runnable jobUnderSync) {
        synchronized (lockObject) {
            jobUnderSync.run();
        }
    }
    private static void syncOne(List lockObjects, Runnable jobUnderSync) {
        synchronized (lockObjects.get(0)) {
            jobUnderSync.run();
        }
    }
    private static void syncTwo(List lockObjects, Runnable jobUnderSync) {
        synchronized (lockObjects.get(0)) {
            synchronized (lockObjects.get(1)) {
                jobUnderSync.run();
            }
        }
    }
}
