package com.zhangfuxing.tools.compress;

import java.io.File;
import java.io.OutputStream;
import java.util.function.Predicate;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2025/4/16
 * @email zhangfuxing1010@163.com
 */
public class ZipUtil {

	public static void zipFile(OutputStream outputStream, File... files) {
		new ZipCompress()
				.setOutputStream(outputStream)
				.addFile(files)
				.close();
	}

	public static void zipDir(OutputStream outputStream, File directory, Predicate<File> matchRole) {
		new ZipCompress()
				.setOutputStream(outputStream)
				.addDirectory(directory, matchRole)
				.close();
	}

	public static void zipDir(OutputStream outputStream, File directory) {
		new ZipCompress()
				.setOutputStream(outputStream)
				.addDirectory(directory, file -> true)
				.close();
	}
}
