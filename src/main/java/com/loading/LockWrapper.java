package com.loading;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

/**
 * Created by wuyuxiang on 2017/3/6.
 */
public class LockWrapper {

    private Long expireMillis;
    private Map<LockType, List<Object>> locks;

    public LockWrapper() {
    }

    public LockWrapper(Long expireMillis) {
        this.expireMillis = expireMillis;
    }

    public void doLock() {
        // TODO
        /*
         * 确定不同锁类型的上锁顺序
         * 比如：
         * 1. 先上QuickFailLock，如atomic
         * 2. 再上tryLock
         * 3. 再上syncLock和synchronized
         *
         * 2/3顺序可以按照需求交换
         */
        List<Object> atomicLocks = getLockList(LockType.atomic);
        if(atomicLocks != null && atomicLocks.size() > 0) {

        }
    }

    public LockWrapper add(LockType lockType, Object... lockObjects) {
        switch (lockType) {
        case ilock_interruptiblyLock:
        case ilock_syncLock:
        case ilock_tryLock:
            addILock(lockType, lockObjects);
            break;
        case atomic:
        case synchronize:
            addObjects(lockType, lockObjects);
            break;
        default:
            throw new IllegalArgumentException("Unknown LockType: " + lockType.name());
        }
        return this;
    }

    private void addILock(LockType lockType, Object... lockObjects) {
        for(Object obj : lockObjects) {
            if(!(obj instanceof Lock)) {
                throw new IllegalArgumentException("LockType: " + lockType.name() + " lockObject must implements Lock");
            }
        }
        List<Object> ilockList = getLockList_ExpectExist(lockType);
        for(Object obj : lockObjects) {
            ilockList.add(obj);
        }
    }

    private void addObjects(LockType lockType, Object... lockObjects) {
        List<Object> lockObjectList = getLockList_ExpectExist(lockType);
        for(Object obj : lockObjects) {
            if(lockType == LockType.synchronize) {
                if(obj instanceof Lock) {
                    // TODO
                    // warning
                }
            } else if(lockType == LockType.atomic) {
                // TODO
            }
            lockObjectList.add(obj);
        }
    }

    public LockWrapper setExpireMillis(Long expireMillis) {
        this.expireMillis = expireMillis;
        return this;
    }

    private List<Object> getLockList(LockType lockType) {
        return locks.get(lockType);
    }

    private List<Object> getLockList_ExpectExist(LockType lockType) {
        List<Object> list = locks.get(lockType);
        if(list == null) {
            list = new ArrayList<>();
            locks.put(lockType, list);
        }
        return list;
    }

}
