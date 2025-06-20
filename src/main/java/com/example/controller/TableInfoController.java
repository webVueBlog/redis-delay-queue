package com.example.controller;

import com.example.model.TableInfo;
import com.example.model.TableColumn;
import com.example.model.TableInfoWithColumns;
import com.example.service.TableInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tables")
public class TableInfoController {

    @Autowired
    private TableInfoService tableInfoService;

    @GetMapping("/info")
    public List<TableInfo> getTableInfo() {
        return tableInfoService.getTableInfo();
    }

    @GetMapping("/columns")
    public List<TableInfoWithColumns> getAllTableColumns() {
        return tableInfoService.getAllTableColumns();
    }
} 