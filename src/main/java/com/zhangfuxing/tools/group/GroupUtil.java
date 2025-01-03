package com.zhangfuxing.tools.group;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * 集合分组工具类，提供多种分组操作方法
 *
 * @author 张福兴
 * @version 1.0
 * @date 2025/1/3
 * @email zhangfuxing1010@163.com
 */
public class GroupUtil {

	/**
	 * 对集合进行简单分组
	 *
	 * @param collection  要分组的集合
	 * @param keyFunction 分组键的提取函数
	 * @param <K>         分组键的类型
	 * @param <T>         集合元素的类型
	 * @return 分组后的Map，key为分组键，value为该组的元素列表
	 * @throws NullPointerException 如果keyFunction为null
	 * @example <pre>{@code
	 * List<Person> persons = Arrays.asList(
	 *     new Person("张三", 20),
	 *     new Person("李四", 20),
	 *     new Person("王五", 25)
	 * );
	 * Map<Integer, List<Person>> result = GroupUtil.grouping(persons, Person::getAge);
	 * // 结果: {20=[张三, 李四], 25=[王五]}
	 * }</pre>
	 */
	public static <K, T> Map<K, List<T>> grouping(Collection<T> collection, Function<? super T, K> keyFunction) {
		if (collection == null || collection.isEmpty()) {
			return new HashMap<>();
		}
		return collection.stream().collect(Collectors.groupingBy(keyFunction));
	}

	/**
	 * 对集合进行分组，支持自定义结果Map的实现
	 *
	 * @param collection  要分组的集合
	 * @param keyFunction 分组键的提取函数
	 * @param supplier    提供结果Map实例的供应商
	 * @param <K>         分组键的类型
	 * @param <T>         集合元素的类型
	 * @return 分组后的Map，使用supplier提供的Map实现
	 * @throws NullPointerException 如果keyFunction或supplier为null
	 * @example <pre>{@code
	 * List<Person> persons = getPersonList();
	 * Map<Integer, List<Person>> result = GroupUtil.grouping(
	 *     persons,
	 *     Person::getAge,
	 *     LinkedHashMap::new  // 使用LinkedHashMap保持插入顺序
	 * );
	 * }</pre>
	 */
	public static <K, T> Map<K, List<T>> grouping(Collection<T> collection,
												  Function<? super T, K> keyFunction,
												  Supplier<Map<K, List<T>>> supplier) {
		if (collection == null || collection.isEmpty()) {
			return new HashMap<>();
		}
		return collection.stream().collect(Collectors.groupingBy(keyFunction, supplier, Collectors.toList()));
	}

	/**
	 * 对集合进行高度自定义的分组操作
	 *
	 * @param collection  要分组的集合
	 * @param keyFunction 分组键的提取函数
	 * @param mapFactory  提供结果Map实例的工厂
	 * @param downstream  下游收集器，用于处理每个分组的元素
	 * @param <T>         输入元素的类型
	 * @param <K>         分组键的类型
	 * @param <D>         结果元素的类型
	 * @param <A>         下游收集器的累加器类型
	 * @param <M>         结果Map的类型
	 * @return 分组后的Map，包含经过downstream处理的结果
	 * @throws NullPointerException 如果任何参数为null
	 * @example <pre>{@code
	 * List<Person> persons = getPersonList();
	 * Map<Integer, Double> result = GroupUtil.grouping(
	 *     persons,
	 *     Person::getAge,
	 *     HashMap::new,
	 *     Collectors.averagingDouble(Person::getSalary)  // 计算每个年龄组的平均工资
	 * );
	 * }</pre>
	 */
	public static <T, K, D, A, M extends Map<K, D>> M grouping(Collection<T> collection,
															   Function<? super T, ? extends K> keyFunction,
															   Supplier<M> mapFactory,
															   Collector<? super T, A, D> downstream) {
		if (collection == null || collection.isEmpty()) {
			return mapFactory.get();
		}
		return collection.stream().collect(Collectors.groupingBy(keyFunction, mapFactory, downstream));
	}

