package com.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/tables")
public class TableController {

    @GetMapping("/mock-columns")
    public List<Map<String, Object>> getTableColumns() {
        List<Map<String, Object>> columns = new ArrayList<>();
        
        // 模拟用户表字段
        columns.add(createColumn("users", "用户表", 1, "id", "bigint", "NO", null, "主键ID", "auto_increment", true, false, 20, null));
        columns.add(createColumn("users", "用户表", 2, "username", "varchar", "NO", null, "用户名", "", false, true, null, 50));
        columns.add(createColumn("users", "用户表", 3, "email", "varchar", "YES", null, "邮箱地址", "", false, false, null, 100));
        columns.add(createColumn("users", "用户表", 4, "password", "varchar", "NO", null, "密码", "", false, false, null, 255));
        columns.add(createColumn("users", "用户表", 5, "created_at", "timestamp", "NO", "CURRENT_TIMESTAMP", "创建时间", "", false, false, null, null));
        columns.add(createColumn("users", "用户表", 6, "updated_at", "timestamp", "YES", null, "更新时间", "", false, false, null, null));
        columns.add(createColumn("users", "用户表", 7, "status", "tinyint", "NO", "1", "状态：1-正常，0-禁用", "", false, false, 1, null));
        
        // 模拟订单表字段
        columns.add(createColumn("orders", "订单表", 1, "id", "bigint", "NO", null, "订单ID", "auto_increment", true, false, 20, null));
        columns.add(createColumn("orders", "订单表", 2, "order_no", "varchar", "NO", null, "订单号", "", false, true, null, 32));
        columns.add(createColumn("orders", "订单表", 3, "user_id", "bigint", "NO", null, "用户ID", "", false, false, 20, null));
        columns.add(createColumn("orders", "订单表", 4, "total_amount", "decimal", "NO", "0.00", "订单总金额", "", false, false, 10, 2));
        columns.add(createColumn("orders", "订单表", 5, "status", "varchar", "NO", "pending", "订单状态", "", false, false, null, 20));
        columns.add(createColumn("orders", "订单表", 6, "created_at", "timestamp", "NO", "CURRENT_TIMESTAMP", "创建时间", "", false, false, null, null));
        columns.add(createColumn("orders", "订单表", 7, "updated_at", "timestamp", "YES", null, "更新时间", "", false, false, null, null));
        
        // 模拟产品表字段
        columns.add(createColumn("products", "产品表", 1, "id", "bigint", "NO", null, "产品ID", "auto_increment", true, false, 20, null));
        columns.add(createColumn("products", "产品表", 2, "name", "varchar", "NO", null, "产品名称", "", false, false, null, 100));
        columns.add(createColumn("products", "产品表", 3, "description", "text", "YES", null, "产品描述", "", false, false, null, null));
        columns.add(createColumn("products", "产品表", 4, "price", "decimal", "NO", "0.00", "产品价格", "", false, false, 8, 2));
        columns.add(createColumn("products", "产品表", 5, "stock", "int", "NO", "0", "库存数量", "", false, false, 11, null));
        columns.add(createColumn("products", "产品表", 6, "category_id", "int", "YES", null, "分类ID", "", false, false, 11, null));
        columns.add(createColumn("products", "产品表", 7, "is_active", "tinyint", "NO", "1", "是否启用", "", false, false, 1, null));
        columns.add(createColumn("products", "产品表", 8, "created_at", "timestamp", "NO", "CURRENT_TIMESTAMP", "创建时间", "", false, false, null, null));
        
        return columns;
    }
    
    private Map<String, Object> createColumn(String tableName, String tableComment, int ordinalPosition, 
                                           String columnName, String dataType, String isNullable, 
                                           String columnDefault, String columnComment, String extra,
                                           boolean isPrimaryKey, boolean isUnique,
                                           Integer numericPrecision, Integer characterMaximumLength) {
        Map<String, Object> column = new HashMap<>();
        column.put("tableName", tableName);
        column.put("tableComment", tableComment);
        column.put("ordinalPosition", ordinalPosition);
        column.put("columnName", columnName);
        column.put("dataType", dataType);
        column.put("isNullable", isNullable);
        column.put("columnDefault", columnDefault);
        column.put("columnComment", columnComment);
        column.put("extra", extra);
        column.put("isPrimaryKey", isPrimaryKey);
        column.put("isUnique", isUnique);
        column.put("numericPrecision", numericPrecision);
        column.put("numericScale", dataType.equals("decimal") ? 2 : null);
        column.put("characterMaximumLength", characterMaximumLength);
        return column;
    }
}