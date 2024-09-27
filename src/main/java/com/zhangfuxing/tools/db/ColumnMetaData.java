package com.zhangfuxing.tools.db;

import javax.persistence.Column;
import java.io.Serial;
import java.io.Serializable;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/9/27
 * @email zhangfuxing1010@163.com
 */
public class ColumnMetaData implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name="TABLE_CAT")
    private String tableCat;
    @Column(name="TABLE_SCHEM")
    private String tableSchem;
    @Column(name="TABLE_NAME")
    private String tableName;
    @Column(name="COLUMN_NAME")
    private String columnName;
    @Column(name="DATA_TYPE")
    private Integer dataType;
    @Column(name="TYPE_NAME")
    private String typeName;
    @Column(name="COLUMN_SIZE")
    private Integer columnSize;
    @Column(name="BUFFER_LENGTH")
    private String bufferLength;
    @Column(name="DECIMAL_DIGITS")
    private Integer decimalDigits;
    @Column(name="NUM_PREC_RADIX")
    private Integer numPrecRadix;
    @Column(name="NULLABLE")
    private Integer nullable;
    @Column(name="REMARKS")
    private String remarks;
    @Column(name="COLUMN_DEF")
    private String columnDef;
    @Column(name="SQL_DATA_TYPE")
    private Integer sqlDataType;
    @Column(name="SQL_DATETIME_SUB")
    private Integer sqlDatetimeSub;
    @Column(name="CHAR_OCTET_LENGTH")
    private String charOctetLength;
    @Column(name="ORDINAL_POSITION")
    private Integer ordinalPosition;
    @Column(name="IS_NULLABLE")
    private String isNullable;
    @Column(name="SCOPE_CATALOG")
    private String scopeCatalog;
    @Column(name="SCOPE_SCHEMA")
    private String scopeSchema;
    @Column(name="SCOPE_TABLE")
    private String scopeTable;
    @Column(name="SOURCE_DATA_TYPE")
    private Integer sourceDataType;
    @Column(name="IS_AUTOINCREMENT")
    private String isAutoincrement;
    @Column(name="IS_GENERATEDCOLUMN")
    private String isGeneratedcolumn;


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

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public Integer getDataType() {
        return dataType;
    }

    public void setDataType(Integer dataType) {
        this.dataType = dataType;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public Integer getColumnSize() {
        return columnSize;
    }

    public void setColumnSize(Integer columnSize) {
        this.columnSize = columnSize;
    }

    public String getBufferLength() {
        return bufferLength;
    }

    public void setBufferLength(String bufferLength) {
        this.bufferLength = bufferLength;
    }

    public Integer getDecimalDigits() {
        return decimalDigits;
    }

    public void setDecimalDigits(Integer decimalDigits) {
        this.decimalDigits = decimalDigits;
    }

    public Integer getNumPrecRadix() {
        return numPrecRadix;
    }

    public void setNumPrecRadix(Integer numPrecRadix) {
        this.numPrecRadix = numPrecRadix;
    }

    public Integer getNullable() {
        return nullable;
    }

    public void setNullable(Integer nullable) {
        this.nullable = nullable;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getColumnDef() {
        return columnDef;
    }

    public void setColumnDef(String columnDef) {
        this.columnDef = columnDef;
    }

    public Integer getSqlDataType() {
        return sqlDataType;
    }

    public void setSqlDataType(Integer sqlDataType) {
        this.sqlDataType = sqlDataType;
    }

    public Integer getSqlDatetimeSub() {
        return sqlDatetimeSub;
    }

    public void setSqlDatetimeSub(Integer sqlDatetimeSub) {
        this.sqlDatetimeSub = sqlDatetimeSub;
    }

    public String getCharOctetLength() {
        return charOctetLength;
    }

    public void setCharOctetLength(String charOctetLength) {
        this.charOctetLength = charOctetLength;
    }

    public Integer getOrdinalPosition() {
        return ordinalPosition;
    }

    public void setOrdinalPosition(Integer ordinalPosition) {
        this.ordinalPosition = ordinalPosition;
    }

    public String getIsNullable() {
        return isNullable;
    }

    public void setIsNullable(String isNullable) {
        this.isNullable = isNullable;
    }

    public String getScopeCatalog() {
        return scopeCatalog;
    }

    public void setScopeCatalog(String scopeCatalog) {
        this.scopeCatalog = scopeCatalog;
    }

    public String getScopeSchema() {
        return scopeSchema;
    }

    public void setScopeSchema(String scopeSchema) {
        this.scopeSchema = scopeSchema;
    }

    public String getScopeTable() {
        return scopeTable;
    }

    public void setScopeTable(String scopeTable) {
        this.scopeTable = scopeTable;
    }

    public Integer getSourceDataType() {
        return sourceDataType;
    }

    public void setSourceDataType(Integer sourceDataType) {
        this.sourceDataType = sourceDataType;
    }

    public String getIsAutoincrement() {
        return isAutoincrement;
    }

    public void setIsAutoincrement(String isAutoincrement) {
        this.isAutoincrement = isAutoincrement;
    }

    public String getIsGeneratedcolumn() {
        return isGeneratedcolumn;
    }

    public void setIsGeneratedcolumn(String isGeneratedcolumn) {
        this.isGeneratedcolumn = isGeneratedcolumn;
    }

    @Override
    public String toString() {
        return "ColumnMetaData{" +
               "tableCat='" + tableCat + '\'' +
               ", tableSchem='" + tableSchem + '\'' +
               ", tableName='" + tableName + '\'' +
               ", columnName='" + columnName + '\'' +
               ", dataType=" + dataType +
               ", typeName='" + typeName + '\'' +
               ", columnSize=" + columnSize +
               ", bufferLength='" + bufferLength + '\'' +
               ", decimalDigits=" + decimalDigits +
               ", numPrecRadix=" + numPrecRadix +
               ", nullable=" + nullable +
               ", remarks='" + remarks + '\'' +
               ", columnDef='" + columnDef + '\'' +
               ", sqlDataType=" + sqlDataType +
               ", sqlDatetimeSub=" + sqlDatetimeSub +
               ", charOctetLength='" + charOctetLength + '\'' +
               ", ordinalPosition=" + ordinalPosition +
               ", isNullable='" + isNullable + '\'' +
               ", scopeCatalog='" + scopeCatalog + '\'' +
               ", scopeSchema='" + scopeSchema + '\'' +
               ", scopeTable='" + scopeTable + '\'' +
               ", sourceDataType=" + sourceDataType +
               ", isAutoincrement='" + isAutoincrement + '\'' +
               ", isGeneratedcolumn='" + isGeneratedcolumn + '\'' +
               '}';
    }
}
