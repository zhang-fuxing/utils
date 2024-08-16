package com.zhangfuxing.tools.serial;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/8/16
 * @email zhangfuxing1010@163.com
 */
public class FileSerializeProvider<T extends Serializable> implements SerializeProvider<T, File> {

    private File file;

    public FileSerializeProvider() {
    }

    public FileSerializeProvider(File file) {
        Objects.requireNonNull(file);
        this.file = file;
    }

    @Override
    public File serialize(T serializeObj) {
        check(false);
        try (FileOutputStream fileOut = new FileOutputStream(file);
             ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
            out.writeObject(serializeObj);
            return file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public T deserialize(File serializeObj) {
        check(true);
        try (FileInputStream fileIn = new FileInputStream(file);
             ObjectInputStream in = new ObjectInputStream(fileIn)) {
            return (T) in.readObject();
        } catch (IOException e) {
            throw new RuntimeException("反序列化失败，I/O错误", e);
        } catch (ClassNotFoundException c) {
            throw new RuntimeException("未找到反序列化需要的类对象", c);
        }
    }

    private void check(boolean isRead) {
        if (file == null) throw new IllegalArgumentException("未指定序列化/反序列化使用的文件");
        if (isRead && !file.exists()) throw new IllegalArgumentException("无法对不存在的文件进行反序列化");
        if (isRead && file.isDirectory())
            throw new IllegalArgumentException("该文件不是一个有效的文件，无法反序列化，请指定一个文件");
        if (!isRead && !file.exists()) {
            try {
                Files.createDirectories(Path.of(file.getParent()));
            } catch (IOException e) {
                throw new RuntimeException("文件夹创建失败", e);
            }
        }
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        Objects.requireNonNull(file);
        this.file = file;
    }

    public void setFile(String filePath) {
        Objects.requireNonNull(filePath);
        this.file = new File(filePath);
    }

    public void setFile(String parent, String name) {
        Objects.requireNonNull(parent);
        Objects.requireNonNull(name);
        this.file = new File(parent, name);
    }

    public void setFile(Path path) {
        Objects.requireNonNull(path);
        this.file = path.toFile();
    }

    @Override
    public void setResource(File resource) {
        this.file = resource;
    }
}
