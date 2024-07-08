package com.zhangfuxing.tools.file;

import com.zhangfuxing.tools.util.Str;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/5/6
 * @email zhangfuxing1010@163.com
 */
public class Fs {
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
        Path sourcePath = Paths.get(source);
        Path targetPath = Paths.get(target);
        checkBiParam(source, target);
        Files.move(sourcePath, targetPath);
    }

    public static void cp(String source, String target) throws IOException {
        Path sourcePath = Paths.get(source);
        Path targetPath = Paths.get(target);
        checkBiParam(source, target);
        Files.copy(sourcePath, targetPath);
    }

    public static void rm(String path) throws IOException {
        Objects.requireNonNull(path);
        Files.delete(Paths.get(path));
    }

    public static void mkdir(String path) throws IOException {
        Objects.requireNonNull(path);
        Path workPath = Paths.get(path);
        if (Files.exists(workPath)) {
            return;
        }
        Files.createDirectories(workPath);
    }

    public static List<File> find(String findPath, FileVisitOption... options) throws IOException {
        return find(findPath, Integer.MAX_VALUE, options);
    }

    public static List<File> find(String findPath, int depth, FileVisitOption... options) throws IOException {
        return find(findPath, depth, file -> true, options);
    }

    public static List<File> find(String findPath, Predicate<File> matchRole, FileVisitOption... options) throws IOException {
        return find(findPath, Integer.MAX_VALUE, matchRole, options);
    }

    public static List<File> find(String findPath, int depth, Predicate<File> matchRole, FileVisitOption... options) throws IOException {
        if (Str.isBlank(findPath)) {
            throw new IllegalArgumentException("未指定查找路径");
        }
        Stream<Path> walk = Files.walk(Path.of(findPath), depth, options);
        try (walk) {
            return walk.map(Path::toFile)
                    .filter(matchRole)
                    .collect(Collectors.toList());
        }
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

    public static String formatSize(long dataSize, DataSizeUnit unit) {
        return BigDecimal.valueOf((double) dataSize / unit.unitSize).setScale(2, RoundingMode.HALF_UP) + " " + unit.name();
    }

    public static String formatSize(long dataSize) {
        DataSizeUnit autoUnit = DataSizeUnit.auto(dataSize);
        return formatSize(dataSize, autoUnit);
    }

    public static long parseSize(String sizeText) {
        if (sizeText == null || sizeText.isBlank()) return 0L;
        DataSizeUnit unit = DataSizeUnit.parseUnit(sizeText);
        return 1L;
    }

    private static void checkBiParam(String source, String target) throws FileNotFoundException {
        Objects.requireNonNull(source);
        Objects.requireNonNull(target);
        if (!Files.exists(Path.of(source))) {
            throw new FileNotFoundException("未找到文件：" + source);
        }
    }
}
