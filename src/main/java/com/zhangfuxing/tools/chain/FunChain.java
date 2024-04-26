package com.zhangfuxing.tools.chain;


import com.zhangfuxing.tools.common.exception.FlowChainException;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2023/12/27
 * @email zhangfuxing@kingshine.com.cn
 */
public class FunChain<T> {
    private final T data;
    private final Throwable throwable;

    public static <T> FunChain<T> create(T data) {
        return new FunChain<T>(data);
    }

    /**
     * 生成一个FunChain对象，并将fun函数与该对象关联起来
     *
     * @param fun 要关联的函数
     * @param <T> FunChain对象中函数的输入参数类型
     * @param <R> FunChain对象中函数的返回值类型
     * @return 关联了fun函数的FunChain对象
     */
    public static <T, R> FunChain<R> doChain(Function<T, R> fun) {
        return new FunChain<T>().then(fun);
    }

    /**
     * 生成一个FunChain对象，并将其初始化为给定的Supplier对象
     *
     * @param fun 用于执行操作的Supplier对象
     * @param <T> 泛型类型参数
     * @return 初始化后的FunChain对象
     */
    public static <T> FunChain<T> doChain(Supplier<T> fun) {
        return new FunChain<T>().start(fun);
    }

    private FunChain() {
        data = null;
        throwable = null;
    }

    public FunChain(T t) {
        data = t;
        this.throwable = null;
    }

    public FunChain(Throwable throwable) {
        this.data = null;
        this.throwable = throwable;
    }

    private <R> FunChain<R> start(Function<T, R> fun) {
        FunChain<R> chain;
        try {
            chain = new FunChain<>(fun.apply(this.data));
        } catch (Exception e) {
            return new FunChain<>(e);
        }
        return chain;
    }

    private FunChain<T> start(Supplier<T> fun) {
        FunChain<T> chain;
        try {
            chain = new FunChain<>(fun.get());
        } catch (Exception e) {
            return new FunChain<>(e);
        }
        return chain;
    }

    /**
     * 对当前FunChain对象进行then操作，然后返回一个新的FunChain对象。
     *
     * @param fun 一个接受当前FunChain对象的数据作为参数的Function函数
     * @param <R> Function函数的返回值类型
     * @return 新的FunChain对象
     */
    public <R> FunChain<R> then(Function<T, R> fun) {
        if (throwable != null) {
            return new FunChain<>(throwable);
        }
        FunChain<R> chain;
        try {
            chain = new FunChain<>(fun.apply(data));
        } catch (Exception e) {
            return new FunChain<>(e);
        }
        return chain;
    }

    /**
     * 返回一个包含单个元素的流。
     *
     * @return 包含单个元素的流
     */
    public Stream<T> stream() {
        return Stream.of(data);
    }

    public FunChain<T> peek(Consumer<T> consumer) {
        if (throwable != null) {
            return new FunChain<>(throwable);
        }
        try {
            consumer.accept(data);
        } catch (Exception e) {
            return new FunChain<>(e);
        }
        return this;
    }

    public <R> FunChain<R> importChainOrg(FunChain<T> chain, BiFunction<FunChain<T>, FunChain<T>, R> then) {
        if (chain.throwable != null) {
            return new FunChain<>(chain.throwable);
        }
        return new FunChain<>(then.apply(chain, this));
    }

    public <R> FunChain<R> importChain(FunChain<T> chain, BiFunction<T, T, R> then) {
        if (chain.throwable != null) {
            return new FunChain<>(chain.throwable);
        }
        return new FunChain<>(then.apply(chain.data, this.data));
    }

    public FunChain<T> getChain() {
        if (throwable != null) {
            throw new FlowChainException(throwable);
        }
        return this;
    }

    public <X extends Throwable> FunChain<T> getChain(Supplier<X> supplier) throws X {
        onError(supplier);
        return this;
    }

    public <X extends Throwable> FunChain<T> getChain(Function<Throwable, X> function) throws X {
        throwError(function);
        return this;
    }

    public <X extends Throwable> T get() throws X {
        return get(() -> new FlowChainException(throwable));
    }

    public <X extends Throwable> T get(Supplier<X> supplier) throws X {
        onError(supplier);
        return data;
    }

    public <X extends Throwable> T get(Function<Throwable, X> function) throws X {
        throwError(function);
        return data;
    }

    public <X extends Throwable> T getOrError(Consumer<Throwable> consumer) {
        if (throwable != null) {
            consumer.accept(throwable);
            return null;
        }
        return data;
    }

    public void onError(Consumer<Throwable> consumer) {
        if (this.throwable != null) {
            consumer.accept(throwable);
        }
    }

    public <X extends Throwable> void onError(Supplier<X> supplier) throws X {
        if (this.throwable != null) {
            throw supplier.get();
        }
    }

    public <X extends Throwable> void throwError(Function<Throwable, X> function) throws X {
        if (this.throwable != null) {
            throw function.apply(throwable);
        }
    }

    public FunChain<T> join(Consumer<T> fun) {
        fun.accept(data);
        return new FunChain<>();
    }


    public T getData() {
        return data;
    }
}
