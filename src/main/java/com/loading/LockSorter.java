package com.loading;

import com.loading.comparator.HashCodeComparator;
import com.loading.comparator.ILockComparator;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * Created by wuyuxiang on 2017/3/6.
 */
public class LockSorter {
    private static final ILockComparator COMPARATOR = new HashCodeComparator();

    /**
     * 对传入的列表，在原列表上排序
     * @param objects
     * @param <T>
     */
    public static <T> void sort(List<T> objects) {
        objects.sort(COMPARATOR::compare);
    }

    /**
     * 以Java默认hashCode实现为排序依据，对传入对象排序
     * @param objects
     * @param <T>
     * @return
     */
    public static <T> List<T> sort(T... objects) {
        List<T> objectList = Arrays.asList(objects);
        objectList.sort(COMPARATOR::compare);
        return objectList;
    }

    /**
     * 以Java默认hashCode实现为排序依据，
     * 以getLockObj(object)返回的对象为hashCode对象，
     * 对传入对象进行排序。
     * @param getLockObj
     * @param objects
     * @param <T>
     * @param <R>
     * @return
     */
    public static <T, R> List<T> sort(Function<T, R> getLockObj, T... objects) {
        List<T> objectList = Arrays.asList(objects);
        objectList.sort(new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                return COMPARATOR.compare(getLockObj.apply(o1), getLockObj.apply(o2));
            }
        });
        return objectList;
    }

}
