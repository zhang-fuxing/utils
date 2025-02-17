package com.zhangfuxing.tools.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 延时任务工具类
 *
 * @author 张福兴
 * @version 1.0
 * @date 2025/2/17
 * @email zhangfuxing1010@163.com
 */
public class DelayTask {
	ScheduledExecutorService executor;
	long delayTime = 0;
	TimeUnit timeUnit = TimeUnit.MILLISECONDS;
	Runnable[] tasks;
	boolean autoShutdown = true;

	private DelayTask() {
	}

	public static DelayTask create(int corePoolSize) {
		DelayTask delayTask = new DelayTask();
		delayTask.executor = Executors.newScheduledThreadPool(corePoolSize);
		return delayTask;
	}

	public static DelayTask create() {
		return create(1);
	}

	public DelayTask setDelayTime(long delayTime) {
		this.delayTime = delayTime;
		return this;
	}

	public DelayTask setTimeUnit(TimeUnit timeUnit) {
		this.timeUnit = timeUnit;
		return this;
	}

	public DelayTask setTasks(Runnable... tasks) {
		if (this.tasks != null) {
			Runnable[] temp = new Runnable[this.tasks.length + tasks.length];
			// 数组合并到 temp
			System.arraycopy(this.tasks, 0, temp, 0, this.tasks.length);
			System.arraycopy(tasks, 0, temp, this.tasks.length, tasks.length);
			this.tasks = temp;
		} else {
			this.tasks = tasks;
		}
		return this;
	}

	public DelayTask setAutoShutdown(boolean autoShutdown) {
		this.autoShutdown = autoShutdown;
		return this;
	}

	public void start() {
		if (executor.isShutdown()) {
			throw new RuntimeException("延迟执行线程池已关闭");
		}
		if (tasks == null) {
			throw new RuntimeException("请设置延时任务");
		}
		for (Runnable task : tasks) {
			executor.schedule(task, delayTime, timeUnit);
		}
		if (autoShutdown) {
			executor.shutdown();
		}
	}
}
