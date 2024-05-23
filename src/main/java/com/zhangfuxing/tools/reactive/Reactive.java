package com.zhangfuxing.tools.reactive;

import java.util.List;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/5/22
 * @email zhangfuxing1010@163.com
 */
public class Reactive<T> {

    final SubmissionPublisher<T> publisher;

    public Reactive() {
        publisher = new SubmissionPublisher<T>();
    }

    public Reactive(List<Flow.Subscriber<T>> subscriberList) {
        publisher = new SubmissionPublisher<T>();
        for (Flow.Subscriber<T> tSubscriber : subscriberList) {
            publisher.subscribe(tSubscriber);
        }
    }

    public Reactive<T> addSub(Flow.Subscriber<T> sub) {
        publisher.subscribe(sub);
        return this;
    }

    public Reactive<T> submit(T t) {
        publisher.submit(t);
        return this;
    }

    public void close() {
        publisher.close();
    }
}
