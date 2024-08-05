package com.zhangfuxing.tools.lambda;

import java.io.Serializable;
import java.util.function.Function;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/7/29
 * @email zhangfuxing1010@163.com
 */
@FunctionalInterface
public interface SFun<T,R> extends Function<T,R>, Serializable {


}
