package com.loading;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by wuyuxiang on 2017/3/6.
 */
public class LockSorterTest {

    @Test
    public void testSorter() {
        List<Object> objectList = new ArrayList<>(10);
        for(int i = 0; i < 10; i++) {
            objectList.add(new Object[0]);
        }
        List<Object> objectListCopy = new ArrayList<>(objectList);

        disrupt(objectList);
        disrupt(objectListCopy);

        List<Object> sort1 = LockSorter.sort(objectList.toArray(new Object[0]));
        List<Object> sort2 = LockSorter.sort(objectListCopy.toArray(new Object[0]));

        Assert.assertEquals(objectList.size(), sort1.size());
        Assert.assertEquals(objectList.size(), sort2.size());
        for(int i = 0; i < sort1.size(); i++) {
            Assert.assertEquals(sort1.get(i), sort2.get(i));
        }
    }

    private void disrupt(List<Object> list) {
        Random random = new Random();
        for(int i = 0; i < list.size(); i++) {
            int exchangeIndex = random.nextInt(list.size());
            Object from = list.get(i);
            list.set(i, list.get(exchangeIndex));
            list.set(exchangeIndex, from);
        }
    }

}
