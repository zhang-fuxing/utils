package com.zhangfuxing.tools.spring.ioc;

import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

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
    private static ApplicationListener<?>[] applicationListenerInstance;

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

    public static ConfigurableApplicationContext start(Class<?> mainClass, String[] args) {
        return loadcontext(mainClass, null, args);
    }

    public static ConfigurableApplicationContext start(Class<?> mainClass, String[] args, Class<?>... applicationListener) {
        return loadcontext(mainClass, applicationListener, args);
    }

    public static ConfigurableApplicationContext start(Class<?> mainClass, String[] args,ApplicationListener<?>... applicationListener) {
        Springs.applicationListenerInstance = applicationListener;
        return loadcontext(mainClass, null, args);
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
            InputStream resourceAsStream = Springs.class.getClassLoader().getResourceAsStream(listenerPath);
            if (resourceAsStream != null) {
                try (var br = new BufferedReader(new InputStreamReader(resourceAsStream))) {
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
            if (Springs.applicationListenerInstance != null) {
                for (ApplicationListener<?> listener : Springs.applicationListenerInstance) {
                    context.addApplicationListener(listener);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
