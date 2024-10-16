package com.zhangfuxing.tools.pool;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.time.Duration;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/5/15
 * @email zhangfuxing1010@163.com
 */
public class PoolsConfig<T> extends GenericObjectPoolConfig<T> {

    public PoolsConfig() {
        setTestWhileIdle(true);
        setTimeBetweenEvictionRuns(Duration.ofMillis(30000));
        setNumTestsPerEvictionRun(-1);
    }
}
