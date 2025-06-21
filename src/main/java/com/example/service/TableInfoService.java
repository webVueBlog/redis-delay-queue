package com.example.service;

import com.example.model.TableInfo;
import com.example.model.TableColumn;
import com.example.model.TableInfoWithColumns;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataAccessException;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
@Slf4j
public class TableInfoService {

    // 注入主数据源JdbcTemplate
    private final JdbcTemplate primaryJdbcTemplate;
    
    // 注入用户数据源JdbcTemplate
    private final JdbcTemplate userJdbcTemplate;
    
    @Autowired
    public TableInfoService(@Qualifier("primaryDataSource") DataSource primaryDataSource,
                           @Qualifier("userDataSource") DataSource userDataSource) {
        this.primaryJdbcTemplate = new JdbcTemplate(primaryDataSource);
        this.userJdbcTemplate = new JdbcTemplate(userDataSource);
    }

    // 获取表信息
    public List<TableInfo> getTableInfo() {
        try {
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

            // 使用主数据源JdbcTemplate执行查询，并返回结果
            return primaryJdbcTemplate.query(sql, (rs, rowNum) -> {
                TableInfo info = new TableInfo();
                info.setTableName(rs.getString("tableName"));
                info.setEngine(rs.getString("engine"));
                info.setDataLength(rs.getLong("dataLength"));
                info.setIndexLength(rs.getLong("indexLength"));
                info.setTableSize(rs.getLong("tableSize"));
                info.setDataSpacePercentage(rs.getDouble("dataSpacePercentage"));
                return info;
            });
        } catch (DataAccessException e) {
            log.error("Failed to get table info: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    // 查询所有表的字段结构（合并两个数据源）
    public List<TableInfoWithColumns> getAllTableColumns() {
        List<TableColumn> allColumns = new ArrayList<>();
        
        try {
            // 查询主数据源 sys_db
            String sql = "SELECT TABLE_NAME as tableName, COLUMN_NAME as columnName, COLUMN_TYPE as columnType, " +
                    "IF(COLUMN_KEY='PRI','YES','NO') as isPrimaryKey, IS_NULLABLE as isNullable, COLUMN_DEFAULT as columnDefault, COLUMN_COMMENT as columnComment " +
                    "FROM information_schema.columns WHERE table_schema = 'sys_db' ORDER BY TABLE_NAME, ORDINAL_POSITION";
            List<TableColumn> primaryColumns = primaryJdbcTemplate.query(sql, (rs, rowNum) -> {
                TableColumn column = new TableColumn();
                column.setTableName("[sys_db] " + rs.getString("tableName"));
                column.setColumnName(rs.getString("columnName"));
                column.setColumnType(rs.getString("columnType"));
                column.setIsPrimaryKey(rs.getString("isPrimaryKey"));
                column.setIsNullable(rs.getString("isNullable"));
                column.setColumnDefault(rs.getString("columnDefault"));
                column.setColumnComment(rs.getString("columnComment"));
                return column;
            });
            allColumns.addAll(primaryColumns);
            
            // 查询用户数据源 sys_user_db
            String userSql = "SELECT TABLE_NAME as tableName, COLUMN_NAME as columnName, COLUMN_TYPE as columnType, " +
                    "IF(COLUMN_KEY='PRI','YES','NO') as isPrimaryKey, IS_NULLABLE as isNullable, COLUMN_DEFAULT as columnDefault, COLUMN_COMMENT as columnComment " +
                    "FROM information_schema.columns WHERE table_schema = 'sys_user_db' ORDER BY TABLE_NAME, ORDINAL_POSITION";
            List<TableColumn> userColumns = userJdbcTemplate.query(userSql, (rs, rowNum) -> {
                TableColumn column = new TableColumn();
                column.setTableName("[sys_user_db] " + rs.getString("tableName"));
                column.setColumnName(rs.getString("columnName"));
                column.setColumnType(rs.getString("columnType"));
                column.setIsPrimaryKey(rs.getString("isPrimaryKey"));
                column.setIsNullable(rs.getString("isNullable"));
                column.setColumnDefault(rs.getString("columnDefault"));
                column.setColumnComment(rs.getString("columnComment"));
                return column;
            });
            allColumns.addAll(userColumns);
            
        } catch (DataAccessException e) {
            log.error("Failed to get table columns from primary database: {}", e.getMessage());
        }
        
        try {
            // 按表名分组
            Map<String, List<TableColumn>> grouped = allColumns.stream().collect(Collectors.groupingBy(TableColumn::getTableName));
            // 组装成 List<TableInfoWithColumns>
            return grouped.entrySet().stream().map(entry -> {
                TableInfoWithColumns info = new TableInfoWithColumns();
                info.setTableName(entry.getKey());
                info.setColumns(entry.getValue());
                return info;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to process table columns: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
}