	/**
	 * 对集合进行分组，并转换每个元素的值
	 *
	 * @param collection    要分组的集合
	 * @param keyFunction   分组键的提取函数
	 * @param valueFunction 值转换函数
	 * @param <T>           输入元素的类型
	 * @param <K>           分组键的类型
	 * @param <U>           转换后值的类型
	 * @return 分组后的Map，其中的值经过valueFunction转换
	 * @throws NullPointerException 如果keyFunction或valueFunction为null
	 * @example <pre>{@code
	 * List<Person> persons = getPersonList();
	 * Map<Integer, List<String>> result = GroupUtil.grouping(
	 *     persons,
	 *     Person::getAge,
	 *     Person::getName  // 只收集人名
	 * );
	 * // 结果: {20=[张三, 李四], 25=[王五]}
	 * }</pre>
	 */
	public static <T, K, U> Map<K, List<U>> grouping(Collection<T> collection,
													 Function<? super T, K> keyFunction,
													 Function<? super T, U> valueFunction) {
		if (collection == null || collection.isEmpty()) {
			return new HashMap<>();
		}
		return collection.stream().collect(Collectors.groupingBy(keyFunction,
				Collectors.mapping(valueFunction, Collectors.toList())));
	}

	/**
	 * 对集合进行分组，并使用下游收集器处理每个组的元素
	 *
	 * @param collection 要分组的集合
	 * @param classifier 分组键的提取函数
	 * @param downstream 下游收集器，用于处理每个分组的元素
	 * @param <T>        输入元素的类型
	 * @param <K>        分组键的类型
	 * @param <A>        下游收集器的累加器类型
	 * @param <D>        结果元素的类型
	 * @return 分组后的Map，其中的值经过downstream处理
	 * @throws NullPointerException 如果classifier或downstream为null
	 * @example <pre>{@code
	 * List<Person> persons = getPersonList();
	 * // 按年龄分组并计算每组的平均工资
	 * Map<Integer, Double> result = GroupUtil.grouping(
	 *     persons,
	 *     Person::getAge,
	 *     Collectors.averagingDouble(Person::getSalary)
	 * );
	 * // 按部门分组并统计人数
	 * Map<String, Long> countByDept = GroupUtil.grouping(
	 *     persons,
	 *     Person::getDepartment,
	 *     Collectors.counting()
	 * );
	 * }</pre>
	 */
	public static <T, K, A, D> Map<? extends K, D> grouping(Collection<T> collection,
															Function<? super T, ? extends K> classifier,
															Collector<? super T, A, D> downstream) {
		if (collection == null || collection.isEmpty()) {
			return new HashMap<>();
		}
		return collection.stream().collect(Collectors.groupingBy(classifier, downstream));
	}

	/**
	 * 对集合进行多级分组
	 *
	 * @param collection 要分组的集合
	 * @param functions  分组键的提取函数序列，按顺序应用
	 * @param <T>        输入元素的类型
	 * @param <K>        分组键的类型
	 * @return 多级嵌套的Map结构
	 * @throws IllegalArgumentException 如果functions为空
	 * @example <pre>{@code
	 * List<Person> persons = getPersonList();
	 * // 先按部门分组，再按年龄分组
	 * Map<String, Map<Integer, List<Person>>> result = GroupUtil.multiGrouping(
	 *     persons,
	 *     Person::getDepartment,
	 *     Person::getAge
	 * );
	 * }</pre>
	 */
	@SafeVarargs
	public static <T, K> Map<K, ?> multiGrouping(Collection<T> collection, Function<? super T, ? extends K>... functions) {
		if (functions == null || functions.length == 0) {
			throw new IllegalArgumentException("至少需要一个分组函数");
		}
		if (collection == null || collection.isEmpty()) {
			return new HashMap<>();
		}

		Collector<? super T, ?, ?> collector = Collectors.toList();
		for (int i = functions.length - 1; i >= 0; i--) {
			collector = Collectors.groupingBy(functions[i], collector);
		}
		return collection.stream().collect((Collector<T, ?, Map<K, ?>>) collector);
	}

	/**
	 * 对集合进行分组并计算每组的统计信息
	 *
	 * @param collection    要分组的集合
	 * @param keyFunction   分组键的提取函数
	 * @param valueFunction 用于统计的数值提取函数
	 * @param <T>           输入元素的类型
	 * @param <K>           分组键的类型
	 * @return 每组的统计信息，包含计数、总和、平均值、最大值、最小值等
	 * @example <pre>{@code
	 * List<Person> persons = getPersonList();
	 * // 按部门统计工资信息
	 * Map<String, DoubleSummaryStatistics> stats = GroupUtil.groupingWithStats(
	 *     persons,
	 *     Person::getDepartment,
	 *     Person::getSalary
	 * );
	 * // 使用统计信息
	 * stats.forEach((dept, stat) -> {
	 *     System.out.println("部门: " + dept);
	 *     System.out.println("  人数: " + stat.getCount());
	 *     System.out.println("  平均工资: " + stat.getAverage());
	 *     System.out.println("  最高工资: " + stat.getMax());
	 *     System.out.println("  最低工资: " + stat.getMin());
	 * });
	 * }</pre>
	 */
	public static <T, K> Map<K, DoubleSummaryStatistics> groupingWithStats(
			Collection<T> collection,
			Function<? super T, ? extends K> keyFunction,
			ToDoubleFunction<? super T> valueFunction) {
		if (collection == null || collection.isEmpty()) {
			return new HashMap<>();
		}
		return collection.stream().collect(
				Collectors.groupingBy(keyFunction,
						Collectors.summarizingDouble(valueFunction))
		);
	}

