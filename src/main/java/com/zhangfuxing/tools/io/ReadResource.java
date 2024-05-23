package com.zhangfuxing.tools.io;

import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * 读取资源文件工具类
 *
 * @author zhangfx
 * @version 1.0
 * @date 2024-05-07 17:22:26
 */
@Component
public class ReadResource {
    private boolean readClasspath = true;
    private String filepath;
    private InputStream inputStream;

    private ReadResource() {
    }

    public static ReadResource load(String filepath) {
        return load(filepath, true);
    }

    public static ReadResource load(String filepath, boolean readClasspath) {
        ReadResource reader = new ReadResource();
        reader.readClasspath = readClasspath;
        reader.inputStream = reader.getInputStream();
        reader.filepath = filepath;
        return reader;
    }


    /**
     * 获取资源文件长度
     * @param call 调用函数
     * @return 读取资源对象
     */
    public ReadResource streamLengthCallback(Consumer<Long> call) {
        try {
            int available = this.inputStream.available();
            Optional.ofNullable(call).ifPresent(c -> c.accept((long) available));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * 对资源文件输入流进行操
     *
     * @param call 操作函数
     * @return 读取资源对象
     */
    public ReadResource streamCallback(Consumer<InputStream> call) {
        Optional.ofNullable(call).ifPresent(c -> c.accept(this.inputStream));
        try {
            if (this.inputStream.available() <= 0) {
                this.inputStream = getInputStream();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * 设置资源文件路径,重新设置文件路径后需要调用 reloadStream 重新加载资源
     *
     * @param filepath 资源文件路径
     * @return 读取资源对象
     */
    public ReadResource resetFilePath(String filepath) {
        this.filepath = filepath;
        return this;
    }

    /**
     * 设置是否从classpath读取资源文件
     *
     * @param readClasspath 是否从classpath读取资源文件，默认true
     * @return 读取资源对象
     */
    public ReadResource readByClasspath(boolean readClasspath) {
        this.readClasspath = readClasspath;
        return this;
    }

    /**
     * 重新加载资源文件输入流
     *
     * @return 读取资源对象
     */
    public ReadResource reloadStream() {
        this.inputStream = getInputStream();
        return this;
    }


    /**
     * 将资源文件内容读取为字符串
     *
     * @return 资源文件内容字符串
     */
    public String readText() {
        try (var reader = new BufferedReader(new InputStreamReader(this.inputStream))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将资源文件内容读取为字节数组
     *
     * @return 资源文件内容字节数组
     */
    public byte[] readBytes() {
        try (var reader = new BufferedInputStream(this.inputStream)) {
            return reader.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将资源文件内容读取为字节数组
     *
     * @param len 读取字节数
     * @return 资源文件内容字节数组
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public byte[] readBytes(int len) {
        try (var reader = new BufferedInputStream(this.inputStream)) {
            byte[] bytes = new byte[len];
            reader.read(bytes);
            return bytes;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取资源文件输入流
     *
     * @return 资源文件输入流
     */
    public InputStream readStream() {
        return this.inputStream;
    }

    /**
     * 获取资源文件输入流，并包装为BufferedInputStream
     *
     * @return 资源文件输入流
     */
    public BufferedInputStream readBufferedStream() {
        return new BufferedInputStream(this.inputStream);
    }

    /**
     * 获取资源文件输入流，并包装为BufferedReader
     *
     * @return 资源文件输入流
     */
    public BufferedReader readBufferedReader() {
        return new BufferedReader(new InputStreamReader(this.inputStream));
    }

    /**
     * 将资源文件内容移动到指定文件，如果目标文件存在，抛出异常
     *
     * @param dest 目标文件
     * @throws IOException 如果目标文件存在，抛出异常
     */
    public void moveTo(File dest) throws IOException {
        moveTo(dest, false);
    }

    /**
     * 将资源文件内容移动到指定文件
     *
     * @param dest      目标文件
     * @param overwrite 是否覆盖目标文件
     * @throws IOException 如果目标文件存在，且不允许覆盖，抛出异常
     */
    public void moveTo(File dest, boolean overwrite) throws IOException {
        // 参数检查
        if (dest == null) {
            throw new IllegalArgumentException("dest is null");
        }
        boolean exists = dest.exists();
        if (exists && !overwrite) {
            throw new IllegalArgumentException("dest already exists: " + dest.getAbsolutePath());
        }
        // 如果目标文件存在，并且允许覆盖，则先删除
        if (exists) {
            Files.delete(dest.toPath());
        }

        // 如果目录basePath不存在，则创建
        File parentFile = dest.getParentFile();
        if (!parentFile.exists()) {
            Files.createDirectories(parentFile.toPath());
        }
        moveTo(new BufferedOutputStream(new FileOutputStream(dest)));
    }

    /**
     * 将资源文件内容移动到指定输出流
     *
     * @param outputStream 输出流
     * @param close        是否关闭输出流
     * @throws IOException 如果输出流关闭失败/输出失败，抛出异常
     */
    public void moveTo(OutputStream outputStream, boolean close) throws IOException {
        // 参数检查
        if (outputStream == null) {
            throw new IllegalArgumentException("outputStream is null");
        }
        try (var reader = new BufferedInputStream(this.inputStream)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = reader.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
        } finally {
            if (close) {
                outputStream.close();
            }
        }
    }

    /**
     * 将资源文件内容移动到指定输出流，并关闭输出流
     *
     * @param outputStream 输出流
     * @throws IOException 如果输出流关闭失败/输出失败，抛出异常
     */
    public void moveTo(OutputStream outputStream) throws IOException {
        try (var reader = new BufferedInputStream(this.inputStream);
             outputStream) {
            byte[] buffer = new byte[1024 * 4];
            int len;
            while ((len = reader.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
        }
    }

    /**
     * 获取资源文件输入流
     *
     * @return 资源文件输入流
     */
    private InputStream getInputStream() {
        if (readClasspath) {
            InputStream is = getClass().getClassLoader().getResourceAsStream(filepath);
            if (is == null) {
                throw new IllegalArgumentException("filepath not found: " + filepath);
            }
            return is;
        }
        try {
            return new FileInputStream(filepath);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("filepath not found: " + filepath);
        }
    }
}