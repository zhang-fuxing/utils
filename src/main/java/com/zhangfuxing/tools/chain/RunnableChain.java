package com.zhangfuxing.tools.chain;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2023/12/29
 * @email zhangfuxing@kingshine.com.cn
 */
public class RunnableChain {
    private final Throwable throwable;

    private RunnableChain() {
        this.throwable = null;
    }

    private RunnableChain(Throwable throwable) {
        this.throwable = throwable;
    }

    /**
     * 创建一个RunnableChain对象
     *
     * @return 返回一个新的RunnableChain对象
     */
    public static RunnableChain create() {
        return new RunnableChain();
    }

    /**
     * 依次执行当前的RunnableChain，并且可以选择是否忽略错误
     *
     * @param runnable    需要执行的Runnable
     * @param ignoreError 是否忽略错误
     * @return 返回新的RunnableChain实例
     */
    public RunnableChain then(Runnable runnable, boolean ignoreError) {
        if (throwable != null && !ignoreError) {
            return new RunnableChain(throwable);
        }
        try {
            runnable.run();
        } catch (Exception e) {
            return new RunnableChain(e);
        }
        return this;
    }

    /**
     * 连接当前RunnableChain对象并执行指定的Runnable任务。
     *
     * @param runnable 要执行的Runnable任务
     * @return 返回一个新的RunnableChain对象
     */
    public RunnableChain then(Runnable runnable) {
        return then(runnable, false);
    }

    /**
     * 依次执行当前的RunnableChain对象和指定的Runnable对象，并在当前的RunnableChain对象完成后，
     * 如果在执行指定的Runnable对象时抛出了异常，则将该异常传递给指定的Consumer对象进行处理。
     * 如果当前的RunnableChain对象已经抛出了异常且指定了Consumer对象，则将该异常传递给指定的Consumer对象进行处理。
     * 最后返回一个新的RunnableChain对象。
     *
     * @param runnable 需要执行的Runnable对象
     * @param consumer 异常处理的Consumer对象
     * @return 新的RunnableChain对象
     */
    public RunnableChain then(Runnable runnable, Consumer<Throwable> consumer) {
        if (throwable != null && consumer != null) {
            consumer.accept(throwable);
        }
        try {
            runnable.run();
        } catch (Exception e) {
            return new RunnableChain(e);
        }
        return this;
    }

    /**
     * 连接当前RunnableChain对象并执行指定的Runnable任务。
     *
     * @param runnable 要执行的Runnable任务
     * @return 返回一个新的RunnableChain对象
     */
    public RunnableChain next(Runnable runnable) {
        return next(runnable, false);
    }

    /**
     * 依次执行当前的RunnableChain，并且可以选择是否忽略错误
     *
     * @param runnable    需要执行的Runnable
     * @param ignoreError 是否忽略错误
     * @return 返回新的RunnableChain实例
     */
    public RunnableChain next(Runnable runnable, boolean ignoreError) {
        return then(runnable, ignoreError);
    }

    /**
     * 依次执行当前的RunnableChain对象和指定的Runnable对象，并在当前的RunnableChain对象完成后，
     * 如果在执行指定的Runnable对象时抛出了异常，则将该异常传递给指定的Consumer对象进行处理。
     * 如果当前的RunnableChain对象已经抛出了异常且指定了Consumer对象，则将该异常传递给指定的Consumer对象进行处理。
     * 最后返回一个新的RunnableChain对象。
     *
     * @param runnable 需要执行的Runnable对象
     * @param consumer 异常处理的Consumer对象
     * @return 新的RunnableChain对象
     */
    public RunnableChain next(Runnable runnable, Consumer<Throwable> consumer) {
        return then(runnable, consumer);
    }

    /**
     * 处理错误的函数
     *
     * @param consumer 错误处理函数
     * @return 返回 RunnableChain 对象
     */
    public RunnableChain error(Consumer<Throwable> consumer) {
        if (throwable != null) {
            consumer.accept(throwable);
        }
        return this;
    }

    /**
     * 根据给定的函数创建一个新的异常，并将其抛出。
     *
     * @param supplier 用于获取新的异常的函数
     * @throws X 转换后的异常
     */
    public <X extends Throwable> void error(Supplier<X> supplier) throws X {
        if (throwable != null) {
            throw supplier.get();
        }
    }

    /**
     * 根据给定的函数创建一个新的异常，并将其抛出。
     *
     * @param function 用于转换Throwable为特定类型的异常的函数
     * @throws X 转换后的异常
     */
    public <X extends Throwable> void newError(Function<Throwable, X> function) throws X {
        if (throwable != null) {
            throw function.apply(throwable);
        }
    }
}
