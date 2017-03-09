# 锁列

TODO

## Todo List

- [x] 实现简单的排序
- [x] 支持synchronized的锁列
- [x] 支持Lock类的锁列
- [x] 保存已获得锁的锁对象用于上下文
- [x] 再获取新的锁情况，放弃原有锁，并按照顺序重新上锁
- [ ] 在Future.get()情况下导致死锁的解决方案

## 锁列的由来

### 死锁的四个必要条件
1. 互斥条件：资源不能被共享，只能由一个进程使用
1. 请求与保持条件：已经得到资源的进程可以再次申请新的资源
1. 非剥夺条件：已经分配的资源不能从相应的进程中被强制地剥夺。
1. **循环等待条件：系统中若干进程组成环路，该环路中每个进程都在等待相邻进程正占用的资源。**

### 处理死锁的策略
1. 忽略该问题。例如鸵鸟算法，该算法可以应用在极少发生死锁的情况下。
1. 检测死锁并且恢复。
1. 仔细地对资源进行动态分配，以避免死锁。
1. **通过破除死锁四个必要条件之一，来防止死锁产生。**
    - **可以规定该资源获得顺序，如果请求的资源顺序与当前请求资源顺序不符合，则把当前持有的资源释放，再重新得到资源。**
    - 尝试定时的锁，使用显示Lock类中定时tryLock特性，来替代使用内部锁机制。
    - 在程序中尽量使用开放调用。依赖于开放调用的程序，相比于那些在持有锁的时候还调用外部方法的程序，更容易进行死锁自由度的分析。重新构建synchronized使开放调用更加安全。所谓开放调用是指调用的方法本身没有加锁，但是要以对方法操作的内容进程加锁。一句话，synchronized尽量不要锁定整个函数，而是把想锁的内容给锁了。
    - 可抢占锁，也就是能够响应中断。

### 分析和想法
鸵鸟算法无法解决问题。仔细地对资源进行动态分配不能从根本上避免死锁。而检测死锁并且恢复没有什么好的想法，不好实现。

这里就针对破除死锁四个必要条件之一的策略来说一下

1. **就是锁列。将所有锁都按照固定顺序上锁，这样无论如何都不会出现循环等待的问题。即把死锁的必要条件：循环等待条件给破除了。**
1. 和synchronized和Lock.lock()不同，给上锁加上一个超时时间，这样即使出现循环等待，也会在超时时间后解开。这样虽然能避免死锁，但是还是避免不了循环等待出现时导致的业务可能被延误的问题。
1. 开放调用。其实核心思想是加锁范围最小化。[样例，稍微理解一下](http://yangbolin.cn/2014/10/25/open-call-to-avoid-deadlock/)
1. 可抢占锁，即可以响应interrupt的上锁操作，Lock.interruptiblyLock()就是可抢占锁。这样在死锁出现的时候可以中断该循环里的某条线程，即可解开这个环。

### 问题汇总
Q: 在已获得锁的情况下，修改了数据，再请求新的锁，若请求的锁顺序与当前获得的锁顺序不符合，则需要放弃当前获得锁，再重新获得锁。**放弃所有锁会再和其他线程竞争，可能被其他线程先抢占，但是已经修改了数据，此时该数据是否还合法？**

A: **在已获得锁的情况下，修改了数据，说明此时该数据的修改是合法的，无论之后是继续请求新的锁，还是释放锁，该数据都是合法的才对。**若需要等到获取完所有锁，更改才合法，那么就在获取全锁之后才执行数据的更改即可。

Q: A线程上锁，然后给ExecutorService提交任务，并调用Future.get()等待结果返回。任务里面也会上锁，此时若任务依赖的锁和A线程已获得的锁有重复，则产生死锁。此时应该怎么解决？

A: **TODO**

## 实现

### 锁排序

```java
    /**
     * Returns a hash code value for the object. This method is
     * supported for the benefit of hash tables such as those provided by
     * {@link java.util.HashMap}.
     * <p>
     * The general contract of {@code hashCode} is:
     * <ul>
     * <li>Whenever it is invoked on the same object more than once during
     *     an execution of a Java application, the {@code hashCode} method
     *     must consistently return the same integer, provided no information
     *     used in {@code equals} comparisons on the object is modified.
     *     This integer need not remain consistent from one execution of an
     *     application to another execution of the same application.
     * <li>If two objects are equal according to the {@code equals(Object)}
     *     method, then calling the {@code hashCode} method on each of
     *     the two objects must produce the same integer result.
     * <li>It is <em>not</em> required that if two objects are unequal
     *     according to the {@link java.lang.Object#equals(java.lang.Object)}
     *     method, then calling the {@code hashCode} method on each of the
     *     two objects must produce distinct integer results.  However, the
     *     programmer should be aware that producing distinct integer results
     *     for unequal objects may improve the performance of hash tables.
     * </ul>
     * <p>
     * As much as is reasonably practical, the hashCode method defined by
     * class {@code Object} does return distinct integers for distinct
     * objects. (This is typically implemented by converting the internal
     * address of the object into an integer, but this implementation
     * technique is not required by the
     * Java&trade; programming language.)
     *
     * @return  a hash code value for this object.
     * @see     java.lang.Object#equals(java.lang.Object)
     * @see     java.lang.System#identityHashCode
     */
    public native int hashCode();
```
```java
    /**
     * Returns the same hash code for the given object as
     * would be returned by the default method hashCode(),
     * whether or not the given object's class overrides
     * hashCode().
     * The hash code for the null reference is zero.
     *
     * @param x object for which the hashCode is to be calculated
     * @return  the hashCode
     * @since   JDK1.1
     */
    public static native int identityHashCode(Object x);
```


由Object.hashCode()的javadoc可知，不同对象的默认hashCode实现会返回不同的值（由内部内存地址转换而来）

**而锁排序实现中，想法是传入的对象没有规定继承什么类，或者实现什么接口，所以使用默认的hashCode值作为排序依据想来是最好的。**通过System.identityHashCode(Object)来获取默认hashCode。

### 批量锁封装

#### 支持单一上锁方式的封装
`com.loading.ilock.ILockImpl`批量锁的接口，通过实现该接口可以自定义选择上锁方式，现在已有的实现有：

- tryLock
- interruptiblyLock
- syncLock

`com.loading.ilock.LockWrapper`类包装了批量锁的上锁解锁操作。具体用法见`com.loading.ilock.LockWrapperTest`。

#### 多次上锁
`com.loading.ilock.RelockWrapper`类实现了多次上锁的功能，包括了`com.loading.ilock.LockWrapper`的所有功能。具体用法见`com.loading.ilock.RelockWrapperTest`。