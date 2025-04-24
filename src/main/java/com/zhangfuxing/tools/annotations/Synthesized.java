package com.zhangfuxing.tools.annotations;

import java.lang.annotation.*;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2025/4/21
 * @email zhangfuxing1010@163.com
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE})
public @interface Synthesized {
}
