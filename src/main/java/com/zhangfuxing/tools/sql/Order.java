package com.zhangfuxing.tools.sql;
 /**
 * @author 张福兴
 * @version 1.0
 * @date 2024/4/25
 * @email zhangfuxing1010@163.com
 */
public record Order(String col, OrderBy orderType) {
     public static Order of(String col, OrderBy orderType) {
        return new Order(col, orderType);
     }

    public static Order ofAsc(String col) {
        return new Order(col, OrderBy.ASC);
    }

     public static Order[] ofAsc(String... cols) {
         Order[] orders = new Order[cols.length];
         for (int i = 0; i < cols.length; i++) {
             orders[i] = new Order(cols[i], OrderBy.ASC);
         }

         return orders;
     }
    public static Order ofDesc(String col) {
        return new Order(col, OrderBy.DESC);
    }
     public static Order[] ofDesc(String... cols) {
         Order[] orders = new Order[cols.length];
         for (int i = 0; i < cols.length; i++) {
             orders[i] = new Order(cols[i], OrderBy.DESC);
         }
         return orders;
     }
}
