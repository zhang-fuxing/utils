package com.zhangfuxing.tools.async;

import java.util.concurrent.*;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2025/5/6
 * @email zhangfuxing1010@163.com
 */
public class Async {
	private static final boolean USE_COMMON_POOL =
			(ForkJoinPool.getCommonPoolParallelism() > 1);

	private static final Executor EXECUTOR = USE_COMMON_POOL ? ForkJoinPool.commonPool() : command -> new Thread(command).start();

	public static <T> Asyncable<T> of(Callable<T> callable) {
		return new AsyncableImpl<>(callable);
	}

	public static <T> Asyncable<T> of(Callable<T> callable, boolean startNow) {
		Asyncable<T> asyncable = of(callable);
		if (startNow) {
			asyncable.start();
		}
		return asyncable;
	}

	public static <T> Asyncable<T> of(Runnable runnable) {
		return new AsyncableImpl<>(runnable);
	}

	public static <T> Asyncable<T> of(Runnable runnable, boolean startNow) {
		Asyncable<T> asyncable = of(runnable);
		if (startNow) {
			asyncable.start();
		}
		return asyncable;
	}

	public static <T> Asyncable<T> of(Executor executor, Callable<T> callable) {
		return new AsyncableImpl<>(executor, callable);
	}

	public static <T> Asyncable<T> of(Executor executor, Callable<T> callable, boolean startNow) {
		Asyncable<T> asyncable = of(executor, callable);
		if (startNow) {
			asyncable.start();
		}
		return asyncable;
	}

	public static <T> Asyncable<T> of(Executor executor, Runnable runnable) {
		return new AsyncableImpl<>(executor, runnable);
	}

	public static <T> Asyncable<T> of(Executor executor, Runnable runnable, boolean startNow) {
		Asyncable<T> asyncable = of(executor, runnable);
		if (startNow) {
			asyncable.start();
		}
		return asyncable;
	}


	private static class AsyncableImpl<T> implements Asyncable<T> {
		private final Executor executor;
		private final Runnable runnable;
		private final Callable<T> callable;
		CompletableFuture<T> future;
		private boolean  started;

		public AsyncableImpl(Callable<T> callable) {
			this(EXECUTOR, callable);
		}

		public AsyncableImpl(Runnable runnable) {
			this(EXECUTOR, runnable);
		}

		public AsyncableImpl(Executor executor, Callable<T> callable) {
			this(executor, null, callable);
		}

		public AsyncableImpl(Executor executor, Runnable runnable) {
			this(executor, runnable, null);
		}

		private AsyncableImpl(Executor executor, Runnable runnable, Callable<T> callable) {
			this.executor = executor;
			this.runnable = runnable;
			this.callable = callable;
		}

		@Override
		@SuppressWarnings("unchecked")
		public void start() {
			if (runnable == null && callable == null) {
				throw new IllegalArgumentException("请设置执行任务");
			}
			if (runnable != null) {
				this.future = (CompletableFuture<T>) CompletableFuture.runAsync(this.runnable, executor);
			} else {
				this.future = CompletableFuture.supplyAsync(() -> {
					try {
						return this.callable.call();
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}, executor);
			}
			this.started = true;
		}

		@Override
		public void stop() {
			this.checkStarted();
			this.future.cancel(true);
		}

		@Override
		public void stop(long timeout, TimeUnit unit) {
			try {
				this.get(timeout, unit);
			} catch (Exception e) {
				this.stop();
			}
		}

		@Override
		public CompletableFuture<T> asFuture() {
			this.checkStarted();
			return this.future;
		}

		@Override
		public T get() {
			this.checkStarted();
			try {
				return this.future.get();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public T get(long timeout, TimeUnit unit) {
			this.checkStarted();
			try {
				return this.future.get(timeout, unit);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}


		private void checkStarted() {
			if (!started) {
				throw new IllegalStateException("请先调用start方法");
			}
		}
	}
}
