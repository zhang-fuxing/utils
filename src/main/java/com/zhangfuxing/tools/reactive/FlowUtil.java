package com.zhangfuxing.tools.reactive;

import java.util.concurrent.SubmissionPublisher;
import java.util.function.Consumer;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/5/21
 * @email zhangfuxing1010@163.com
 */
public class FlowUtil {
    @SafeVarargs
    public static <T> SubmissionPublisher<T> buildPublisher(Consumer<T>... onNexts) {
        SubmissionPublisher<T> publisher = new SubmissionPublisher<>();
        for (Consumer<T> onNext : onNexts) {
            publisher.subscribe(new DefaultSubscriberImpl<>(onNext));
        }
        return publisher;
    }


}
