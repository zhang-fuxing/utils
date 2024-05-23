package com.zhangfuxing.tools.reactive;

import java.util.Optional;
import java.util.concurrent.Flow;
import java.util.function.Consumer;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/5/22
 * @email zhangfuxing1010@163.com
 */
public class SubscriberBuilder<T> {
    Consumer<T> onNext;
    Consumer<Flow.Subscription> onSubscribe;
    Consumer<Throwable> onError;
    Runnable onComplete;

    public static <T> SubscriberBuilder<T> newInstance() {
        return new SubscriberBuilder<T>();
    }

    public SubscriberBuilder<T> onNext(Consumer<T> onNext) {
        this.onNext = onNext;
        return this;
    }

    public SubscriberBuilder<T> onSubscribe(Consumer<Flow.Subscription> onSubscribe) {
        this.onSubscribe = onSubscribe;
        return this;
    }

    public SubscriberBuilder<T> onError(Consumer<Throwable> onError) {
        this.onError = onError;
        return this;
    }

    public SubscriberBuilder<T> onComplete(Runnable onComplete) {
        this.onComplete = onComplete;
        return this;
    }

    public Flow.Subscriber<T> build() {
        DefaultSubscriberImpl<T> subscriber = new DefaultSubscriberImpl<>();
        Optional.ofNullable(onSubscribe)
                .ifPresent(s -> subscriber.onSubscribe=s);
        Optional.ofNullable(onNext)
                .ifPresent(next -> subscriber.onNext=next);
        Optional.ofNullable(onError)
                .ifPresent(error -> subscriber.onError=error);
        Optional.ofNullable(onComplete)
                .ifPresent(complete -> subscriber.onComplete=complete);
        return subscriber;
    }


}
