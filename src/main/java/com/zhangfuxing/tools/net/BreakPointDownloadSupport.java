package com.zhangfuxing.tools.net;

import com.zhangfuxing.tools.io.Model;
import com.zhangfuxing.tools.io.RandomResource;
import com.zhangfuxing.tools.util.ITools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/9/2
 * @email zhangfuxing1010@163.com
 */
@SuppressWarnings("DuplicatedCode")
public class BreakPointDownloadSupport {
    private static final Logger log = LoggerFactory.getLogger(BreakPointDownloadSupport.class);

    public static void breakPointDownload(File file,javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) {
        String rangeHeader = request.getHeader("Range");
        RandomResource randomResource = RandomResource.create(file, Model.R, e ->
                log.error("文件资源读取异常：msg={}, type={}", e.getMessage(), e.getClass().getName(), e));
        response.setContentType("application/octet-stream");
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("Last-Modified", ITools.DateTime.format());
        response.setHeader("Content-Disposition", "attachment; filename=" + "\"" + ITools.Str.encodeUrl(file.getName()) + "\"");
        long fileLength = file.length();
        response.setContentLengthLong(fileLength);

        if (rangeHeader == null) {
            response.setStatus(200);
            response.setHeader("Content-Range", "bytes 0-" + (fileLength - 1) + "/" + fileLength);
            try (randomResource; var outputStream = response.getOutputStream()) {
                randomResource.readAll(outputStream, true);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            // 解析 Range 头部
            long start = 0, end = fileLength;
            String[] parts = rangeHeader.substring(rangeHeader.indexOf("=") + 1).split("-");
            if (parts.length > 0) {
                start = Long.parseLong(parts[0]);
                if (parts.length > 1) {
                    end = Long.parseLong(parts[1]);
                }
            }

            // 设置HTTP响应头
            long contentLength = end - start;
            String contentRange = "bytes " + start + "-" + (end - 1) + "/" + fileLength;
            response.setHeader("Content-Range", contentRange);
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            response.setHeader("Content-Length", String.valueOf(contentLength));
            try (var os = response.getOutputStream(); randomResource) {
                randomResource.readChunk(os, start, end);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void breakPointDownload(File file, jakarta.servlet.http.HttpServletRequest request,jakarta.servlet.http.HttpServletResponse response) {
        String rangeHeader = request.getHeader("Range");
        RandomResource randomResource = RandomResource.create(file, Model.R, e ->
                log.error("文件资源读取异常：msg={}, type={}", e.getMessage(), e.getClass().getName(), e));
        response.setContentType("application/octet-stream");
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("Last-Modified", ITools.DateTime.format());
        response.setHeader("Content-Disposition", "attachment; filename=" + "\"" + ITools.Str.encodeUrl(file.getName()) + "\"");
        long fileLength = file.length();
        response.setContentLengthLong(fileLength);

        if (rangeHeader == null) {
            response.setStatus(200);
            response.setHeader("Content-Range", "bytes 0-" + (fileLength - 1) + "/" + fileLength);
            try (randomResource; var outputStream = response.getOutputStream()) {
                randomResource.readAll(outputStream, true);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            // 解析 Range 头部
            long start = 0, end = fileLength;
            String[] parts = rangeHeader.substring(rangeHeader.indexOf("=") + 1).split("-");
            if (parts.length > 0) {
                start = Long.parseLong(parts[0]);
                if (parts.length > 1) {
                    end = Long.parseLong(parts[1]);
                }
            }

            // 设置HTTP响应头
            long contentLength = end - start;
            String contentRange = "bytes " + start + "-" + (end - 1) + "/" + fileLength;
            response.setHeader("Content-Range", contentRange);
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            response.setHeader("Content-Length", String.valueOf(contentLength));
            try (var os = response.getOutputStream(); randomResource) {
                randomResource.readChunk(os, start, end);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
