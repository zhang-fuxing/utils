package com.zhangfuxing.tools.db.page;

public class DbVersion {
    private final String version;
    private final String productName;

    public DbVersion(String version, String productName) {
        this.version = version;
        this.productName = productName;
    }

    public boolean isOracle12cOrLater() {
        return "oracle".equalsIgnoreCase(productName) && 
               compareVersion(version, "12.0.0.0") >= 0;
    }

    public boolean isSqlServer2012OrLater() {
        return "sqlserver".equalsIgnoreCase(productName) && 
               compareVersion(version, "11.0.0.0") >= 0;
    }

    public static int compareVersion(String version1, String version2) {
        String[] v1 = version1.split("\\.");
        String[] v2 = version2.split("\\.");
        
        int length = Math.max(v1.length, v2.length);
        for (int i = 0; i < length; i++) {
            int num1 = i < v1.length ? Integer.parseInt(v1[i]) : 0;
            int num2 = i < v2.length ? Integer.parseInt(v2[i]) : 0;
            
            if (num1 != num2) {
                return num1 - num2;
            }
        }
        return 0;
    }
} 