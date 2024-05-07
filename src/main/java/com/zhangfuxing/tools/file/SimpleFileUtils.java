package com.zhangfuxing.tools.file;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.util.regex.Pattern;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/5/6
 * @email zhangfuxing1010@163.com
 */
public class SimpleFileUtils {
    public static final Character[] fileNameStdChar = {'\\', '/', ':', '*', '?', '"', '<', '>', '|'};

    /**
     * 检查文件名是否合法
     *
     * @param fileName 文件名称
     * @return true合法，false不合法
     */
    public static boolean isStdName(String fileName) {
        Pattern compile = Pattern.compile("[\\\\/:*?\"<>|]");
        return !compile.matcher(fileName).find();
    }

    public static String toStdName(String fileName) {
        return toStdName(fileName, '_');
    }

    public static String toStdName(String fileName, char replaceChar) {
        if (isStdName(fileName)) {
            return fileName;
        }
        for (Character c : fileNameStdChar) {
            fileName = fileName.replace(c, replaceChar);
        }
        return fileName;
    }

    public static void mv(String source, String target) throws IOException {
        Files.move(Paths.get(source), Paths.get(target));
    }

    public static void cp(String source, String target) throws IOException {
        Files.copy(Paths.get(source), Paths.get(target));
    }

    public static void rm(String path) throws IOException {
        Files.delete(Paths.get(path));
    }

    public static void mkdir(String path) throws IOException {
        Files.createDirectories(Paths.get(path));
    }

    public static RandomAccessFile getRandomAccessFile(String path, String mode) throws IOException {
        return new RandomAccessFile(path, mode);
    }

    public static FileChannel getChannel(String path, OpenOption... options) throws IOException {
        return FileChannel.open(Paths.get(path), options);
    }

    public static void closeChannel(FileChannel... channel) throws IOException {
        for (FileChannel c : channel) {
            if (c != null) {
                c.close();
            }
        }
    }

}
