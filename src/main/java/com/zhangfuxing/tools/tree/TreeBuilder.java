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

	public static class Builder<T> {
		/**
		 * 根节点集合提供者
		 */
		Supplier<Collection<T>> rootSupplier;
		/**
		 * 判断是否是根节点
		 */
		Predicate<T> isRootItem;
		/**
		 * 唯一键
		 */
		Function<T, ?> uniqueKey;
		/**
		 * 父节点键
		 */
		Function<T, ?> parentKey;
		/**
		 * 添加子节点
		 */
		BiConsumer<T, T> addChild;
		/**
		 * 是否将没有父节点的节点添加到根节点
		 */
		boolean noneParentAddToRoot;

		Collection<T> source;


		/**
		 * 构建为树型集合
		 *
		 * @return 树型集合
		 */
		public Collection<T> build() {
			check();
			return buildTree(source, rootSupplier, isRootItem, uniqueKey, parentKey, addChild, noneParentAddToRoot);
		}

		/**
		 * 构建为树型集合
		 *
		 * @return 树型集合
		 */
		public ArrayList<T> buildAsArrayList() {
			check();
			return new ArrayList<>(buildTree(source, ArrayList::new, isRootItem, uniqueKey, parentKey, addChild, noneParentAddToRoot));
		}

		/**
		 * 构建为树型集合
		 *
		 * @return 树型集合
		 */
		public LinkedList<T> buildAsLinkedList() {
			check();
			return new LinkedList<>(buildTree(source, LinkedList::new, isRootItem, uniqueKey, parentKey, addChild, noneParentAddToRoot));
		}

		/**
		 * 构建为树型集合
		 *
		 * @return 树型集合
		 */
		public TreeSet<T> buildAsTreeSet() {
			check();
			return new TreeSet<>(buildTree(source, TreeSet::new, isRootItem, uniqueKey, parentKey, addChild, noneParentAddToRoot));
		}

		/**
		 * 构建为树型集合
		 *
		 * @return 树型集合
		 */
		public HashSet<T> buildAsHashSet() {
			check();
			return new HashSet<>(buildTree(source, HashSet::new, isRootItem, uniqueKey, parentKey, addChild, noneParentAddToRoot));
		}

		private void check() {
			if (rootSupplier == null) this.rootSupplier = ArrayList::new;
			if (isRootItem == null) throw new IllegalArgumentException("未指定根节点判断条件");
			if (uniqueKey == null) throw new IllegalArgumentException("未指定唯一键");
			if (parentKey == null) throw new IllegalArgumentException("未指定父节点键");
			if (addChild == null) throw new IllegalArgumentException("未指定添加子节点的逻辑");
		}

		public Builder<T> setRootSupplier(Supplier<Collection<T>> rootSupplier) {
			this.rootSupplier = rootSupplier;
			return this;
		}

		public Builder<T> isRoot(Predicate<T> isRootItem) {
			this.isRootItem = isRootItem;
			return this;
		}

		public Builder<T> isRoot(final boolean isRootItem) {
			this.isRootItem = item -> isRootItem;
			return this;
		}

		public Builder<T> uniqueKey(Function<T, ?> uniqueKey) {
			this.uniqueKey = uniqueKey;
			return this;
		}

		public Builder<T> parentKey(Function<T, ?> parentKey) {
			this.parentKey = parentKey;
			return this;
		}

		public Builder<T> addChild(BiConsumer<T, T> addChild) {
			this.addChild = addChild;
			return this;
		}

		public Builder<T> noneParentAddToRoot(boolean noneParentAddToRoot) {
			this.noneParentAddToRoot = noneParentAddToRoot;
			return this;
		}

		public void setSource(Collection<T> source) {
			this.source = source;
		}
	}

	public static <T> Builder<T> builder(Collection<T> source) {
		Builder<T> builder = new Builder<>();
		builder.setSource(source);
		return builder;
	}

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

}
