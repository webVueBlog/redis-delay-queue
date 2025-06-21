package com.example.repository;

import com.example.entity.SystemSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 系统设置数据访问接口
 */
@Repository
public interface SystemSettingsRepository extends JpaRepository<SystemSettings, Long> {
    
    /**
     * 根据分类和设置键查找设置
     */
    Optional<SystemSettings> findByCategoryAndSettingKey(String category, String settingKey);
    
    /**
     * 根据分类查找所有设置
     */
    List<SystemSettings> findByCategory(String category);
    
    /**
     * 根据分类查找所有设置（按设置键排序）
     */
    List<SystemSettings> findByCategoryOrderBySettingKey(String category);
    
    /**
     * 查找所有可编辑的设置
     */
    List<SystemSettings> findByIsEditableTrue();
    
    /**
     * 根据分类查找可编辑的设置
     */
    List<SystemSettings> findByCategoryAndIsEditableTrue(String category);
    
    /**
     * 查找所有非敏感设置
     */
    List<SystemSettings> findByIsSensitiveFalse();
    
    /**
     * 根据分类查找非敏感设置
     */
    List<SystemSettings> findByCategoryAndIsSensitiveFalse(String category);
    
    /**
     * 检查设置是否存在
     */
    boolean existsByCategoryAndSettingKey(String category, String settingKey);
    
    /**
     * 删除指定分类的所有设置
     */
    void deleteByCategory(String category);
    
    /**
     * 获取所有分类
     */
    @Query("SELECT DISTINCT s.category FROM SystemSettings s ORDER BY s.category")
    List<String> findAllCategories();
    
    /**
     * 根据设置键模糊查询
     */
    List<SystemSettings> findBySettingKeyContainingIgnoreCase(String keyword);
    
    /**
     * 根据分类和设置键模糊查询
     */
    List<SystemSettings> findByCategoryAndSettingKeyContainingIgnoreCase(String category, String keyword);
    
    /**
     * 获取指定分类的设置数量
     */
    @Query("SELECT COUNT(s) FROM SystemSettings s WHERE s.category = :category")
    long countByCategory(@Param("category") String category);
    
    /**
     * 批量更新设置值
     */
    @Query("UPDATE SystemSettings s SET s.settingValue = :value, s.updatedAt = CURRENT_TIMESTAMP, s.updatedBy = :updatedBy WHERE s.category = :category AND s.settingKey = :key")
    int updateSettingValue(@Param("category") String category, @Param("key") String settingKey, @Param("value") String value, @Param("updatedBy") String updatedBy);
}