	/**
	 * 对集合进行分组并对每组元素应用归约操作
	 *
	 * @param collection    要分组的集合
	 * @param keyFunction   分组键的提取函数
	 * @param valueFunction 值转换函数
	 * @param operator      归约操作
	 * @param <T>           输入元素的类型
	 * @param <K>           分组键的类型
	 * @param <U>           归约结果的类型
	 * @return 每组归约后的结果
	 * @example <pre>{@code
	 * List<Person> persons = getPersonList();
	 * // 按部门计算工资总和
	 * Map<String, Double> result = GroupUtil.groupingAndReduce(
	 *     persons,
	 *     Person::getDepartment,
	 *     Person::getSalary,
	 *     Double::sum
	 * );
	 * // 按年龄找出每组工资最高的人
	 * Map<Integer, Double> maxSalaries = GroupUtil.groupingAndReduce(
	 *     persons,
	 *     Person::getAge,
	 *     Person::getSalary,
	 *     Double::max
	 * );
	 * }</pre>
	 */
	public static <T, K, U> Map<K, U> groupingAndReduce(
			Collection<T> collection,
			Function<? super T, ? extends K> keyFunction,
			Function<? super T, ? extends U> valueFunction,
			BinaryOperator<U> operator) {
		if (collection == null || collection.isEmpty()) {
			return new HashMap<>();
		}
		return collection.stream().collect(
						Collectors.groupingBy(
								keyFunction,
								Collectors.mapping(
										valueFunction,
										Collectors.reducing(operator)
								)
						)
				).entrySet().stream()
				.filter(e -> e.getValue().isPresent())
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						e -> e.getValue().get()
				));
	}

	/**
	 * 对集合进行分组并对每组元素进行过滤
	 *
	 * @param collection  要分组的集合
	 * @param keyFunction 分组键的提取函数
	 * @param predicate   过滤条件
	 * @param <T>         输入元素的类型
	 * @param <K>         分组键的类型
	 * @return 每组过滤后的结果
	 * @example <pre>{@code
	 * List<Person> persons = getPersonList();
	 * // 按部门分组，并只保留工资大于10000的员工
	 * Map<String, List<Person>> result = GroupUtil.groupingAndFilter(
	 *     persons,
	 *     Person::getDepartment,
	 *     p -> p.getSalary() > 10000
	 * );
	 * }</pre>
	 */
	public static <T, K> Map<K, List<T>> groupingAndFilter(
			Collection<T> collection,
			Function<? super T, ? extends K> keyFunction,
			Predicate<? super T> predicate) {
		if (collection == null || collection.isEmpty()) {
			return new HashMap<>();
		}
		return collection.stream()
				.filter(predicate)
				.collect(Collectors.groupingBy(keyFunction));
	}

	/**
	 * 并发分组，使用默认的 ForkJoinPool
	 *
	 * @param collection  要分组的集合
	 * @param keyFunction 分组键的提取函数
	 * @param <K>         分组键的类型
	 * @param <T>         集合元素的类型
	 * @return 分组后的并发Map
	 * @example <pre>{@code
	 * List<Person> persons = getLargePersonList(); // 假设有大量数据
	 * Map<String, List<Person>> result = GroupUtil.groupingParallel(
	 *     persons,
	 *     Person::getDepartment
	 * );
	 * }</pre>
	 */
	public static <K, T> Map<K, List<T>> groupingParallel(Collection<T> collection,
														  Function<? super T, ? extends K> keyFunction) {
		if (collection == null || collection.isEmpty()) {
			return new ConcurrentHashMap<>();
		}
		return collection.parallelStream()
				.collect(Collectors.groupingByConcurrent(keyFunction));
	}

	/**
	 * 使用自定义线程池进行并发分组
	 *
	 * @param collection  要分组的集合
	 * @param keyFunction 分组键的提取函数
	 * @param pool        自定义的ForkJoinPool
	 * @param <K>         分组键的类型
	 * @param <T>         集合元素的类型
	 * @return 分组后的并发Map
	 * @example <pre>{@code
	 * List<Person> persons = getLargePersonList();
	 * ForkJoinPool customPool = new ForkJoinPool(10); // 自定义线程池大小
	 * Map<String, List<Person>> result = GroupUtil.groupingParallel(
	 *     persons,
	 *     Person::getDepartment,
	 *     customPool
	 * );
	 * }</pre>
	 */
	public static <K, T> Map<? extends K, List<T>> groupingParallel(Collection<T> collection,
																	Function<? super T, ? extends K> keyFunction,
																	ForkJoinPool pool) {
		if (collection == null || collection.isEmpty()) {
			return new ConcurrentHashMap<>();
		}
		return pool.submit(() ->
				collection.parallelStream()
						.collect(Collectors.groupingByConcurrent(keyFunction))
		).join();
	}

	/**
	 * 并发分组并对每组元素进行转换
	 *
	 * @param collection    要分组的集合
	 * @param keyFunction   分组键的提取函数
	 * @param valueFunction 值转换函数
	 * @param <T>           输入元素的类型
	 * @param <K>           分组键的类型
	 * @param <U>           转换后值的类型
	 * @return 分组后的并发Map
	 * @example <pre>{@code
	 * List<Person> persons = getLargePersonList();
	 * Map<String, List<String>> result = GroupUtil.groupingParallel(
	 *     persons,
	 *     Person::getDepartment,
	 *     Person::getName
	 * );
	 * }</pre>
	 */
	public static <T, K, U> Map<K, List<U>> groupingParallel(Collection<T> collection,
															 Function<? super T, ? extends K> keyFunction,
															 Function<? super T, ? extends U> valueFunction) {
		if (collection == null || collection.isEmpty()) {
			return new ConcurrentHashMap<>();
		}
		return collection.parallelStream()
				.collect(Collectors.groupingByConcurrent(keyFunction,
						Collectors.mapping(valueFunction, Collectors.toList())));
	}

	/**
	 * 并发分组并对每组元素进行归约操作
	 *
	 * @param collection    要分组的集合
	 * @param keyFunction   分组键的提取函数
	 * @param valueFunction 值转换函数
	 * @param operator      归约操作
	 * @param <T>           输入元素的类型
	 * @param <K>           分组键的类型
	 * @param <U>           归约结果的类型
	 * @return 分组后的并发Map
	 * @example <pre>{@code
	 * List<Person> persons = getLargePersonList();
	 * // 并发计算每个部门的工资总和
	 * Map<String, Double> result = GroupUtil.groupingParallelAndReduce(
	 *     persons,
	 *     Person::getDepartment,
	 *     Person::getSalary,
	 *     Double::sum
	 * );
	 * }</pre>
	 */
	public static <T, K, U> Map<K, U> groupingParallelAndReduce(Collection<T> collection,
																Function<? super T, ? extends K> keyFunction,
																Function<? super T, ? extends U> valueFunction,
																BinaryOperator<U> operator) {
		if (collection == null || collection.isEmpty()) {
			return new ConcurrentHashMap<>();
		}
		return collection.parallelStream()
				.collect(Collectors.groupingByConcurrent(
						keyFunction,
						Collectors.mapping(
								valueFunction,
								Collectors.reducing(operator)
						)
				))
				.entrySet().stream()
				.filter(e -> e.getValue().isPresent())
				.collect(Collectors.toConcurrentMap(
						Map.Entry::getKey,
						e -> e.getValue().get()
				));
	}

	/**
	 * 并发分组并计算每组的统计信息
	 *
	 * @param collection    要分组的集合
	 * @param keyFunction   分组键的提取函数
	 * @param valueFunction 用于统计的数值提取函数
	 * @param <T>           输入元素的类型
	 * @param <K>           分组键的类型
	 * @return 每组的统计信息
	 * @example <pre>{@code
	 * List<Person> persons = getLargePersonList();
	 * // 并发统计每个部门的工资信息
	 * Map<String, DoubleSummaryStatistics> stats = GroupUtil.groupingParallelWithStats(
	 *     persons,
	 *     Person::getDepartment,
	 *     Person::getSalary
	 * );
	 * }</pre>
	 */
	public static <T, K> Map<K, DoubleSummaryStatistics> groupingParallelWithStats(
			Collection<T> collection,
			Function<? super T, ? extends K> keyFunction,
			ToDoubleFunction<? super T> valueFunction) {
		if (collection == null || collection.isEmpty()) {
			return new ConcurrentHashMap<>();
		}
		return collection.parallelStream()
				.collect(Collectors.groupingByConcurrent(
						keyFunction,
						Collectors.summarizingDouble(valueFunction)
				));
	}
}
