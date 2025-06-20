package com.example.service;

import com.example.model.TableInfo;
import com.example.model.TableColumn;
import com.example.model.TableInfoWithColumns;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TableInfoService {

    // 注入JdbcTemplate
    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 获取表信息
    public List<TableInfo> getTableInfo() {
        // 查询语句
        String sql = "SELECT " +
                "table_name as tableName, " +
                "engine, " +
                "data_length as dataLength, " +
                "index_length as indexLength, " +
                "(data_length + index_length) as tableSize, " +
                "ROUND((data_length / (data_length + index_length)) * 100, 2) as dataSpacePercentage " +
                "FROM information_schema.tables " +
                "WHERE table_schema = DATABASE()";

        // 使用JdbcTemplate执行查询，并返回结果
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            TableInfo info = new TableInfo();
            info.setTableName(rs.getString("tableName"));
            info.setEngine(rs.getString("engine"));
            info.setDataLength(rs.getLong("dataLength"));
            info.setIndexLength(rs.getLong("indexLength"));
            info.setTableSize(rs.getLong("tableSize"));
            info.setDataSpacePercentage(rs.getDouble("dataSpacePercentage"));
            return info;
        });
    }

    // 查询所有表的字段结构
    public List<TableInfoWithColumns> getAllTableColumns() {
        String sql = "SELECT TABLE_NAME as tableName, COLUMN_NAME as columnName, COLUMN_TYPE as columnType, " +
                "IF(COLUMN_KEY='PRI','YES','NO') as isPrimaryKey, IS_NULLABLE as isNullable, COLUMN_DEFAULT as columnDefault, COLUMN_COMMENT as columnComment " +
                "FROM information_schema.columns WHERE table_schema = DATABASE() ORDER BY TABLE_NAME, ORDINAL_POSITION";
        List<TableColumn> columns = jdbcTemplate.query(sql, (rs, rowNum) -> {
            TableColumn column = new TableColumn();
            column.setTableName(rs.getString("tableName"));
            column.setColumnName(rs.getString("columnName"));
            column.setColumnType(rs.getString("columnType"));
            column.setIsPrimaryKey(rs.getString("isPrimaryKey"));
            column.setIsNullable(rs.getString("isNullable"));
            column.setColumnDefault(rs.getString("columnDefault"));
            column.setColumnComment(rs.getString("columnComment"));
            return column;
        });
        // 按表名分组
        Map<String, List<TableColumn>> grouped = columns.stream().collect(Collectors.groupingBy(TableColumn::getTableName));
        // 组装成 List<TableInfoWithColumns>
        return grouped.entrySet().stream().map(entry -> {
            TableInfoWithColumns info = new TableInfoWithColumns();
            info.setTableName(entry.getKey());
            info.setColumns(entry.getValue());
            return info;
        }).collect(Collectors.toList());
    }
} 