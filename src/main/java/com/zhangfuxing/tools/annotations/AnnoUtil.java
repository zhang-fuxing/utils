package com.zhangfuxing.tools.annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2025/4/21
 * @email zhangfuxing1010@163.com
 */
public abstract class AnnoUtil {

	public static <T extends Annotation> T getAnnotation(Method method, Class<T> annotationClass) {
		T annotation = method.getAnnotation(annotationClass);
		if (annotation != null) {
			return annotation;
		}
		for (Annotation anno : method.getAnnotations()) {
			T metaAnnotation = anno.annotationType().getAnnotation(annotationClass);
			if (metaAnnotation != null) {
				return buildSynthesizedAnnotationImpl(anno, metaAnnotation);
			}
		}
		return null;
	}

	public static <T extends Annotation> T getAnnotation(AnnotatedElement element, Class<T> annotationClass) {
		T annotation = element.getAnnotation(annotationClass);
		if (annotation != null) {
			return annotation;
		}
		for (Annotation anno : element.getAnnotations()) {
			T metaAnnotation = anno.annotationType().getAnnotation(annotationClass);
			if (metaAnnotation != null) {
				return buildSynthesizedAnnotationImpl(anno, metaAnnotation);
			}
		}
		return null;
	}

	public static <T extends Annotation> T getAnnotation(Class<?> clazz, Class<T> annotationClass) {
		T annotation = clazz.getAnnotation(annotationClass);
		if (annotation != null) {
			return annotation;
		}
		for (Annotation anno : clazz.getAnnotations()) {
			T metaAnnotation = anno.annotationType().getAnnotation(annotationClass);
			if (metaAnnotation != null) {
				return buildSynthesizedAnnotationImpl(anno, metaAnnotation);
			}
		}
		return null;
	}

	public static <T extends Annotation> Map<String, Object> getAnnoAttrs(T annotation) {
		Map<String, Object> attrs = new LinkedHashMap<>();
		// 假设 proxy 是通过你代码中 buildSynthesizedAnnotationImpl() 创建的代理对象
		if (Proxy.isProxyClass(annotation.getClass())) {
			InvocationHandler handler = Proxy.getInvocationHandler(annotation);
			if (handler instanceof AnnoUtil.AnnotationInvocationHandler<?, ?> annoHandler) {
				// 获取被代理的原始注解对象
				Annotation originalAnno = annoHandler.childAnnotation();
				Annotation metaAnnotation = annoHandler.metaAnnotation();

				extractAnnotationAttributes(originalAnno, attrs);
				extractAnnotationAttributes(metaAnnotation, attrs);
			}
		} else {
			extractAnnotationAttributes(annotation, attrs);
		}
		return attrs;
	}

	private static void extractAnnotationAttributes(Annotation annotation, Map<String, Object> attrs) {
		Class<? extends Annotation> annotationType = annotation.annotationType();

		Arrays.stream(annotationType.getDeclaredMethods()).forEach(method -> {
			try {
				AliasFor aliasFor = method.getAnnotation(AliasFor.class);
				String targetAttribute = method.getName();
				Class<? extends Annotation> targetAnnotationType = annotationType;

				if (aliasFor != null) {
					targetAttribute = aliasFor.attribute().isEmpty() ? method.getName() : aliasFor.attribute();
					targetAnnotationType = aliasFor.annotation() == Annotation.class ? annotationType : aliasFor.annotation();
				}

				Object value = method.invoke(annotation);

				// 当前注解属性优先处理
				if (value != null || !attrs.containsKey(targetAttribute)) {
					attrs.put(targetAttribute, value);
				}

				// 元注解属性补充处理
				if (targetAnnotationType != annotationType) {
					Annotation metaAnnotation = annotationType.getAnnotation(targetAnnotationType);
					if (metaAnnotation != null) {
						Map<String, Object> metaAttrs = new LinkedHashMap<>();
						extractAnnotationAttributes(metaAnnotation, metaAttrs);
						metaAttrs.forEach((k, v) -> {
							if (!attrs.containsKey(k)) {
								attrs.put(k, v);
							}
						});
					}
				}
			} catch (Exception e) {
				throw new RuntimeException("Failed to extract annotation attributes", e);
			}
		});
	}


	@SuppressWarnings("unchecked")
	private static <T extends Annotation> T buildSynthesizedAnnotationImpl(Annotation childAnnotation, T metaAnnotation) {
		return (T) Proxy.newProxyInstance(
				childAnnotation.annotationType().getClassLoader(),
				new Class<?>[]{metaAnnotation.annotationType()},
				new AnnotationInvocationHandler<>(childAnnotation, metaAnnotation));
	}

	// 辅助处理类（推荐封装为内部类）
	private record AnnotationInvocationHandler<A extends Annotation, M extends Annotation>
			(A childAnnotation, M metaAnnotation) implements InvocationHandler {

		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			// 排除默认方法和Object方法
			if (method.getDeclaringClass() == Object.class) {
				return method.invoke(this, args);
			}

			try {
				// 1. 优先获取子注解的属性值
				Method childMethod = childAnnotation.annotationType()
						.getMethod(method.getName());
				Object value = childMethod.invoke(childAnnotation);
				if (value != null) {
					return value;
				}
			} catch (Exception ignored) {
				// 若子注解无该属性则忽略
			}

			// 2. 尝试获取元注解的默认值
			try {
				Method metaMethod = metaAnnotation.annotationType()
						.getMethod(method.getName());
				return metaMethod.invoke(metaAnnotation);
			} catch (Exception e) {
				throw new RuntimeException("元注解属性获取失败", e);
			}
		}
	}
}
