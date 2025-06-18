package com.example.model;

import lombok.Data;

@Data
public class TableInfo {
    private String tableName;
    private String engine;
    private Long dataLength;
    private Long indexLength;
    private Long tableSize;
    private Double dataSpacePercentage;
} 