package com.example.repository;

import com.example.entity.Organization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    
    /**
     * 根据名称查找组织
     */
    List<Organization> findByName(String name);
    
    /**
     * 检查名称是否存在
     */
    boolean existsByName(String name);
    
    /**
     * 根据父组织ID查找子组织
     */
    List<Organization> findByParentId(Long parentId);
    
    /**
     * 查找根组织（父组织ID为空）
     */
    List<Organization> findByParentIdIsNull();
    
    /**
     * 根据状态查找组织
     */
    List<Organization> findByStatus(Integer status);
    
    /**
     * 根据名称模糊查询
     */
    Page<Organization> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    /**
     * 多条件查询组织
     */
    @Query("SELECT o FROM Organization o WHERE " +
           "(:name IS NULL OR LOWER(o.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:status IS NULL OR o.status = :status) ORDER BY o.id")
    Page<Organization> findByConditions(@Param("name") String name, @Param("status") Integer status, Pageable pageable);
    
    /**
     * 检查组织名称是否已存在（排除指定ID）
     */
    @Query("SELECT COUNT(o) > 0 FROM Organization o WHERE o.name = :name AND (:id IS NULL OR o.id != :id)")
    boolean existsByNameAndIdNot(@Param("name") String name, @Param("id") Long id);
    
    /**
     * 查找组织及其所有子组织的ID
     */
    @Query(value = "WITH RECURSIVE org_tree AS (" +
                   "  SELECT id, parent_id FROM organizations WHERE id = :orgId " +
                   "  UNION ALL " +
                   "  SELECT o.id, o.parent_id FROM organizations o " +
                   "  INNER JOIN org_tree ot ON o.parent_id = ot.id " +
                   ") SELECT id FROM org_tree", nativeQuery = true)
    List<Long> findAllChildOrganizationIds(@Param("orgId") Long orgId);
}