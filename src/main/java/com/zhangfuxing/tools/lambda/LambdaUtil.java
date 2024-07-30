package com.zhangfuxing.tools.lambda;

import com.zhangfuxing.tools.lambda.meta.LambdaMeta;
import com.zhangfuxing.tools.lambda.meta.ProxyLambdaMeta;
import com.zhangfuxing.tools.lambda.meta.RefLambdaMeta;
import com.zhangfuxing.tools.lambda.meta.SerialLambdaMeta;
import com.zhangfuxing.tools.util.RefUtil;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/7/29
 * @email zhangfuxing1010@163.com
 */
public class LambdaUtil {

    /**
     * 获取lambda表达式的元信息
     *
     * @param sfun 可序列化的函数式函数接口
     * @param <T>  实体类型T
     * @return 元信息
     */
    public static <T> LambdaMeta getMeta(SFun<T, ?> sfun) {
        if (sfun instanceof Proxy proxyFun) {
            return new ProxyLambdaMeta(proxyFun);
        }
        try {
            Method method = sfun.getClass().getDeclaredMethod("writeReplace");
            method.setAccessible(true);
            return new RefLambdaMeta(((SerializedLambda) method.invoke(sfun)), sfun.getClass().getClassLoader());
        } catch (Throwable e) {
            return new SerialLambdaMeta(sfun);
        }
    }

    public static <T> String getName(SFun<T, ?> fun) {
        Field field = getField(fun);
        return field == null ? "" : field.getName();
    }

    @SafeVarargs
    public static <T> List<Field> getFields(SFun<T, ?>... funs) {
        List<Field> result = new ArrayList<>(funs.length);
        Class<?> clazz = null;
        Field[] allFields = null;
        for (SFun<T, ?> fun : funs) {
            if (fun == null) continue;
            LambdaMeta meta = getMeta(fun);
            String methodName = meta.implMethodName().toLowerCase();
            if (clazz == null) {
                clazz = meta.instanceClass();
                allFields = RefUtil.getAllFields(clazz);
            }
            String field;
            if (methodName.startsWith("is")) {
                field = methodName.substring(2);
            } else {
                field = methodName.substring(3);
            }
            for (Field item : allFields) {
                String filedName = item.getName().toLowerCase();
                if (field.equals(filedName)) {
                    result.add(item);
                }

            }
        }

        return result;
    }

    public static <T> Field getField(SFun<T, ?> fun) {
        List<Field> fields = getFields(fun);
        return fields.isEmpty() ? null : fields.get(0);
    }
}
