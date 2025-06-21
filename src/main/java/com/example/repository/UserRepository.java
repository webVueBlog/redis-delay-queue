package com.example.repository;

import com.example.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);
    
    /**
     * 根据状态查找用户
     */
    Page<User> findByStatus(Integer status, Pageable pageable);
    
    /**
     * 根据角色查找用户
     */
    Page<User> findByRole(String role, Pageable pageable);
    
    /**
     * 根据用户名模糊查询
     */
    Page<User> findByUsernameContainingIgnoreCase(String username, Pageable pageable);
    
    /**
     * 统计指定时间范围内创建的用户数量
     */
    long countByCreatedAtBetween(java.time.LocalDateTime startTime, java.time.LocalDateTime endTime);
    
    /**
     * 根据邮箱模糊查询
     */
    Page<User> findByEmailContainingIgnoreCase(String email, Pageable pageable);
    
    /**
     * 多条件查询用户
     */
    @Query("SELECT u FROM User u WHERE " +
           "(:username IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%'))) AND " +
           "(:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
           "(:status IS NULL OR u.status = :status) AND " +
           "(:role IS NULL OR u.role = :role)")
    Page<User> findByConditions(@Param("username") String username,
                               @Param("email") String email,
                               @Param("status") Integer status,
                               @Param("role") String role,
                               Pageable pageable);
    
    /**
     * 根据组织ID查找用户
     */
    @Query("SELECT u FROM User u WHERE u.organizationIds LIKE CONCAT('%', :orgId, '%')")
    Page<User> findByOrganizationId(@Param("orgId") String orgId, Pageable pageable);
    
    /**
     * 统计用户总数
     */
    long count();
    
    /**
     * 统计活跃用户数
     */
    long countByStatus(Integer status);
    
    /**
     * 统计各角色用户数
     */
    @Query("SELECT u.role, COUNT(u) FROM User u GROUP BY u.role")
    Object[][] countByRole();
}