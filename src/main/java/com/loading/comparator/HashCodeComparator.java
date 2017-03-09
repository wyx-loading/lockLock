package com.loading.comparator;

/**
 * Created by wuyuxiang on 2017/3/6.
 */
public class HashCodeComparator implements ILockComparator {
    public int compare(Object o1, Object o2) {
        return Integer.compare(System.identityHashCode(o1), System.identityHashCode(o2));
    }
}
