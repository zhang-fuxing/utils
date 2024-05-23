package com.zhangfuxing.tools.reactive;

import java.util.concurrent.Flow;
import java.util.function.Consumer;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/5/21
 * @email zhangfuxing1010@163.com
 */
public class DefaultSubscriberImpl<T> implements Flow.Subscriber<T> {
    Consumer<T> onNext;
    Consumer<Flow.Subscription> onSubscribe;
    Consumer<Throwable> onError;
    Runnable onComplete;

    public DefaultSubscriberImpl() {
        onNext = p -> {};
        onSubscribe = subscription -> subscription.request(Long.MAX_VALUE);
        onError = p -> {};
        onComplete = () -> {};
    }

    public DefaultSubscriberImpl(Consumer<T> onNext) {
        this();
        this.onNext = onNext;
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.onSubscribe.accept(subscription);
    }

    @Override
    public void onNext(T item) {
        onNext.accept(item);
    }

    @Override
    public void onError(Throwable throwable) {
        onError.accept(throwable);
    }

    @Override
    public void onComplete() {
        onComplete.run();
    }
}
