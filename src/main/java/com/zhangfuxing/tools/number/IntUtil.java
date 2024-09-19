package com.zhangfuxing.tools.number;

import java.util.Objects;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/9/18
 * @email zhangfuxing1010@163.com
 */
public class IntUtil {
    public static Integer add(Integer... addInts) {
        int result = 0;
        for (Integer addInt : addInts) {
            if (addInt != null) {
                result += addInt;
            }
        }
        return result;
    }

    public static Integer subtract(Integer number,Integer... subtractInts) {
        int result = Objects.requireNonNullElse(number, 0);
        for (Integer subtractInt : subtractInts) {
            if (subtractInt == null) continue;
            result -= subtractInt;
        }

        return result;
    }

    public static Integer multiply(boolean nullToZero,Integer number,Integer... multiplyInts) {
        if (nullToZero) {
            if (number == null) return 0;
            for (Integer multiplyInt : multiplyInts) {
                if (multiplyInt == null) return 0;
                number *= multiplyInt;
            }
        } else {
            if (number == null) number = 1;
            for (Integer multiplyInt : multiplyInts) {
                if (multiplyInt == null) continue;
                number *= multiplyInt;
            }
        }
        return number;
    }

    public static Integer multiply(Integer number,Integer... multiplyInts) {
        return multiply(false, number, multiplyInts);
    }

    public static Integer v(Integer value) {
        return value == null ? 0 : value;
    }

}
