package com.zhangfuxing.tools.db;

import javax.persistence.Column;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/9/27
 * @email zhangfuxing1010@163.com
 */
public class TableMetaData implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "TABLE_CAT")
    private String tableCat;
    @Column(name = "TABLE_SCHEM")
    private String tableSchem;
    @Column(name = "TABLE_NAME")
    private String tableName;
    @Column(name = "TABLE_TYPE")
    private String tableType;
    @Column(name = "REMARKS")
    private String remarks;
    @Column(name = "TYPE_CAT")
    private String typeCat;
    @Column(name = "TYPE_SCHEM")
    private String typeSchem;
    @Column(name = "TYPE_NAME")
    private String typeName;

    private List<ColumnMetaData> columnMetaData;

    public List<ColumnMetaData> getColumnMetaData() {
        return columnMetaData;
    }

    public void setColumnMetaData(List<ColumnMetaData> columnMetaData) {
        this.columnMetaData = columnMetaData;
    }

    public String getTableCat() {
        return tableCat;
    }

    public void setTableCat(String tableCat) {
        this.tableCat = tableCat;
    }

    public String getTableSchem() {
        return tableSchem;
    }

    public void setTableSchem(String tableSchem) {
        this.tableSchem = tableSchem;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTableType() {
        return tableType;
    }

    public void setTableType(String tableType) {
        this.tableType = tableType;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getTypeCat() {
        return typeCat;
    }

    public void setTypeCat(String typeCat) {
        this.typeCat = typeCat;
    }

    public String getTypeSchem() {
        return typeSchem;
    }

    public void setTypeSchem(String typeSchem) {
        this.typeSchem = typeSchem;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    @Override
    public String toString() {
        return "TableMetaData{" +
               "tableCat='" + tableCat + '\'' +
               ", tableSchem='" + tableSchem + '\'' +
               ", tableName='" + tableName + '\'' +
               ", tableType='" + tableType + '\'' +
               ", remarks='" + remarks + '\'' +
               ", typeCat='" + typeCat + '\'' +
               ", typeSchem='" + typeSchem + '\'' +
               ", typeName='" + typeName + '\'' +
               '}';
    }
}
