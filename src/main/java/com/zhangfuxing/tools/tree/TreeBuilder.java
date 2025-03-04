package com.zhangfuxing.tools.tree;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2025/3/4
 * @email zhangfuxing1010@163.com
 */
public class TreeBuilder {

	/**
	 * 根据传入的集合构建树
	 *
	 * @param source              数据源
	 * @param rootSupplier        保存根节点的集合提供者
	 * @param isRootItem          节点是否是根节点
	 * @param uniqueKey           唯一键，元素的ID
	 * @param parentKey           父节点键， 元素的父元素ID
	 * @param addChild            添加子节点， 判空等问题需要调用方处理
	 * @param noneParentAddToRoot 是否将没有父节点的节点添加到根节点
	 * @param <T>                 泛型
	 * @return 构建为树型的集合
	 */
	public static <T> Collection<T> buildTree(Collection<T> source,
											  Supplier<Collection<T>> rootSupplier,
											  Predicate<T> isRootItem,
											  Function<T, ?> uniqueKey,
											  Function<T, ?> parentKey,
											  BiConsumer<T, T> addChild,
											  boolean noneParentAddToRoot) {
		Collection<T> root = rootSupplier.get();
		if (source == null || source.isEmpty()) {
			return root;
		}
		Map<Object, T> map = new HashMap<>();
		for (T item : source) {
			Object apply = uniqueKey.apply(item);
			map.put(apply, item);
		}

		for (T item : source) {
			if (isRootItem.test(item)) {
				root.add(item);
				continue;
			}
			Object parentId = parentKey.apply(item);
			T parent = map.get(parentId);
			if (parent == null) {
				if (noneParentAddToRoot) {
					root.add(item);
				}
				continue;
			}
			addChild.accept(parent, item);
		}
		return root;
	}


	/**
	 * 根据传入的集合构建树，使用ArrayList保存根节点
	 *
	 * @param source              数据源
	 * @param isRootItem          节点是否是根节点
	 * @param uniqueKey           唯一键，元素的ID
	 * @param parentKey           父节点键， 元素的父元素ID
	 * @param addChild            添加子节点， 判空等问题需要调用方处理
	 * @param noneParentAddToRoot 是否将没有父节点的节点添加到根节点
	 * @param <T>                 泛型
	 * @return 构建为树型的集合
	 */
	public static <T> List<T> arrayListTree(Collection<T> source, Predicate<T> isRootItem,
											Function<T, ?> uniqueKey,
											Function<T, ?> parentKey,
											BiConsumer<T, T> addChild,
											boolean noneParentAddToRoot) {
		return new ArrayList<>(buildTree(source, ArrayList::new, isRootItem, uniqueKey, parentKey, addChild, noneParentAddToRoot));
	}

	/**
	 * 根据传入的集合构建树，使用LinkedList保存根节点
	 *
	 * @param source              数据源
	 * @param isRootItem          节点是否是根节点
	 * @param uniqueKey           唯一键，元素的ID
	 * @param parentKey           父节点键， 元素的父元素ID
	 * @param addChild            添加子节点， 判空等问题需要调用方处理
	 * @param noneParentAddToRoot 是否将没有父节点的节点添加到根节点
	 * @param <T>                 泛型
	 * @return 构建为树型的集合
	 */
	public static <T> List<T> linkedListTree(Collection<T> source, Predicate<T> isRootItem,
											 Function<T, ?> uniqueKey,
											 Function<T, ?> parentKey,
											 BiConsumer<T, T> addChild,
											 boolean noneParentAddToRoot) {
		return new LinkedList<>(buildTree(source, LinkedList::new, isRootItem, uniqueKey, parentKey, addChild, noneParentAddToRoot));
	}

	/**
	 * 根据传入的集合构建树，使用TreeSet保存根节点
	 *
	 * @param source              数据源
	 * @param isRootItem          节点是否是根节点
	 * @param uniqueKey           唯一键，元素的ID
	 * @param parentKey           父节点键， 元素的父元素ID
	 * @param addChild            添加子节点， 判空等问题需要调用方处理
	 * @param noneParentAddToRoot 是否将没有父节点的节点添加到根节点
	 * @param <T>                 泛型
	 * @return 构建为树型的集合
	 */
	public static <T> Set<T> setTree(Collection<T> source, Predicate<T> isRootItem,
									 Function<T, ?> uniqueKey,
									 Function<T, ?> parentKey,
									 BiConsumer<T, T> addChild,
									 boolean noneParentAddToRoot) {
		return new TreeSet<>(buildTree(source, TreeSet::new, isRootItem, uniqueKey, parentKey, addChild, noneParentAddToRoot));
	}

	/**
	 * 根据传入的集合构建树，使用HashSet保存根节点
	 *
	 * @param source              数据源
	 * @param isRootItem          节点是否是根节点
	 * @param uniqueKey           唯一键，元素的ID
	 * @param parentKey           父节点键， 元素的父元素ID
	 * @param addChild            添加子节点， 判空等问题需要调用方处理
	 * @param noneParentAddToRoot 是否将没有父节点的节点添加到根节点
	 * @param <T>                 泛型
	 * @return 构建为树型的集合
	 */
	public static <T> Set<T> hashTree(Collection<T> source, Predicate<T> isRootItem,
									  Function<T, ?> uniqueKey,
									  Function<T, ?> parentKey,
									  BiConsumer<T, T> addChild,
									  boolean noneParentAddToRoot) {
		return new HashSet<>(buildTree(source, HashSet::new, isRootItem, uniqueKey, parentKey, addChild, noneParentAddToRoot));
	}
}
