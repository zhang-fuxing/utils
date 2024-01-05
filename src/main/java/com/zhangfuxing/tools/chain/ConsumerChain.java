package com.zhangfuxing.tools.chain;


import com.zhangfuxing.tools.common.exception.FlowChainException;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2023/12/28
 * @email zhangfuxing@kingshine.com.cn
 */
public class ConsumerChain<T> {
    private final T data;
    private final Throwable throwable;

    private ConsumerChain() {
        data = null;
        throwable = null;
    }

    private ConsumerChain(Throwable throwable) {
        this.data = null;
        this.throwable = throwable;
    }

    private ConsumerChain(T data) {
        this.data = data;
        this.throwable = null;
    }

    public static <T> ConsumerChain<T> create(T t) {
        return new ConsumerChain<T>(t);
    }


    public ConsumerChain<T> then(Consumer<T> consumer) {
        if (throwable != null) {
            return this;
        }
        consumer.accept(this.data);
        return this;
    }

    public <R> ConsumerChain<R> map(Function<T, R> function) {
        if (throwable != null) {
            return new ConsumerChain<>(throwable);
        }
        try {
            return new ConsumerChain<>(function.apply(data));
        } catch (Exception e) {
            return new ConsumerChain<>(e);
        }
    }

    public Stream<T> stream() {
        return Stream.of(data);
    }

    public ConsumerChain<T> peek(Consumer<T> consumer) {
        if (throwable != null) {
            return new ConsumerChain<>(throwable);
        }
        try {
            consumer.accept(data);
        } catch (Exception e) {
            return new ConsumerChain<>(e);
        }
        return this;
    }

    public ConsumerChain<T> getChain() {
        if (throwable != null) {
            throw new FlowChainException(throwable);
        }
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

}
