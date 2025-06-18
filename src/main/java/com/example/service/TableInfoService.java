package com.example.service;

import com.example.model.TableInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

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
} 