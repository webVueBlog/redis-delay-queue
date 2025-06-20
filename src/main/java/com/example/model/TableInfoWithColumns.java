package com.example.model;

import lombok.Data;
import java.util.List;

@Data
public class TableInfoWithColumns {
    private String tableName; // 表名
    private List<TableColumn> columns; // 字段列表
} 