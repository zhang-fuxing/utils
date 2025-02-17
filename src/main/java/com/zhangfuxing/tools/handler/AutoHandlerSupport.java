package com.zhangfuxing.tools.handler;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2025/1/6
 * @email zhangfuxing1010@163.com
 */
public interface AutoHandlerSupport<T extends AutoHandlerSupport<T>> {

	void setNext(T next);

	T getNext();

	default boolean canHandle() {
		return false;
	}

	/**
	 * 遍历所有处理器
	 *
	 * @param consumer 处理器消费函数
	 */
	@SuppressWarnings("unchecked")
	default void forEach(Consumer<T> consumer) {
		T current = (T) this;
		while (current != null) {
			consumer.accept(current);
			current = (T) current.getNext();
		}
	}

	/**
	 * 可中断的遍历器
	 *
	 * @param consumer 处理器消费函数,返回true表示继续遍历,返回false表示中断遍历
	 */
	@SuppressWarnings("unchecked")
	default void forEachBreakable(Function<T, Boolean> consumer) {
		T current = (T) this;
		while (current != null) {
			if (!consumer.apply(current)) {
				break;
			}
			current = (T) current.getNext();
		}
	}

}
