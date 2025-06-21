package com.example.repository;

import com.example.entity.Menu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {
    
    /**
     * 根据编码查找菜单
     */
    Optional<Menu> findByCode(String code);
    
    /**
     * 检查编码是否存在
     */
    boolean existsByCode(String code);
    
    /**
     * 根据父菜单ID查找子菜单，按排序字段排序
     */
    List<Menu> findByParentIdOrderBySortOrder(Long parentId);
    
    /**
     * 查找根菜单，按排序字段排序
     */
    List<Menu> findByParentIdIsNullOrderBySortOrder();
    
    /**
     * 根据状态查找菜单，按排序字段排序
     */
    List<Menu> findByStatusOrderBySortOrder(Integer status);
    
    /**
     * 根据角色查找菜单
     */
    @Query("SELECT m FROM Menu m WHERE m.status = 1 AND (m.requiredRole IS NULL OR m.requiredRole = :role) ORDER BY m.sortOrder")
    List<Menu> findByRole(@Param("role") String role);
    
    /**
     * 根据角色查找根菜单
     */
    @Query("SELECT m FROM Menu m WHERE m.parentId IS NULL AND (m.requiredRole = :role OR m.requiredRole = 'USER') AND m.status = 1 ORDER BY m.sortOrder")
    List<Menu> findRootMenusByRole(@Param("role") String role);
    
    /**
     * 根据父菜单ID和角色查找子菜单
     */
    @Query("SELECT m FROM Menu m WHERE m.parentId = :parentId AND (m.requiredRole = :role OR m.requiredRole = 'USER') AND m.status = 1 ORDER BY m.sortOrder")
    List<Menu> findChildMenusByParentIdAndRole(@Param("parentId") Long parentId, @Param("role") String role);
    
    /**
     * 根据名称模糊查询
     */
    Page<Menu> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    /**
     * 多条件查询菜单
     */
    @Query("SELECT m FROM Menu m WHERE " +
           "(:name IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:code IS NULL OR LOWER(m.code) LIKE LOWER(CONCAT('%', :code, '%'))) AND " +
           "(:status IS NULL OR m.status = :status) AND " +
           "(:requiredRole IS NULL OR m.requiredRole = :requiredRole)")
    Page<Menu> findByConditions(@Param("name") String name,
                               @Param("code") String code,
                               @Param("status") Integer status,
                               @Param("requiredRole") String requiredRole,
                               Pageable pageable);
}