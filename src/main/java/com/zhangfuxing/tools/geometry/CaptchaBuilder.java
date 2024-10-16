package com.zhangfuxing.tools.geometry;

import java.awt.*;
import java.io.OutputStream;

/**
 * 构造验证码对象的构造类
 *
 * @author 张福兴
 * @version 1.0
 * @date 2024/9/24
 * @email zhangfuxing1010@163.com
 */
public class CaptchaBuilder {
    private final Captcha captcha;

    private CaptchaBuilder() {
        this.captcha = new Captcha();

    }

    public static CaptchaBuilder createBuilder() {
        return new CaptchaBuilder();
    }

    public CaptchaBuilder setWidth(int width) {
        captcha.setWidth(width);
        return this;
    }

    public CaptchaBuilder setHeight(int height) {
        captcha.setHeight(height);
        return this;
    }

    public CaptchaBuilder setContent(String content) {
        captcha.setContent(content);
        return this;
    }

    public CaptchaBuilder setLength(int length) {
        captcha.setLength(length);
        return this;
    }

    public CaptchaBuilder setTextColor(Color color) {
        captcha.setTextColor(color);
        return this;
    }

    public CaptchaBuilder setBackgroundColor(Color color) {
        captcha.setBackgroundColor(color);
        return this;
    }

    public CaptchaBuilder setFont(Font font) {
        captcha.setFont(font);
        return this;
    }

    public CaptchaBuilder setLineCount(int count) {
        captcha.setLineCount(count);
        return this;
    }

    public CaptchaBuilder setLineWidth(int width) {
        captcha.setLineWidth(width);
        return this;
    }

    public CaptchaBuilder setTextPosition(int x, int y) {
        captcha.setContentX(x);
        captcha.setContentY(y);
        return this;
    }

    public CaptchaBuilder setOutputStream(OutputStream outputStream) {
        captcha.setOutputStream(outputStream);
        return this;
    }

    public CaptchaBuilder setPointCount(int count) {
        captcha.setPointCount(count);
        return this;
    }

    public CaptchaBuilder setPointMaxSize(int size) {
        captcha.setPointSize(size);
        return this;
    }

    public CaptchaBuilder useGradient(Color color, Color color2) {
        captcha.setUseGradient();
        captcha.setGradientStart(color);
        captcha.setGradientEnd(color2);
        return this;
    }

    public CaptchaBuilder useGradient() {
        captcha.setUseGradient();
        return this;
    }

    public CaptchaBuilder setGradientPaint(GradientPaint gradientPaint) {
        captcha.setGradientPaint(gradientPaint);
        return this;
    }

    public CaptchaBuilder backgroundGradient(Color color, Color color2) {
        captcha.backgroundGradient(color, color2);
        return this;
    }

    public CaptchaBuilder setBackgroundGradientType(Captcha.GradientType gradientType) {
        captcha.setBackgroundGradientType(gradientType);
        return this;
    }

    public CaptchaBuilder setTextGradientType(Captcha.GradientType gradientType) {
        captcha.setTextGradientType(gradientType);
        return this;
    }


    public Captcha build() {
        return captcha;
    }
}
