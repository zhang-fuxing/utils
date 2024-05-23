package com.zhangfuxing.tools.util;


import com.zhangfuxing.tools.chain.ConsumerChain;
import com.zhangfuxing.tools.chain.FunChain;
import com.zhangfuxing.tools.chain.RunnableChain;

import java.util.function.Supplier;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2023/12/28
 * @email zhangfuxing@kingshine.com.cn
 */
public class ChainUtil {
    /**
     * 创建一个FunChain对象
     *
     * @param <T> 泛型类型
     * @return FunChain对象
     */
    public static <T> FunChain<T> funChain() {
        return FunChain.create(null);
    }

    /**
     * 创建一个FunChain对象
     *
     * @param <T> 数据类型
     * @param data 传入的数据
     * @return FunChain对象
     */
    public static <T> FunChain<T> funChain(T data) {
        return FunChain.create(data);
    }

    /**
     * 创建一个FunChain对象，通过给定的Supplier对象实现函数链的最末端。
     *
     * @param fun 用于实现函数链最末端的Supplier对象
     * @param <T> 泛型类型参数，用于指定函数链返回值的类型
     * @return FunChain对象
     */
    public static <T> FunChain<T> funChain(Supplier<T> fun) {
        return FunChain.doChain(fun);
    }

    public static <T> ConsumerChain<T> consumerChain() {
        return ConsumerChain.create(null);
    }

    public static <T> ConsumerChain<T> consumerChain(T data) {
        return ConsumerChain.create(data);
    }

    public static RunnableChain runChain(Runnable... runnable) {
        RunnableChain chain = RunnableChain.create();
        if (runnable != null) {
            for (Runnable run : runnable) {
                chain.then(run);
            }
        }
        return chain;
    }


}

