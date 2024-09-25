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
 * 生成验证码图片的类，可自定义字体 颜色 背景等；该验证图片只能生成简单的图片验证码，复杂验证请自行更换其他库
 *
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

    /**
     * 验证码背景颜色1
     */
    private Color background1;
    /**
     * 验证码背景颜色2
     */
    private Color background2;
    /**
     * 是否使用背景颜色渐变
     */
    private boolean backgroundGradient;

    private GradientType backgroundGradientType;

    private GradientType textGradientType;

    Captcha() {
        this.width = 400;
        this.height = 200;
        this.content = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        this.length = 4;
        this.textColor = Color.BLACK;
        this.backgroundColor = Color.WHITE;
        this.lineCount = 50;
        this.lineWidth = 1.5F;
        this.font = new Font("Arial", Font.ITALIC, 150);
        this.contentX = 0;
        this.contentY = 150;
        this.pointCount = 100;
        this.pointSize = 20;
        this.gradientStart = new Color(93, 178, 215);
        this.gradientEnd = new Color(33, 5, 66);
        this.useGradient = false;
        this.background1 = new Color(255, 255, 255);
        this.background2 = new Color(240, 240, 240);
        this.backgroundGradient = false;
        this.backgroundGradientType = GradientType.HORIZONTAL;
        this.textGradientType = GradientType.HORIZONTAL;
        try {
            this.outputStream = new FileOutputStream("captcha.png");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static CaptchaBuilder builder() {
        return CaptchaBuilder.createBuilder();
    }

    /**
     * 颜色值转颜色对象
     *
     * @param hex 颜色值
     * @return 颜色对象
     */
    public static Color hex2Color(String hex) {
        if (hex == null || (hex.length() != 7 && hex.length() != 9)) {
            throw new IllegalArgumentException("颜色格式不正确: " + hex);
        }

        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }

        try {
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);

            if (hex.length() == 8) {
                int a = Integer.parseInt(hex.substring(6, 8), 16);
                return new Color(r, g, b, a);
            }

            return new Color(r, g, b);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("颜色值解析错误: " + hex, e);
        }
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
            if (this.backgroundGradient) {
                GradientPaint paint = this.backgroundGradientType.createGradientPaint(width, height, background1, background2);
                g.setPaint(paint);
            } else {
                g.setColor(this.backgroundColor);
            }
            g.fillRect(0, 0, width, height);

            g.setFont(this.font);
            if (this.useGradient) {
                gradientPaint = Objects.requireNonNullElse(gradientPaint, this.textGradientType.createGradientPaint(width, height, gradientStart, getGradientEnd()));
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

    public enum GradientType {
        HORIZONTAL, VERTICAL, DIAGONAL, ANTI_DIAGONAL;

        public GradientPaint createGradientPaint(int width, int height, Color from, Color to) {
            return switch (this) {
                case HORIZONTAL -> new GradientPaint(0, 0, from, width, 0, to);
                case VERTICAL -> new GradientPaint(0, 0, from, 0, height, to);
                case DIAGONAL -> new GradientPaint(0, 0, from, width, height, to);
                case ANTI_DIAGONAL -> new GradientPaint(0, height, from, width, 0, to);
            };
        }
    }

    public String write(OutputStream outputStream) {
        setOutputStream(outputStream);
        return createCaptcha();
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

    void backgroundGradient(Color background1, Color background2) {
        this.backgroundGradient = true;
        this.background1 = background1;
        this.background2 = background2;
    }

    void setBackgroundGradientType(GradientType backgroundGradientType) {
        this.backgroundGradientType = backgroundGradientType;
    }

    void setTextGradientType(GradientType textGradientType) {
        this.textGradientType = textGradientType;
    }
}
