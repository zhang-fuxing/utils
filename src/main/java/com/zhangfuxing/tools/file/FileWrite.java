package com.zhangfuxing.tools.file;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 字符文件写入工具类
 *
 * @author 张福兴
 * @version 1.0
 * @date 2025/5/19
 * @email zhangfuxing1010@163.com
 */
@SuppressWarnings("NullableProblems")
public class FileWrite extends Writer implements AutoCloseable{
	private final String path;
	private final Charset charset;
	private final boolean append;
	private final BufferedWriter writer;

	public FileWrite(String path) throws FileNotFoundException {
		this(path, StandardCharsets.UTF_8, false);
	}

	public FileWrite(String path, Charset charset) throws FileNotFoundException {
		this(path, charset, false);
	}

	public FileWrite(String path, Charset charset, boolean append) throws FileNotFoundException {
		this.path = path;
		this.charset = charset;
		this.append = append;
		writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path, append), charset));
	}

	public static void write(String content, String path, Charset charset, boolean append) throws IOException {
		Path filePath = Paths.get(path);
		File file = filePath.getParent().toFile();
		if (!file.exists()) {
			Files.createDirectories(filePath.getParent());
		}
		Files.createFile(filePath);
		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath.toFile(), append), charset))) {
			writer.write(content);
		}
	}

	public static void write(String content, String path, Charset charset) throws IOException {
		write(content, path, charset, false);
	}

	public static void write(String content, String path, boolean append) throws IOException {
		write(content, path, StandardCharsets.UTF_8, append);
	}

	public static void write(String content, String path) throws IOException {
		write(content, path, false);
	}

	public static void append(String content, String path) throws IOException {
		write(content, path, true);
	}

	public static void append(String content, String path, Charset charset) throws IOException {
		write(content, path, charset, true);
	}

	public void writeLine(String content) throws IOException {
		this.writer.write(content);
		this.writer.newLine();
	}


	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		this.writer.write(cbuf, off, len);
	}

	@Override
	public void write(int c) throws IOException {
		this.writer.write(c);
	}

	@Override
	public void write(char[] cbuf) throws IOException {
		this.writer.write(cbuf);
	}

	@Override
	public void write(String str) throws IOException {
		this.writer.write(str);
	}

	@Override
	public void write(String str, int off, int len) throws IOException {
		this.writer.write(str, off, len);
	}

	@Override
	public Writer append(CharSequence csq) throws IOException {
		return this.writer.append(csq);
	}

	@Override
	public Writer append(CharSequence csq, int start, int end) throws IOException {
		return this.writer.append(csq, start, end);
	}

	@Override
	public Writer append(char c) throws IOException {
		return this.writer.append(c);
	}

	public void  flush() throws IOException {
		writer.flush();
	}

	@Override
	public void close() throws IOException {
		if (this.writer != null) {
			flush();
			this.writer.close();
		}
	}
}
