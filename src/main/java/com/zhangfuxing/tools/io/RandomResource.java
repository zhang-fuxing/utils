package com.zhangfuxing.tools.io;



import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/8/1
 * @email zhangfuxing1010@163.com
 */
public class RandomResource implements Closeable{
    File file;
    RandomAccessFile raf;
    Consumer<Exception> exceptionHandler;
    // 4KB 1024*1024*4
    int bufferSize = 1 << 12;

    private RandomResource() {
        exceptionHandler = e -> {};
    }
    public static RandomResource create(File file, Model model) {
        return create(file, model, null);
    }
    public static RandomResource create(File file, Model model, Consumer<Exception> exceptionHandler) {
        RandomResource this_ = new RandomResource();
        Objects.requireNonNull(file);
        Objects.requireNonNull(model);
        if (exceptionHandler != null) {
            this_.exceptionHandler = exceptionHandler;
        }
        try {
            this_.file = file;
            this_.raf = new RandomAccessFile(this_.file, model.name().toLowerCase());
        } catch (FileNotFoundException e) {
            this_.exceptionHandler.accept(e);
        }
        return this_;
    }

    public RandomResource readAll(OutputStream out, boolean close) {
        Objects.requireNonNull(out);
        try {
            raf.seek(0);
            int len;
            byte[] buffer = new byte[bufferSize];
            while ((len = raf.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            out.flush();
        } catch (Exception e) {
            exceptionHandler.accept(e);
        } finally {
            if (close) {
                close(out);
            }
        }
        return this;
    }

    private void close(Closeable out) {
        try (out) {
        } catch (IOException e) {
            exceptionHandler.accept(e);
        }
    }

    public RandomResource readAll(OutputStream out) {
        return readAll(out, false);
    }

    /**
     * 将文件切割为小块进行输出
     *
     * @param out       输出对象
     * @param chunkSize 每一块的大小
     * @return 当前对象
     */
    public RandomResource readChunk(OutputStream out, long chunkSize) {
        Objects.requireNonNull(out, "out cannot be null");
        Objects.requireNonNull(raf, "RandomAccessFile not initialized");
        try {
            long fileLength = file.length();
            long remaining = fileLength;
            long start = 0;

            while (remaining > 0) {
                long end = Math.min(start + chunkSize, fileLength);
                readChunk(out, start, end - start);
                start = end;
                remaining -= chunkSize;
                if (remaining < chunkSize) {
                    chunkSize = remaining; // 最后一个块可能小于 chunkSize
                }
            }
        } catch (Exception e) {
            exceptionHandler.accept(e);
        }

        return this;
    }


    public RandomResource readChunk(OutputStream out, long start, long end) {
        Objects.requireNonNull(out);
        try {
            long length = file.length();
            if (length < start) {
                throw new IndexOutOfBoundsException("开始位置超出文件长度：start-%d, fileLen-%d".formatted(start, length));
            }
            if (length < end) {
                throw new IndexOutOfBoundsException("结束位置超出文件长度：end-%d, fileLen-%d".formatted(end, length));
            }
            raf.seek(start);
            // 读取 start - end 的文件数据
            long readSize = end - start;
            int bufferSize = (int) Math.min(this.bufferSize, readSize); // 缓冲区大小不超过剩余需要读取的字节数
            byte[] buffer = new byte[bufferSize];

            long bytesRead = 0;
            while (bytesRead < readSize) {
                // 每次读取的字节数
                int toRead = (int) Math.min(bufferSize, readSize - bytesRead);
                int len = raf.read(buffer, 0, toRead);
                if (len == -1) { // 理论上不应该发生，除非文件在读取过程中被截断
                    throw new IOException("Unexpected end of file while reading chunk");
                }
                out.write(buffer, 0, len);
                bytesRead += len;
            }

            out.flush(); // 确保所有数据都被写出

        } catch (Exception e) {
            exceptionHandler.accept(e);
        }

        return this;
    }

    public void readChunkByChannel(OutputStream out, long start, long end) {
        Objects.requireNonNull(out);
        try (FileChannel channel = raf.getChannel();
             var bos = new BufferedOutputStream(out)) {
            long length = file.length();
            if (length < start) {
                throw new IndexOutOfBoundsException("开始位置超出文件长度：start-%d, fileLen-%d".formatted(start, length));
            }
            if (length < end) {
                throw new IndexOutOfBoundsException("结束位置超出文件长度：end-%d, fileLen-%d".formatted(end, length));
            }

            // 确保文件指针移动到正确的起始位置
            channel.position(start);

            // 读取 start - end 的文件数据
            long readSize = end - start;
            // 4GB
            if (readSize >= (1L << 32)) {
                this.bufferSize = 1 << 13;
            }
            int bufferSize = (int) Math.min(this.bufferSize, readSize); // 缓冲区大小不超过剩余需要读取的字节数
            ByteBuffer buffer = ByteBuffer.allocate(bufferSize);

            long bytesRead = 0;
            while (bytesRead < readSize) {
                // 每次读取的字节数
                int toRead = (int) Math.min(bufferSize, readSize - bytesRead);
                buffer.clear(); // 清空缓冲区
                buffer.limit(toRead); // 设置缓冲区的限制
                int len = channel.read(buffer);
                if (len == -1) { // 理论上不应该发生，除非文件在读取过程中被截断
                    exceptionHandler.accept(new IOException("Unexpected end of file while reading chunk"));
                    break;
                }
                buffer.flip(); // 准备写出数据
                bos.write(buffer.array(), 0, len);
                bytesRead += len;
            }

            bos.flush(); // 确保所有数据都被写出

        } catch (Exception e) {
            exceptionHandler.accept(e);
        }
    }

    public void readAllByChannel(OutputStream out, boolean close) {
        Objects.requireNonNull(out);
        try (FileChannel channel = raf.getChannel();
             var bos = new BufferedOutputStream(out)) {
            // 4GB
            if (this.file.length() >= (1L << 32)) {
                this.bufferSize = 1 << 13;
            }
            ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
            while (channel.read(buffer) != -1) {
                buffer.flip();
                bos.write(buffer.array(), 0, buffer.limit());
                buffer.clear();
            }
            bos.flush();
        } catch (Exception e) {
            exceptionHandler.accept(e);
        } finally {
            if (close) {
                close(out);
            }
        }
    }


    @Override
    public void close() throws IOException {
        close(raf);
    }
}
