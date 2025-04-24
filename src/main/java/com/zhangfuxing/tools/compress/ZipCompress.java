package com.zhangfuxing.tools.compress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2025/4/16
 * @email zhangfuxing1010@163.com
 */
public class ZipCompress {
	private static final Logger log = LoggerFactory.getLogger(ZipCompress.class);
	private static final int DEFAULT_BUFFER_SIZE = 64 * 1024; // 64KB缓冲区
	private static final int DEFAULT_COMPRESS_LEVEL = Deflater.BEST_SPEED;


	ZipOutputStream zos;
	byte[] buffer;


	public ZipCompress() {
		this.buffer = new byte[DEFAULT_BUFFER_SIZE];
	}


	public ZipCompress setOutputStream(OutputStream outputStream) {
		this.zos = new ZipOutputStream(outputStream);
		this.zos.setLevel(DEFAULT_COMPRESS_LEVEL);
		return this;
	}


	public ZipCompress setBuffer(int bufferSize) {
		this.buffer = new byte[bufferSize];
		return this;
	}

	public ZipCompress addFile(File... files) {
		for (File file : files) {
			addFile(file);
		}
		return this;
	}

	public ZipCompress addDirectory(File directory, Predicate<File> matchRole) {
		if (directory == null || !directory.exists() || !directory.isDirectory()) {
			throw new IllegalArgumentException("directory 不存在");
		}
		try {
			compressDirectory(directory, matchRole, zos);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return this;
	}


	public void close() {
		try {
			if (zos != null) {
				zos.close();
			}
		} catch (Exception e) {
			throw new RuntimeException("关闭资源失败", e);
		}
	}

	private void addFile(File file) {
		if (file == null || !file.exists() || !file.isFile()) {
			throw new IllegalArgumentException("file 不存在");
		}
		try {
			log.info("压缩文件：{}", file.getAbsolutePath());
			this.zos.putNextEntry(new ZipEntry(file.getName()));
			try (FileInputStream fis = new FileInputStream(file)) {
				int bytesRead;
				while ((bytesRead = fis.read(buffer)) != -1) {
					this.zos.write(buffer, 0, bytesRead);
				}
			}
			this.zos.closeEntry();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void compressDirectory(File rootDir, Predicate<File> matchRole, ZipOutputStream zos) throws IOException {
		/*
				File[] files = currentDir.listFiles();
		if (files == null) {
			return;
		}
		for (File file : files) {
			if (file.isDirectory()) {
				// 递归处理子目录
				compressDirectory(rootDir, file, matchRole, zos);
			} else {
				if (matchRole != null && !matchRole.test(file)) {
					continue;
				}
				// 计算相对路径
				String relativePath = rootDir.toPath().relativize(file.toPath()).toString();
				log.info("压缩文件：{}", relativePath);

				// 创建ZIP条目时使用相对路径
				ZipEntry zipEntry = new ZipEntry(relativePath);
				zos.putNextEntry(zipEntry);

				// 写入文件内容
				try (FileInputStream fis = new FileInputStream(file)) {
					int length;
					while ((length = fis.read(buffer)) > 0) {
						zos.write(buffer, 0, length);
					}
				}
				zos.closeEntry();
			}
		}
		*/
		for (File file : collectFiles(rootDir, matchRole)) {
			// 计算相对路径
			String relativePath = rootDir.toPath().relativize(file.toPath()).toString();
			log.info("压缩文件：{}", relativePath);

			// 创建ZIP条目时使用相对路径
			ZipEntry zipEntry = new ZipEntry(relativePath);
			zos.putNextEntry(zipEntry);

			// 写入文件内容
			try (FileInputStream fis = new FileInputStream(file)) {
				int length;
				while ((length = fis.read(buffer)) > 0) {
					zos.write(buffer, 0, length);
				}
			}
			zos.closeEntry();
		}
	}

	private List<File> collectFiles(File directory, Predicate<File> matchRole) throws IOException {
		List<File> fileList = new ArrayList<>();
		try (Stream<Path> fileStream = Files.walk(directory.toPath())) {
			fileStream
					.filter(p -> !Files.isDirectory(p))
					.map(Path::toFile)
					.filter(file -> matchRole == null || matchRole.test(file))
					.forEach(fileList::add);
		}

		return fileList;
	}


}
