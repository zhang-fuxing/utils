package com.zhangfuxing.tools.handler;

import com.zhangfuxing.tools.handler.example.DbType;
import com.zhangfuxing.tools.handler.example.PagedHandler;

import java.io.File;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.JarFile;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2025/1/6
 * @email zhangfuxing1010@163.com
 */
public class AutoHandlerProcess {
	public static void main(String[] args) {
		PagedHandler process = process(PagedHandler.class);
		String sql = "select * from UserInfo";
		DbType dbType = DbType.Kingbase;
		if (process != null) {
			process.forEachBreakable((pagedHandler -> {
				if (!pagedHandler.support(dbType)) {
					return true;
				}
				System.out.println(pagedHandler.addPages(sql, 0, 20));
				return false;
			}));
		}
	}

	@SuppressWarnings("unchecked")
	public static <T extends AutoHandlerSupport<T>> T process(Class<T> clazz) {
		// 判断 clazz 是不是接口并且是不是 AutoHandlerSupport 的子类
		if (!clazz.isInterface() || !AutoHandlerSupport.class.isAssignableFrom(clazz)) {
			return null;
		}
		// 获取类路径下所有携带 AutoHandler 注解的类对象
		// 获取所有带有 AutoHandler 注解的类
		Set<Class<?>> classes = new HashSet<>();
		try {
			// 获取类加载器
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			// 获取所有资源
			// 只扫描当前项目的包
			String basePackage = clazz.getPackage().getName();
			String path = basePackage.replace('.', '/');
			Enumeration<URL> resources = classLoader.getResources(path);
			while (resources.hasMoreElements()) {
				URL resource = resources.nextElement();
				// 获取资源路径
				String protocol = resource.getProtocol();
				if ("file".equals(protocol)) {
					// 文件系统中的类
					String filePath = URLDecoder.decode(resource.getFile(), StandardCharsets.UTF_8);
					findClassesInPath(new File(filePath), basePackage, classes);
				} else if ("jar".equals(protocol)) {
					// JAR包中的类
					JarURLConnection connection = (JarURLConnection) resource.openConnection();
						JarFile jarFile = connection.getJarFile();
					findClassesInJar(jarFile, basePackage, classes);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 过滤出带有 AutoHandler 注解的类
		List<Class<?>> handlerClasses = classes.stream()
				.filter(cls -> cls.isAnnotationPresent(AutoHandler.class))
				.filter(clazz::isAssignableFrom)
				.sorted(Comparator.comparingInt(cls -> cls.getAnnotation(AutoHandler.class).order()))
				.toList();
		// 创建实例
		// 如果没有处理器类,返回null
		if (handlerClasses.isEmpty()) {
			return null;
		}

		// 创建所有处理器实例
		List<AutoHandlerSupport<?>> handlers = new ArrayList<>();
		for (Class<?> handlerClass : handlerClasses) {
			try {
				AutoHandlerSupport<?> handler = (AutoHandlerSupport<?>) handlerClass.getDeclaredConstructor().newInstance();
				handlers.add(handler);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// 如果没有成功创建任何处理器实例,返回null
		if (handlers.isEmpty()) {
			return null;
		}

		// 连接处理器链
		for (int i = 0; i < handlers.size() - 1; i++) {
			var support = (T) handlers.get(i + 1);
			((T) handlers.get(i)).setNext(support);
		}

		// 返回第一个处理器实例
		return (T) handlers.get(0);

	}

	private static void findClassesInPath(File directory, String packageName, Set<Class<?>> classes) {
		File[] files = directory.listFiles();
		if (files == null) return;

		for (File file : files) {
			String fileName = file.getName();
			if (file.isDirectory()) {
				findClassesInPath(file, packageName + fileName + ".", classes);
			} else if (fileName.endsWith(".class")) {
				try {
					String className = packageName + "." + fileName.substring(0, fileName.length() - 6);
					Class<?> cls = Class.forName(className);
					classes.add(cls);
				} catch (ClassNotFoundException ignored) {
					// 忽略无法加载的类
				}
			}
		}
	}

	private static void findClassesInJar(JarFile jarFile, String basePackage, Set<Class<?>> classes) {
		String pathPrefix = basePackage.replace('.', '/') + "/";
		jarFile.stream()
				.filter(entry -> entry.getName().endsWith(".class") 
						&& entry.getName().startsWith(pathPrefix))
				.forEach(entry -> {
					String className = entry.getName()
							.replace('/', '.')
							.substring(0, entry.getName().length() - 6);
					try {
						Class<?> cls = Class.forName(className);
						classes.add(cls);
					} catch (ClassNotFoundException ignored) {
						// 忽略无法加载的类
					}
				});
	}
}
