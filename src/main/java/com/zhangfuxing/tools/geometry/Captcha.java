package com.zhangfuxing.tools.geometry;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;
import java.util.Random;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/9/24
 * @email zhangfuxing1010@163.com
 */
public class Captcha {
    /**
     * 图片宽度
     */
    private int width;
    /**
     * 图片高度
     */
    private int height;
    /**
     * 生成验证码的字符集
     */
    private String content;
    /**
     * 验证码长度
     */
    private int length;
    /**
     * 验证码颜色
     */
    private Color textColor;
    /**
     * 背景颜色
     */
    private Color backgroundColor;
    /**
     * 干扰线数量
     */
    private int lineCount;
    /**
     * 干扰线宽度
     */
    private float lineWidth;
    /**
     * 验证码字体
     */
    private Font font;
    /**
     * 验证码起始X坐标
     */
    private int contentX;
    /**
     * 验证码起始Y坐标
     */
    private int contentY;
    /**
     * 输出流
     */
    private OutputStream outputStream;
    /**
     * 是否使用默认输出流
     */
    private boolean defaultOutputStream = true;
    /**
     * 干扰点数量
     */
    private int pointCount;
    /**
     * 干扰点大小
     */
    private int pointSize;
    /**
     * 渐变色
     */
    private GradientPaint gradientPaint;
    /**
     * 渐变色起始颜色
     */
    private Color gradientStart;
    /**
     * 渐变色结束颜色
     */
    private Color gradientEnd;

    /**
     * 是否使用渐变色
     */
    private boolean useGradient;

    private String currentCode;

    Captcha() {
        this.width = 550;
        this.height = 300;
        this.content = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        this.length = 4;
        this.textColor = Color.BLACK;
        this.backgroundColor = Color.WHITE;
        this.lineCount = 30;
        this.lineWidth = 1.5F;
        this.font = new Font("Arial", Font.ITALIC, 150);
        this.contentX = height / 4;
        this.contentY = height - height / 3;
        this.pointCount = 100;
        this.pointSize = 5;
        try {
            this.outputStream = new FileOutputStream("captcha.png");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        this.gradientStart = new Color(194, 239, 57);
        this.gradientEnd = new Color(48, 174, 211);
        this.useGradient = false;
    }

    public static CaptchaBuilder builder() {
        return CaptchaBuilder.createBuilder();
    }

    public String createCaptcha() {
        Random random = new Random(System.currentTimeMillis());
        // 生成随机验证码
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(this.content.charAt(random.nextInt(this.content.length())));
        }
        String code = sb.toString();

        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bi.createGraphics();

        try {
            g.setColor(this.backgroundColor);
            g.fillRect(0, 0, width, height);
            g.setFont(this.font);
            if (this.useGradient) {
                gradientPaint = Objects.requireNonNullElse(gradientPaint, new GradientPaint(0, 0, gradientStart, width, height, gradientEnd));
                g.setPaint(gradientPaint);
            } else {
                g.setColor(this.textColor);
            }
            // 绘制字符串
            g.drawString(code, contentX, contentY);

            // 绘制干扰线
            for (int i = 0; i < this.lineCount; i++) {
                int x1 = random.nextInt(width);
                int y1 = random.nextInt(height);
                int x2 = random.nextInt(width);
                int y2 = random.nextInt(height);
                g.setColor(new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
                g.setStroke(new BasicStroke(this.lineWidth));
                g.drawLine(x1, y1, x2, y2);
            }

            // 绘制干扰点
            for (int i = 0; i < this.pointCount; i++) {
                int x = random.nextInt(width);
                int y = random.nextInt(height);
                g.setColor(new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
                int pointSizeR0 = random.nextInt(this.pointSize);
                int pointSizeR1 = random.nextInt(this.pointSize);
                g.drawOval(x, y, pointSizeR0, pointSizeR1);
            }
        } catch (Exception e) {
            throw new RuntimeException("绘制图像时发生错误: " + e.getMessage());
        } finally {
            g.dispose(); // 确保在任何情况下都释放资源
        }

        try {
            ImageIO.write(bi, "png", this.outputStream);
        } catch (IOException e) {
            throw new RuntimeException("保存图像时发生错误: " + e.getMessage());
        } finally {
            if (this.defaultOutputStream) {
                close(this.outputStream);
            }
        }
        this.currentCode = code;
        return code;
    }

    public boolean verify(String code) {
        if (this.currentCode == null) return false;
        return this.currentCode.equalsIgnoreCase(code);
    }

    void setUseGradient() {
        this.useGradient = true;
    }

    private void close(OutputStream outputStream) {
        try (outputStream) {
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getCurrentCode() {
        return currentCode;
    }

    Font getFont() {
        return font;
    }

    void setFont(Font font) {
        Objects.requireNonNull(font);
        this.font = font;
    }

    int getWidth() {
        return width;
    }

    void setWidth(int width) {
        this.width = width;
    }

    int getHeight() {
        return height;
    }

    void setHeight(int height) {
        this.height = height;
    }

    String getContent() {
        return content;
    }

    void setContent(String content) {
        Objects.requireNonNull(content);
        this.content = content;
    }

    int getLength() {
        return length;
    }

    void setLength(int length) {
        this.length = length;
    }

    Color getTextColor() {
        return textColor;
    }

    void setTextColor(Color textColor) {
        Objects.requireNonNull(textColor);
        this.textColor = textColor;
    }

    Color getBackgroundColor() {
        return backgroundColor;
    }

    void setBackgroundColor(Color backgroundColor) {
        Objects.requireNonNull(backgroundColor);
        this.backgroundColor = backgroundColor;
    }

    int getLineCount() {
        return lineCount;
    }

    void setLineCount(int lineCount) {
        this.lineCount = lineCount;
    }

    float getLineWidth() {
        return lineWidth;
    }

    void setLineWidth(float lineWidth) {
        this.lineWidth = lineWidth;
    }

    int getContentX() {
        return contentX;
    }

    void setContentX(int contentX) {
        this.contentX = contentX;
    }

    int getContentY() {
        return contentY;
    }

    void setContentY(int contentY) {
        this.contentY = contentY;
    }

    OutputStream getOutputStream() {
        return outputStream;
    }

    void setOutputStream(OutputStream outputStream) {
        Objects.requireNonNull(outputStream);
        this.defaultOutputStream = false;
        this.outputStream = outputStream;
    }

    int getPointCount() {
        return pointCount;
    }

    void setPointCount(int pointCount) {
        this.pointCount = pointCount;
    }

    int getPointSize() {
        return pointSize;
    }

    void setPointSize(int pointSize) {
        this.pointSize = pointSize;
    }

    Color getGradientStart() {
        return gradientStart;
    }

    void setGradientStart(Color gradientStart) {
        this.gradientStart = gradientStart;
    }

    Color getGradientEnd() {
        return gradientEnd;
    }

    void setGradientEnd(Color gradientEnd) {
        this.gradientEnd = gradientEnd;
    }

    void setGradientPaint(GradientPaint gradientPaint) {
        this.useGradient = true;
        this.gradientPaint = gradientPaint;
    }
}
