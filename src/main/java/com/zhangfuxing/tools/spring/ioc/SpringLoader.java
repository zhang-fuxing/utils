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
public class SpringLoader  {

    private static final String listenerPath = "META-INF/spring.listener";
    private static ConfigurableApplicationContext context;
    private static ApplicationListener<?>[] applicationListenerInstance;


    public static ConfigurableApplicationContext load(Class<?> mainClass, String[] args) {
        return loadContext(mainClass, null, args);
    }

    public static ConfigurableApplicationContext load(Class<?> mainClass, String[] args, Class<?>... applicationListener) {
        return loadContext(mainClass, applicationListener, args);
    }

    public static ConfigurableApplicationContext load(Class<?> mainClass, String[] args, ApplicationListener<?>... applicationListener) {
        SpringLoader.applicationListenerInstance = applicationListener;
        return loadContext(mainClass, null, args);
    }



    public static ConfigurableApplicationContext getContext() {
        if (SpringLoader.context == null) {
            throw new RuntimeException("Spring context is not initialized");
        }
        return SpringLoader.context;
    }

    private static void addApplicationListener(Class<?>[] applicationListener, AnnotationConfigApplicationContext context) {
        try {
            // 读取类路径下META-INF/spring.listener文件
            InputStream resourceAsStream = SpringLoader.class.getClassLoader().getResourceAsStream(listenerPath);
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
            if (SpringLoader.applicationListenerInstance != null) {
                for (ApplicationListener<?> listener : SpringLoader.applicationListenerInstance) {
                    context.addApplicationListener(listener);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private static ConfigurableApplicationContext loadContext(Class<?> mainClass, Class<?>[] applicationListener, String[] args) {
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
        SpringLoader.context = context;
        return context;
    }



}
