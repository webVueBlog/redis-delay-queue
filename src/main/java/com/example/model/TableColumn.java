package com.example.model;

import lombok.Data;

@Data
public class TableColumn {
    private String tableName;      // 表名
    private String columnName;     // 字段名
    private String columnType;     // 字段类型
    private String isPrimaryKey;   // 是否主键
    private String isNullable;     // 是否可为空
    private String columnDefault;  // 默认值
    private String columnComment;  // 字段注释
} 