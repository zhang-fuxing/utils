package com.zhangfuxing.tools.chain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 可链式注册一个匹配器和代码片段，注册完成后可传入一个参数，用于配配器进行匹配，如果能成功匹配，则执行对应的代码片段，返回一个类型
 * <p>
 * 类似于 if-else 的效果，也可以执行所有匹配项
 * exp:
 * <p>
 * ChainHandlerRegister.<String, Void>newInstance() <br/>
 * .register("A", input -> {
 * System.out.println(input); // A
 * return null;
 * })<br/>
 * .register("B", input -> {
 * System.out.println(input);// B
 * return null;
 * }) <br/>
 * .register(input -> input.equals("C"), input -> {
 * System.out.println(input);// C
 * return null;
 * }) <br/>
 * .doFirstMatching("C");
 *
 * @param <T> 输入类型
 * @param <R> 输出类型
 * @author 张福兴
 * @version 1.0
 * @date 2024/5/8
 * @email zhangfuxing1010@163.com
 */
public class ChainHandlerRegister<T, R> {
    protected final ArrayList<ChainHandler<T, R>> chainHandlers = new ArrayList<>();

    public static <T, R> ChainHandlerRegister<T, R> newInstance() {
        return new ChainHandlerRegister<T, R>();
    }

    /**
     * 注册一个链式处理器
     *
     * @param chainHandler 链式处理器
     * @return 当前实例
     */
    public ChainHandlerRegister<T, R> register(ChainHandler<T, R> chainHandler) {
        chainHandlers.add(chainHandler);
        return this;
    }

    public <K> ChainHandlerRegister<T, R> addFunction(K key, Function<T, R> handler) {
        return this.addFunction(key::equals, handler);
    }

    public <K> ChainHandlerRegister<T, R> addFunction(int order, K key, Function<T, R> handler) {
        return this.addFunction(order, key::equals, handler);
    }

    /**
     * 注册一个链式匹配器和处理器
     *
     * @param matcher 匹配器
     * @param handler 处理器
     * @return 当前实例
     */
    public ChainHandlerRegister<T, R> addFunction(Predicate<T> matcher, Function<T, R> handler) {
        return this.addFunction(ChainHandler.DEFAULT_ORDER, matcher, handler);
    }

    /**
     * 注册一个链式匹配器和处理器，并指定顺序
     *
     * @param order   顺序, 值越小越先执行,需要显示调用 {@link #sorted()} 方法排序, 否则按照注册顺序执行
     * @param matcher 匹配器
     * @param handler 处理器
     * @return 当前实例
     */
    public ChainHandlerRegister<T, R> addFunction(int order, Predicate<T> matcher, Function<T, R> handler) {
        return this.register(new DefaultChainHandlerImpl<>(order, matcher, handler));
    }

    // runnable
    public <K> ChainHandlerRegister<T, R> addRunnable(K key, Runnable handler) {
        return this.addRunnable(key::equals, handler);
    }

    public <K> ChainHandlerRegister<T, R> runnable(int order, K key, Runnable handler) {
        return this.addRunnable(order, key::equals, handler);
    }

    /**
     * 注册一个链式匹配器和处理器
     *
     * @param matcher 匹配器
     * @param handler 处理器
     * @return 当前实例
     */
    public ChainHandlerRegister<T, R> addRunnable(Predicate<T> matcher, Runnable handler) {
        return this.addRunnable(ChainHandler.DEFAULT_ORDER, matcher, handler);
    }

    /**
     * 注册一个链式匹配器和处理器，并指定顺序
     *
     * @param order   顺序, 值越小越先执行,需要显示调用 {@link #sorted()} 方法排序, 否则按照注册顺序执行
     * @param matcher 匹配器
     * @param handler 处理器
     * @return 当前实例
     */
    public ChainHandlerRegister<T, R> addRunnable(int order, Predicate<T> matcher, Runnable handler) {
        return this.register(new DefaultChainHandlerImpl<>(order, matcher, t -> {
            handler.run();
            return null;
        }));
    }

