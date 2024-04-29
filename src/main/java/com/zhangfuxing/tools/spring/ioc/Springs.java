package com.zhangfuxing.tools.spring.ioc;

import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/4/28
 * @email zhangfuxing1010@163.com
 */
public class Springs {

    private static final String listenerPath = "META-INF/spring.listener";
    private Class<?>[] applicationListener;
    private String[] args;
    private static ConfigurableApplicationContext context;

    public static Springs begin() {
        return new Springs();
    }

    @SafeVarargs
    public final Springs addListener(@SuppressWarnings("rawtypes") Class<? extends ApplicationListener>... listener) {
        if (this.applicationListener == null) {
            applicationListener = listener;
        } else {
            Class<?>[] newListener = new Class[applicationListener.length + listener.length];
            System.arraycopy(applicationListener, 0, newListener, 0, applicationListener.length);
            System.arraycopy(listener, 0, newListener, applicationListener.length, listener.length);
            applicationListener = newListener;
        }
        return this;
    }

    public Springs addArgs(String... args) {
        this.args = args;
        return this;
    }

    public ConfigurableApplicationContext start(Class<?> mainClass) {
        return loadcontext(mainClass, applicationListener, args);
    }

    public static ConfigurableApplicationContext start(Class<?> mainClass, String... args) {
        return start(mainClass, null, args);
    }

    public static ConfigurableApplicationContext start(Class<?> mainClass, Class<?>[] applicationListener, String[] args) {
        return loadcontext(mainClass, applicationListener, args);
    }


    public static ConfigurableApplicationContext load(Class<?> classes) {
        return new AnnotationConfigApplicationContext(classes);
    }

    public static ConfigurableApplicationContext load(String... basePackages) {
        return new AnnotationConfigApplicationContext(basePackages);
    }

    private static ConfigurableApplicationContext loadcontext(Class<?> mainClass, Class<?>[] applicationListener, String[] args) {
        var context = new AnnotationConfigApplicationContext();
        Spring annotation = mainClass.getAnnotation(Spring.class);
        Class<?>[] value = annotation.value();
        String[] scanPackages = annotation.scanPackages();
        context.scan(mainClass.getPackageName());
        if (value.length > 0) {
            context.register(value);
        }
        if (scanPackages.length > 0) {
            context.scan(scanPackages);
        }
        addApplicationListener(applicationListener, context);
        context.refresh();
        Springs.context = context;
        return context;
    }

    public static ConfigurableApplicationContext getContext() {
        if (Springs.context == null) {
            throw new RuntimeException("Spring context is not initialized");
        }
        return Springs.context;
    }

    private static void addApplicationListener(Class<?>[] applicationListener, AnnotationConfigApplicationContext context) {
        try {
            // 读取类路径下META-INF/spring.listener文件
            ClassPathResource resource = new ClassPathResource(listenerPath);
            if (resource.exists()) {
                try (var br = new BufferedReader(new FileReader(resource.getFile()))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        line = line.trim();
                        if (line.startsWith("#") || line.startsWith("//")) {
                            continue;
                        }

                        Object o = Class.forName(line)
                                .getDeclaredConstructor()
                                .newInstance();
                        if (o instanceof ApplicationListener<?> listener) {
                            context.addApplicationListener(listener);
                        }
                    }
                }
            }
            if (applicationListener != null) {
                for (Class<?> listenerClass : applicationListener) {
                    Object o = listenerClass.getDeclaredConstructor().newInstance();
                    if (o instanceof ApplicationListener<?> listener) {
                        context.addApplicationListener(listener);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
