package com.zhangfuxing.tools.io;

import java.io.*;

public class ReadResource {
    private boolean readClasspath = true;
    private String filepath;

    private ReadResource() {
    }

    public static ReadResource load(String filepath) {
        return load(filepath, true);
    }

    public static ReadResource load(String filepath, boolean readClasspath) {
        ReadResource reader = new ReadResource();
        reader.readClasspath = readClasspath;
        reader.filepath = filepath;
        return reader;
    }

    public String readText() {
        InputStream is = getInputStream(filepath);
        try (var reader = new BufferedReader(new InputStreamReader(is))) {
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

    public byte[] readBytes() {
        InputStream is = getInputStream(filepath);
        try (var reader = new BufferedInputStream(is)) {
            return reader.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] readBytes(int len) {
        InputStream is = getInputStream(filepath);
        try (var reader = new BufferedInputStream(is)) {
            byte[] bytes = new byte[len];
            reader.read(bytes);
            return bytes;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public InputStream readStream() {
        return getInputStream(filepath);
    }

    public BufferedInputStream readBufferedStream() {
        return new BufferedInputStream(getInputStream(filepath));
    }

    public BufferedReader readBufferedReader() {
        return new BufferedReader(new InputStreamReader(getInputStream(filepath)));
    }


    private InputStream getInputStream(String filepath) {
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