    // consumer
    public <K> ChainHandlerRegister<T, R> addConsumer(K key, Consumer<T> handler) {
        return this.addConsumer(key::equals, handler);
    }

    public <K> ChainHandlerRegister<T, R> addConsumer(int order, K key, Consumer<T> handler) {
        return this.addConsumer(order, key::equals, handler);
    }

    /**
     * 注册一个链式匹配器和处理器
     *
     * @param matcher 匹配器
     * @param handler 处理器
     * @return 当前实例
     */
    public ChainHandlerRegister<T, R> addConsumer(Predicate<T> matcher, Consumer<T> handler) {
        return this.addConsumer(ChainHandler.DEFAULT_ORDER, matcher, handler);
    }

    /**
     * 注册一个链式匹配器和处理器，并指定顺序
     *
     * @param order   顺序, 值越小越先执行,需要显示调用 {@link #sorted()} 方法排序, 否则按照注册顺序执行
     * @param matcher 匹配器
     * @param handler 处理器
     * @return 当前实例
     */
    public ChainHandlerRegister<T, R> addConsumer(int order, Predicate<T> matcher, Consumer<T> handler) {
        return this.register(new DefaultChainHandlerImpl<>(order, matcher, t -> {
            handler.accept(t);
            return null;
        }));
    }

    // supplier
    public <K> ChainHandlerRegister<T, R> addSupplier(K key, Supplier<R> handler) {
        return this.addSupplier(key::equals, handler);
    }

    public <K> ChainHandlerRegister<T, R> addSupplier(int order, K key, Supplier<R> handler) {
        return this.addSupplier(order, key::equals, handler);
    }

    /**
     * 注册一个链式匹配器和处理器
     *
     * @param matcher 匹配器
     * @param handler 处理器
     * @return 当前实例
     */
    public ChainHandlerRegister<T, R> addSupplier(Predicate<T> matcher, Supplier<R> handler) {
        return this.addSupplier(ChainHandler.DEFAULT_ORDER, matcher, handler);
    }

    /**
     * 注册一个链式匹配器和处理器，并指定顺序
     *
     * @param order   顺序, 值越小越先执行,需要显示调用 {@link #sorted()} 方法排序, 否则按照注册顺序执行
     * @param matcher 匹配器
     * @param handler 处理器
     * @return 当前实例
     */
    public ChainHandlerRegister<T, R> addSupplier(int order, Predicate<T> matcher, Supplier<R> handler) {
        return this.register(new DefaultChainHandlerImpl<>(order, matcher, t -> handler.get()));
    }

    /**
     * 按照注册顺序排序
     *
     * @return 当前实例
     */
    public ChainHandlerRegister<T, R> sorted() {
        chainHandlers.sort(Comparator.comparingInt(ChainHandler::getOrder));
        return this;
    }

    /**
     * 执行链式处理, 匹配到第一个处理器返回结果, 没有匹配到返回 null
     *
     * @param source 输入对象
     * @return 输出对象
     */
    public R doFirstMatching(T source) {
        return chainHandlers.stream()
                .filter(chainHandler -> chainHandler.matcher(source))
                .findFirst()
                .map(chainHandler -> chainHandler.handle(source))
                .orElse(null);
    }

    /**
     * 执行链式处理, 匹配到所有处理器返回结果
     *
     * @param source 输入对象
     * @return 输出对象列表
     */
    public List<R> doAllMatching(T source) {
        return chainHandlers.stream()
                .filter(chainHandler -> chainHandler.matcher(source))
                .map(chainHandler -> chainHandler.handle(source))
                .collect(Collectors.toList());
    }

    public void runAllMatching(T source) {
        chainHandlers.stream()
                .filter(chainHandler -> chainHandler.matcher(source))
                .forEachOrdered(chainHandler -> chainHandler.handle(source));
    }

}
