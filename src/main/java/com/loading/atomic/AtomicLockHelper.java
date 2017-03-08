package com.loading.atomic;

import com.loading.LockSorter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuyuxiang on 2017/3/6.
 */
public class AtomicLockHelper {

    public static void lock(AtomicLock... locks) {
        List<AtomicLock> lockList = LockSorter.sort(locks);
        List<AtomicLock> lockedList = new ArrayList<>(lockList.size());
        for(AtomicLock lock : lockList) {
            if(lock.lock()) {
                lockedList.add(lock);
            } else {
                break;
            }
        }
    }

}
