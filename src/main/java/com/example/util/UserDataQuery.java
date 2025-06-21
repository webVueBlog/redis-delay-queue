package com.example.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
public class UserDataQuery {

    @Autowired
    @Qualifier("userJdbcTemplate")
    private JdbcTemplate userJdbcTemplate;

    @GetMapping("/users")
    public Map<String, Object> queryUserData() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 查询所有用户数据
            String sql = "SELECT id, username, email, password, role, status, organization_ids, created_at, updated_at FROM users";
            List<Map<String, Object>> users = userJdbcTemplate.queryForList(sql);
            
            result.put("success", true);
            result.put("message", "查询成功");
            result.put("total", users.size());
            result.put("data", users);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "查询失败: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
        }
        
        return result;
    }
}