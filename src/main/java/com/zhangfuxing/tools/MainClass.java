package com.zhangfuxing.tools;

import com.zhangfuxing.tools.chain.CHR;
import com.zhangfuxing.tools.spring.ioc.Spring;

import java.io.IOException;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/5/22
 * @email zhangfuxing1010@163.com
 */
@Spring
public class MainClass {

    public static void main(String[] args) throws IOException {
        new CHR<String,String>()
                .register("test", () -> System.out.println("123!!"))
                .register("dev", () -> System.out.println("342!!"))
                .register("prod", () -> System.out.println("4588!!"))
                .runAllMatching("prod");

    }


    public static double[] buildPolygonVertices(double centerX, double centerY, double radius, int n) {
        if (n < 3) {
            throw new IllegalArgumentException("n must be greater than 3");
        }

        double[] vertices = new double[2 * n]; // 每个顶点有x和y两个坐标，所以总共2n个double值

        double angleIncrement = 2 * Math.PI / n; // 每次增加的角度（弧度）

        for (int i = 0; i < n; i++) {
            double angle = i * angleIncrement; // 当前角度（弧度）
            double x = centerX + radius * Math.cos(angle); // 计算x坐标
            double y = centerY + radius * Math.sin(angle); // 计算y坐标

            // 将x和y坐标交替存储到vertices数组中
            vertices[2 * i] = x;
            vertices[2 * i + 1] = y;
        }

        return vertices;
    }

}
