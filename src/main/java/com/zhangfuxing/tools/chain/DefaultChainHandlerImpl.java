package com.zhangfuxing.tools.chain;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/5/9
 * @email zhangfuxing1010@163.com
 */
public class DefaultChainHandlerImpl<T,R> implements ChainHandler<T,R> {
    int order;
    Predicate<T> matcher;
    Function<T, R> handler;
    private  DefaultChainHandlerImpl() {
    }

    public DefaultChainHandlerImpl(Predicate<T> matcher, Function<T, R> handler) {
        this(ChainHandler.DEFAULT_ORDER, matcher, handler);
    }

    public DefaultChainHandlerImpl(int order, Predicate<T> matcher, Function<T, R> handler) {
        this.order = order;
        this.matcher = matcher;
        this.handler = handler;
    }

    @Override
    public int getOrder() {
        return Math.max(order, 0);
    }

    @Override
    public boolean matcher(T input) {
        return matcher.test(input);
    }

    @Override
    public R handle(T input) {
        return handler.apply(input);
    }
}
