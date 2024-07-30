package com.zhangfuxing.tools.geometry;


import java.awt.*;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/7/11
 * @email zhangfuxing1010@163.com
 */
public class GeoUtil {

    public static double[] computeCircleInscribedPolygon(double centerX, double centerY, double radius, int edgeNum) {
        if (edgeNum < 3) {
            throw new IllegalArgumentException("edgeNum 必须大于3");
        }
        if (radius <= 0) {
            throw new IllegalArgumentException("radius 必须大于0");
        }
        // 每个顶点有x和y两个坐标，所以总共2n个double值
        // 每次增加的角度（弧度）
        double[] vertices = new double[2 * edgeNum];
        double angleIncrement = 2 * Math.PI / edgeNum;
        for (int i = 0; i < edgeNum; i++) {
            // 当前角度（弧度）
            double angle = i * angleIncrement;
            // 计算x坐标
            double x = centerX + radius * Math.cos(angle);
            // 计算y坐标
            double y = centerY + radius * Math.sin(angle);
            // 将x和y坐标交替存储到vertices数组中
            vertices[2 * i] = x;
            vertices[2 * i + 1] = y;
        }
        return vertices;
    }

    public static List<GeoPoint> computeCircleInscribedPolygon(GeoPoint centerPoint, double radius, int edgeNum) {
        if (edgeNum < 3) {
            throw new IllegalArgumentException("edgeNum 必须大于3");
        }
        if (radius <= 0) {
            throw new IllegalArgumentException("radius 必须大于0");
        }
        List<GeoPoint> result = new ArrayList<>(edgeNum);
        // 每次增加的角度（弧度）
        double angleIncrement = 2 * Math.PI / edgeNum;
        for (int i = 0; i < edgeNum; i++) {
            // 当前角度（弧度）
            double angle = i * angleIncrement;
            // 计算x坐标
            double x = centerPoint.x + radius * Math.cos(angle);
            // 计算y坐标
            double y = centerPoint.y + radius * Math.sin(angle);
            // 将x和y坐标交替存储到vertices数组中
            result.add(new GeoPoint(x, y));
        }

        return result;
    }

    public static boolean hasIntersect(Collection<GeoPoint> polygon1, Collection<GeoPoint> polygon2) {
        Area area = buildArea(polygon1);
        area.intersect(buildArea(polygon2));
        return !area.isEmpty();
    }


    public static Area buildArea(Collection<GeoPoint> points) {
        assert points != null && !points.isEmpty();
        Polygon polygon = new Polygon();
        for (GeoPoint point : points) {
            polygon.addPoint((int) point.x, (int) point.y);
        }
        return new Area(polygon);
    }
}
