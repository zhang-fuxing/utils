package com.zhangfuxing.tools.file;

import java.util.regex.Pattern;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/7/8
 * @email zhangfuxing1010@163.com
 */
public enum DataSizeUnit {
    B(1),
    KB(1024),
    MB(1024 << 10),
    GB(1024 << 20),
    TB(1024L << 30),
    PB(1024L << 40),
    EB(1024L << 50);

    static final String k = "^\\d+(\\.\\d{1,2})?(K|KB)$";
    static final String m = "^\\d+(\\.\\d{1,2})?(M|MB)$";
    static final String g = "^\\d+(\\.\\d{1,2})?(G|GB)$";
    static final String t = "^\\d+(\\.\\d{1,2})?(T|TB)$";
    static final String p = "^\\d+(\\.\\d{1,2})?(P|PB)$";
    static final String e = "^\\d+(\\.\\d{1,2})?(E|EB)$";

    static final Pattern patternK = Pattern.compile(k);
    static final Pattern patternM = Pattern.compile(m);
    static final Pattern patternG = Pattern.compile(g);
    static final Pattern patternT = Pattern.compile(t);
    static final Pattern patternP = Pattern.compile(p);
    static final Pattern patternE = Pattern.compile(e);

    public final long unitSize;

    DataSizeUnit(long unitSize) {
        this.unitSize = unitSize;
    }

    public static DataSizeUnit auto(long dataSize) {
        if (dataSize >= EB.unitSize) return EB;
        else if (dataSize >= PB.unitSize) return PB;
        else if (dataSize >= TB.unitSize) return TB;
        else if (dataSize >= GB.unitSize) return GB;
        else if (dataSize >= MB.unitSize) return MB;
        else if (dataSize >= KB.unitSize) return KB;
        else return B;
    }

    public static DataSizeUnit parseUnit(String sizeText) {
        sizeText = sizeText.toUpperCase().replaceAll("\\s+", "");
        if (patternK.matcher(sizeText).matches()) return KB;
        if (patternM.matcher(sizeText).matches()) return MB;
        if (patternG.matcher(sizeText).matches()) return GB;
        if (patternT.matcher(sizeText).matches()) return TB;
        if (patternP.matcher(sizeText).matches()) return PB;
        if (patternE.matcher(sizeText).matches()) return EB;
        return B;
    }

    public double parseSize(String sizeText) {
        sizeText = sizeText.toUpperCase().replaceAll("\\s+", "");
        String name = this.name();
        if (sizeText.endsWith(name)) {
            sizeText = sizeText.substring(0, sizeText.length() - 2);
        } else {
            sizeText = sizeText.substring(0, sizeText.length() - 1);
        }
        return Double.parseDouble(sizeText) * this.unitSize;
    }
}
