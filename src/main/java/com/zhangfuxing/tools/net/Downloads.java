package com.zhangfuxing.tools.net;

import com.zhangfuxing.tools.file.Fs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/8/30
 * @email zhangfuxing1010@163.com
 */
public class Downloads {

    public static void main(String[] args) throws Exception {
        // 功能测试
        var url = "https://dldir1.qq.com/qqfile/qq/QQNT/Windows/QQ_9.9.15_240826_x64_01.exe";
        long s = System.currentTimeMillis();
        Downloads downloads = new Downloads(url, "QQ_9.9.15_240826_x64_01.exe");
        downloads.setThreadPoolSize(4);
        downloads.concurrentDownload();
        System.out.println(System.currentTimeMillis() - s);
    }

    private String sourceUrl;
    private String savePath;
    private final byte[] buf = new byte[1 << 18];
    // ArrayBlockingQueue、 LinkedBlockingQueue、 SynchronousQueue、 PriorityBlockingQueue
    // AbortPolicy（默认，直接抛出异常）、CallerRunsPolicy（由调用线程处理任务）、
    // DiscardOldestPolicy（丢弃队列中最老的任务，然后尝试重新提交被拒绝的任务）和DiscardPolicy（直接丢弃任务，不抛出异常）
    // new ThreadPoolExecutor(
    //                threadNum,
    //                threadNum,
    //                30000, TimeUnit.MILLISECONDS,
    //                new LinkedBlockingQueue<>(5),
    //                Executors.defaultThreadFactory(),
    //                new ThreadPoolExecutor.AbortPolicy()
    //        );
    private ExecutorService threadPool;
    private int coreThreadSize;
    private final String fileName;

    public Downloads(String sourceUrl, String savePath, int threadNum, String fileName) {
        this.sourceUrl = sourceUrl;
        this.savePath = savePath;
        int maximumPoolSize = Runtime.getRuntime().availableProcessors();
        if (threadNum > maximumPoolSize) {
            threadNum = maximumPoolSize;
        }
        this.coreThreadSize = threadNum;
        this.threadPool = Executors.newFixedThreadPool(threadNum);
        this.fileName = fileName;
    }

    public Downloads(String sourceUrl, String fileName) {
        this(sourceUrl, "./", 3, fileName);
    }

    public void setThreadPoolSize(int threadPoolSize) {
        int maximumPoolSize = Runtime.getRuntime().availableProcessors();
        if (threadPoolSize > maximumPoolSize) {
            threadPoolSize = maximumPoolSize;
        }
        this.coreThreadSize = threadPoolSize;
        this.threadPool = Executors.newFixedThreadPool(threadPoolSize);
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public void concurrentDownload() {
        try {
            HttpResponse<Void> response = HttpClient.newHttpClient()
                    .send(HttpRequest.newBuilder()
                                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                                    .uri(URI.create(this.sourceUrl))
                                    .build(),
                            HttpResponse.BodyHandlers.discarding());
            HttpHeaders headers = response.headers();
//            String rangesSupport = headers.firstValue("Accept-Ranges").orElse(null);
//            boolean isStream = headers.firstValue("Content-Type").map("application/octet-stream"::equals).orElse(false);
            long contentLength = headers.firstValue("Content-Length").map(Long::parseLong).orElse(0L) + 1;
            String saveName = headers.firstValue("content-disposition").map(h -> h.split("filename=")[1]).map(n -> n.replace("\"", "")).orElse(this.fileName);
            if (contentLength == 0) {
                System.out.println("无法获取文件大小");
                return;
            }

            long size = contentLength / coreThreadSize;
            File outputFile = new File(this.savePath + saveName);
            try (RandomAccessFile raf = new RandomAccessFile(outputFile, "rw")) {
                raf.setLength(contentLength);

                ExecutorService executorService = Executors.newFixedThreadPool(coreThreadSize);
                Map<String, AtomicReference<Double>> map = new HashMap<>();
                for (int i = 0; i < coreThreadSize; i++) {
                    long start = i * size;
                    long end = (i == coreThreadSize - 1) ? contentLength - 1 : (i + 1) * size - 1;
                    AtomicReference<Double> reference = new AtomicReference<>();
                    executorService.submit(new DownloadTask(sourceUrl, outputFile, start, end, i + 1, reference));
                    map.put("线程"+ (i+1), reference);
                }

                executorService.shutdown();
                while (!executorService.isTerminated()) {
                    // 等待所有线程完成
                    for (Map.Entry<String, AtomicReference<Double>> entry : map.entrySet()) {
                        String key = entry.getKey();
                        AtomicReference<Double> value = entry.getValue();
                        System.out.printf("\r %s\t%.2f/100.00", key, value.get());
                        System.out.println();
                    }
                }
            }

            System.out.println("文件下载完成");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void download() {
        threadPool.submit(this::exec);
    }

    private void exec() {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(this.sourceUrl));
            File file = new File(savePath, Fs.getName(this.sourceUrl));
            if (file.exists()) {
                builder.header("range", "bytes=" + file.length() + "-");
            }
            var response = HttpClient.newHttpClient().send(builder.build(), HttpResponse.BodyHandlers.ofInputStream());
            Map<String, List<String>> map = response.headers().map();

            int code = response.statusCode();
            if (code != 200 && code != 206) {
                return;
            }
            try (InputStream inputStream = response.body();
                 var rf = new RandomAccessFile(file, "rw")) {
                if (!map.containsKey("Accept-Ranges") && file.exists()) {
                    System.out.println("不支持续传");
                } else {
                    rf.seek(file.length());
                }
                int len;
                while ((len = inputStream.read(buf)) != -1) {
                    rf.write(buf, 0, len);
                    System.out.printf("\r fileSize: %s/%s", Fs.formatSize(file.length()), Fs.formatSize(response.headers().firstValueAsLong("Content-Length").orElse(0)));
                }
            }
        } catch (InterruptedException e) {
            System.out.println("下载中断");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    static class DownloadTask implements Runnable {
        private final String fileUrl;
        private final File outputFile;
        private final long start;
        private final long end;
        private final int taskId;
        private final AtomicReference<Double> progress;

        public DownloadTask(String fileUrl, File outputFile, long start, long end, int taskId, AtomicReference<Double> progress) {
            this.fileUrl = fileUrl;
            this.outputFile = outputFile;
            this.start = start;
            this.end = end;
            this.taskId = taskId;
            this.progress = progress;
        }

        @Override
        public void run() {
            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(fileUrl))
                        .header("Range", "bytes=" + start + "-" + end)
                        .build();

                HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
                if (response.statusCode() != 206) {
                    System.out.printf("\r任务 " + taskId + " 下载失败: " + response.statusCode());
                    return;
                }

                try (InputStream inputStream = response.body();
                     RandomAccessFile raf = new RandomAccessFile(outputFile, "rw")) {
                    raf.seek(start);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    long totalBytesRead = 0;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        raf.write(buffer, 0, bytesRead);
                        totalBytesRead += bytesRead;
                        progress.set((totalBytesRead * 100.0 / (end - start + 1)));
                    }
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
