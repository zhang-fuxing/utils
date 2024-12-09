package com.zhangfuxing.tools.timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/12/9
 * @email zhangfuxing1010@163.com
 */
public class ProgramTimer {
    private final Logger log;
    private final long createTime;
    private long currentTime;

    public ProgramTimer(Logger log) {
        this.log = log;
        this.createTime = System.currentTimeMillis();
        this.currentTime = this.createTime;
    }

    public ProgramTimer() {
        this(ProgramTimer.class);
    }

    public ProgramTimer(Class<?> clazz) {
        this(LoggerFactory.getLogger(clazz));
    }

    public ProgramTimer(String className) {
        this(LoggerFactory.getLogger(className));
    }

    public static ProgramTimer timer(Class<?> clazz) {
        return new ProgramTimer(LoggerFactory.getLogger(clazz));
    }

    public static ProgramTimer timer(String className) {
        return new ProgramTimer(LoggerFactory.getLogger(className));
    }

    public static ProgramTimer timer(Logger log) {
        return new ProgramTimer(log);
    }

    public void take(String tip) {
        take(tip, this.currentTime, System.currentTimeMillis());
    }

    public void take() {
        take("程序执行耗时");
    }

    public void totalTake(String tip) {
        take(tip, this.createTime, System.currentTimeMillis());
    }

    public void totalTake() {
        take("执行总耗时", this.createTime, System.currentTimeMillis());
    }

    public void take(String tip, long startTime, long endTime) {
        if (tip == null || tip.isBlank()) {
            tip = "执行耗时";
        }
        log.info("{}: {} ms", tip, (endTime - startTime));
        this.currentTime = endTime;
    }
}
