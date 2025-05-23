package com.zhangfuxing.tools.file;


import com.zhangfuxing.tools.io.ReadResource;
import com.zhangfuxing.tools.util.ITools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;


/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/6/18
 * @email zhangfuxing1010@163.com
 */
public class FileLoadUtil {
    private static final Logger log = LoggerFactory.getLogger(FileLoadUtil.class);
    public static final String classpath = "classpath:";
    // 查找文件路径，按顺序查找
    static String[] findPath;
    public static final String loadConfirmFile = "confirm:";

    static {
        init();
    }

    public static void setFindPath(String... dirs) {
        findPath = dirs;
    }

    public static void appendTailFindPath(String... dirs) {
        String[] temp = new String[dirs.length + findPath.length];
        System.arraycopy(findPath, 0, temp, 0, findPath.length);
        System.arraycopy(dirs, 0, temp, findPath.length, dirs.length);
        findPath = temp;
    }

    public static void appendHeadFindPath(String... dirs) {
        int len = dirs.length + findPath.length;
        String[] temp = new String[len];
        System.arraycopy(dirs, 0, temp, 0, dirs.length);
        System.arraycopy(findPath, 0, temp, dirs.length, findPath.length);
        findPath = temp;
    }

    public static void resetFindPath() {
        init();
    }



    public static InputStream load(String filepath, AtomicReference<String> atomicString, String... findOrder) throws FileNotFoundException {
        if (findOrder.length == 0) {
            return ReadResource.load(filepath, true).readStream();
        }
        InputStream inputStream = null;

        for (String findPath : findOrder) {
            // 跳过为空的查找路径
            if (findPath == null) {
                continue;
            }
            // 在类路径下查找文件
            if (findPath.equalsIgnoreCase(classpath)) {
                try {
                    inputStream = ReadResource.load(filepath, true).readStream();
                    Optional.ofNullable(atomicString).ifPresent(a -> a.set(findPath + filepath));
                    break;
                } catch (Exception e) {
                    continue;
                }
            }

            // 指定了一个确定的文件，直接加载该文件
            if (findPath.startsWith(loadConfirmFile)) {
                String fileLocation = findPath.substring(loadConfirmFile.length());
                // 判断是否是从类路径加载指定文件
                if (fileLocation.startsWith(classpath)) {
                    Optional.ofNullable(atomicString).ifPresent(a -> a.set(fileLocation));

                    // 删除 classpath: 前缀，获取真实类路径下的文件路径
                    var tempFileLocation = fileLocation.substring(classpath.length());
                    inputStream = ReadResource.load(tempFileLocation, true).readStream();
                    break;
                }
                // 不是从类路径加载，直接获取文件流
                inputStream = new FileInputStream(fileLocation);
                Optional.ofNullable(atomicString).ifPresent(a -> a.set(findPath));
                break;
            }


            File file = new File(findPath, filepath);
            if (file.exists() && file.isFile()) {
                inputStream = new FileInputStream(file);
                Optional.ofNullable(atomicString).ifPresent(a -> a.set(ITools.StrTools.buildURL(findPath, filepath)));
                break;
            }

        }
        if (inputStream == null) {
            throw new FileNotFoundException("未找到文件：%s, 查找路径：%s".formatted(filepath, Arrays.toString(findOrder)));
        }
        return inputStream;
    }

    public static InputStream loadFile(String filepath, String location) throws FileNotFoundException {
        return load(filepath, null, ITools.StrTools.isBlank(location) ? findPath : new String[]{location});
    }

    public static InputStream loadFile(String filepath, AtomicReference<String> loadPath, String location) throws FileNotFoundException {
        return load(filepath, loadPath, ITools.StrTools.isBlank(location) ? findPath : new String[]{location});
    }

    public static InputStream loadFile(String filepath) throws FileNotFoundException {
        return loadFile(filepath, null);
    }

    public static String getLoadFilePath(String filepath, String location) {
        AtomicReference<String> atomicString = new AtomicReference<>("");
        try (InputStream inputStream = load(filepath, atomicString, ITools.StrTools.isBlank(location) ? findPath : new String[]{location})) {
            return atomicString.get();
        } catch (IOException e) {
            log.error("加载文件失败，msg={}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private static void init() {
        findPath = new String[]{"./config", "./", classpath};
    }

}
