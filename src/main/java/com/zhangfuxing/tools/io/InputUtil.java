package com.zhangfuxing.tools.io;


import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/8/26
 * @email zhangfuxing1010@163.com
 */
public class InputUtil {

    public static boolean autoClose = false;
    public static Consumer<Exception> exceptionHandler = e -> {
    };

    public static byte[] readAll(InputStream in) throws IOException {
        try {
            return in.readAllBytes();
        } finally {
            close(in);
        }
    }

    public static InputStream getInputStream(File file) {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<InputStream> getInputStreams(File... files) {
        return Arrays.stream(files)
                .map(InputUtil::getInputStream)
                .collect(Collectors.toList());
    }

    public static List<InputStream> getInputStreams(String... filepaths) {
        return getInputStreams(Arrays.stream(filepaths)
                .map(File::new)
                .toArray(File[]::new));
    }

    public static InputStream getInputStream(String filepath) {
        return getInputStream(new File(filepath));
    }


    public static InputStream byteStream(byte[] bytes) {
        return new ByteArrayInputStream(bytes);
    }

    public static String readString(InputStream in) throws IOException {
        return new String(readAll(in));
    }

    public static String readString(File file) throws IOException {
        FileInputStream in = new FileInputStream(file);
        try {
            return readString(in);
        } finally {
            close(in);
        }
    }

    public static String readString(InputStream in, String encoding) throws IOException {
        return new String(readAll(in), encoding);
    }

    public static String readString(File file, String encoding) throws IOException {
        FileInputStream in = new FileInputStream(file);
        try {
            return readString(in, encoding);
        } finally {
            close(in);
        }
    }

    public static String readLine(InputStream in) throws IOException {
        BufferedReader reader = getBufferedReader(in);
        try {
            return reader.readLine();
        } finally {
            close(reader);
        }
    }

    private static BufferedReader getBufferedReader(InputStream in) {
        return new BufferedReader(new InputStreamReader(in));
    }

    private static void close(Closeable in) {
        if (!autoClose) {
            if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
        }
    }
}
