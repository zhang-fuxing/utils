package com.zhangfuxing.tools;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2023/05/12
 * @email zhangfuxing1010@163.com
 */
public class OptionalUtil {
	public static  <T> Optional<T> of(T t) {
		if (t == null) return Optional.empty();
		if (t instanceof Collection<?> list && list.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(t);
	}

	public static <T> Stream<T> getStream(List<T> t) {
		return of(t).stream().flatMap(Collection::stream);
	}
	
}
