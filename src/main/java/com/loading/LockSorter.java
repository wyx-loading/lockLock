package com.loading;

import java.util.Arrays;
import java.util.List;

/**
 * Created by wuyuxiang on 2017/3/6.
 */
public class LockSorter {

    /**
     * 以默认hashCode为排序依据，对传入对象排序
     * @param lockObjects
     * @return
     */
    public static List<Object> sort(Object... lockObjects) {
        List<Object> lockObjectList = Arrays.asList(lockObjects);
        lockObjectList.sort(LockComparator::compare);
        return lockObjectList;
    }

}
