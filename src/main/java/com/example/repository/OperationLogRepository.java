package com.example.repository;

import com.example.entity.OperationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OperationLogRepository extends JpaRepository<OperationLog, Long>, JpaSpecificationExecutor<OperationLog> {
    
    /**
     * 根据用户名查询操作日志
     */
    List<OperationLog> findByUsernameOrderByCreatedAtDesc(String username);
    
    /**
     * 根据操作类型查询操作日志
     */
    List<OperationLog> findByActionOrderByCreatedAtDesc(String action);
    
    /**
     * 根据模块查询操作日志
     */
    List<OperationLog> findByModuleOrderByCreatedAtDesc(String module);
    
    /**
     * 根据日志级别查询操作日志
     */
    List<OperationLog> findByLevelOrderByCreatedAtDesc(String level);
    
    /**
     * 根据IP地址查询操作日志
     */
    List<OperationLog> findByIpOrderByCreatedAtDesc(String ip);
    
    /**
     * 根据时间范围查询操作日志
     */
    List<OperationLog> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 根据用户名和时间范围查询操作日志
     */
    List<OperationLog> findByUsernameAndCreatedAtBetweenOrderByCreatedAtDesc(String username, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 统计指定时间范围内的日志数量
     */
    long countByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 统计指定级别的日志数量
     */
    long countByLevel(String level);
    
    /**
     * 统计指定级别和时间范围内的日志数量
     */
    long countByLevelAndCreatedAtBetween(String level, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 统计指定时间范围内的不重复用户数
     */
    @Query("SELECT COUNT(DISTINCT o.username) FROM OperationLog o WHERE o.createdAt BETWEEN :startTime AND :endTime")
    long countDistinctUsernameByCreatedAtBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    /**
     * 统计指定用户的操作次数
     */
    long countByUsername(String username);
    
    /**
     * 统计指定操作类型的次数
     */
    long countByAction(String action);
    
    /**
     * 统计指定模块的操作次数
     */
    long countByModule(String module);
    
    /**
     * 删除指定时间之前的日志
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM OperationLog o WHERE o.createdAt < :beforeTime")
    int deleteByCreatedAtBefore(@Param("beforeTime") LocalDateTime beforeTime);
    
    /**
     * 删除指定用户的日志
     */
    @Modifying
    @Transactional
    int deleteByUsername(String username);
    
    /**
     * 查询最近的操作日志
     */
    @Query("SELECT o FROM OperationLog o ORDER BY o.createdAt DESC")
    List<OperationLog> findRecentLogs(org.springframework.data.domain.Pageable pageable);
    
    /**
     * 查询指定用户的最近操作日志
     */
    @Query("SELECT o FROM OperationLog o WHERE o.username = :username ORDER BY o.createdAt DESC")
    List<OperationLog> findRecentLogsByUsername(@Param("username") String username, org.springframework.data.domain.Pageable pageable);
    
    /**
     * 查询错误日志
     */
    @Query("SELECT o FROM OperationLog o WHERE o.level = 'ERROR' ORDER BY o.createdAt DESC")
    List<OperationLog> findErrorLogs(org.springframework.data.domain.Pageable pageable);
    
    /**
     * 查询指定时间范围内的错误日志
     */
    @Query("SELECT o FROM OperationLog o WHERE o.level = 'ERROR' AND o.createdAt BETWEEN :startTime AND :endTime ORDER BY o.createdAt DESC")
    List<OperationLog> findErrorLogsBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查询慢操作日志（耗时超过指定毫秒数）
     */
    @Query("SELECT o FROM OperationLog o WHERE o.duration > :minDuration ORDER BY o.duration DESC")
    List<OperationLog> findSlowLogs(@Param("minDuration") Long minDuration, org.springframework.data.domain.Pageable pageable);
    
    /**
     * 统计各操作类型的数量
     */
    @Query("SELECT o.action, COUNT(o) FROM OperationLog o GROUP BY o.action ORDER BY COUNT(o) DESC")
    List<Object[]> countByActionGroupBy();
    
    /**
     * 统计各模块的操作数量
     */
    @Query("SELECT o.module, COUNT(o) FROM OperationLog o GROUP BY o.module ORDER BY COUNT(o) DESC")
    List<Object[]> countByModuleGroupBy();
    
    /**
     * 统计各用户的操作数量
     */
    @Query("SELECT o.username, COUNT(o) FROM OperationLog o GROUP BY o.username ORDER BY COUNT(o) DESC")
    List<Object[]> countByUsernameGroupBy();
    
    /**
     * 统计指定时间范围内各操作类型的数量
     */
    @Query("SELECT o.action, COUNT(o) FROM OperationLog o WHERE o.createdAt BETWEEN :startTime AND :endTime GROUP BY o.action ORDER BY COUNT(o) DESC")
    List<Object[]> countByActionGroupByBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    /**
     * 统计指定时间范围内各模块的操作数量
     */
    @Query("SELECT o.module, COUNT(o) FROM OperationLog o WHERE o.createdAt BETWEEN :startTime AND :endTime GROUP BY o.module ORDER BY COUNT(o) DESC")
    List<Object[]> countByModuleGroupByBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    /**
     * 统计指定时间范围内各用户的操作数量
     */
    @Query("SELECT o.username, COUNT(o) FROM OperationLog o WHERE o.createdAt BETWEEN :startTime AND :endTime GROUP BY o.username ORDER BY COUNT(o) DESC")
    List<Object[]> countByUsernameGroupByBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查询指定业务ID的操作日志
     */
    List<OperationLog> findByBusinessIdAndBusinessTypeOrderByCreatedAtDesc(Long businessId, String businessType);
    
    /**
     * 查询指定会话ID的操作日志
     */
    List<OperationLog> findBySessionIdOrderByCreatedAtDesc(String sessionId);
    
    /**
     * 查询指定来源的操作日志
     */
    List<OperationLog> findBySourceOrderByCreatedAtDesc(String source);
    
    /**
     * 查询指定租户的操作日志
     */
    List<OperationLog> findByTenantIdOrderByCreatedAtDesc(Long tenantId);
    
    /**
     * 根据描述模糊查询
     */
    @Query("SELECT o FROM OperationLog o WHERE o.description LIKE %:keyword% ORDER BY o.createdAt DESC")
    List<OperationLog> findByDescriptionContaining(@Param("keyword") String keyword);
    
    /**
     * 复合条件查询
     */
    @Query("SELECT o FROM OperationLog o WHERE " +
           "(:username IS NULL OR o.username LIKE %:username%) AND " +
           "(:action IS NULL OR o.action = :action) AND " +
           "(:module IS NULL OR o.module = :module) AND " +
           "(:level IS NULL OR o.level = :level) AND " +
           "(:startTime IS NULL OR o.createdAt >= :startTime) AND " +
           "(:endTime IS NULL OR o.createdAt <= :endTime) " +
           "ORDER BY o.createdAt DESC")
    List<OperationLog> findByConditions(
        @Param("username") String username,
        @Param("action") String action,
        @Param("module") String module,
        @Param("level") String level,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        org.springframework.data.domain.Pageable pageable
    );
}