package com.example.service;

import com.example.entity.Organization;
import com.example.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrganizationService {
    
    private final OrganizationRepository organizationRepository;
    
    /**
     * 获取组织树
     */
    public List<Organization> getOrganizationTree() {
        try {
            log.info("开始查询根组织");
            List<Organization> rootOrganizations = organizationRepository.findByParentIdIsNull();
            log.info("找到{}个根组织", rootOrganizations.size());
            
            for (Organization rootOrg : rootOrganizations) {
                log.debug("构建组织树: {}", rootOrg.getName());
                buildOrganizationTree(rootOrg);
            }
            
            log.info("组织树构建完成");
            return rootOrganizations;
        } catch (Exception e) {
            log.error("获取组织树时发生错误: {}", e.getMessage(), e);
            throw new RuntimeException("获取组织树失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 递归构建组织树
     */
    private void buildOrganizationTree(Organization parentOrg) {
        List<Organization> children = organizationRepository.findByParentId(parentOrg.getId());
        parentOrg.setChildren(children);
        
        for (Organization child : children) {
            buildOrganizationTree(child);
        }
    }
    
    /**
     * 获取所有组织
     */
    public List<Organization> getAllOrganizations() {
        return organizationRepository.findByStatus(1);
    }
    
    /**
     * 分页查询组织
     */
    public Page<Organization> getOrganizations(String name, Integer status, Pageable pageable) {
        return organizationRepository.findByConditions(name, status, pageable);
    }
    
    /**
     * 根据ID获取组织
     */
    public Organization getOrganizationById(Long id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("组织不存在"));
    }
    
    /**
     * 创建组织
     */
    public Organization createOrganization(Organization organization) {
        // 验证父组织是否存在
        if (organization.getParentId() != null) {
            if (!organizationRepository.existsById(organization.getParentId())) {
                throw new RuntimeException("父组织不存在");
            }
        }
        
        Organization savedOrganization = organizationRepository.save(organization);
        log.info("组织创建成功: {}", savedOrganization.getName());
        return savedOrganization;
    }
    
    /**
     * 更新组织
     */
    public Organization updateOrganization(Long id, Organization organizationUpdate) {
        Organization existingOrganization = getOrganizationById(id);
        
        // 验证父组织
        if (organizationUpdate.getParentId() != null) {
            if (organizationUpdate.getParentId().equals(id)) {
                throw new RuntimeException("不能将自己设置为父组织");
            }
            if (!organizationRepository.existsById(organizationUpdate.getParentId())) {
                throw new RuntimeException("父组织不存在");
            }
            
            // 检查是否会形成循环引用
            if (isCircularReference(id, organizationUpdate.getParentId())) {
                throw new RuntimeException("不能设置子组织为父组织，会形成循环引用");
            }
        }
        
        // 更新字段
        existingOrganization.setName(organizationUpdate.getName());
        existingOrganization.setDescription(organizationUpdate.getDescription());
        existingOrganization.setParentId(organizationUpdate.getParentId());
        existingOrganization.setStatus(organizationUpdate.getStatus());
        
        Organization updatedOrganization = organizationRepository.save(existingOrganization);
        log.info("组织更新成功: {}", updatedOrganization.getName());
        return updatedOrganization;
    }
    
    /**
     * 检查是否会形成循环引用
     */
    private boolean isCircularReference(Long orgId, Long parentId) {
        if (parentId == null) {
            return false;
        }
        
        List<Long> childIds = organizationRepository.findAllChildOrganizationIds(orgId);
        return childIds.contains(parentId);
    }
    
    /**
     * 删除组织
     */
    public void deleteOrganization(Long id) {
        Organization organization = getOrganizationById(id);
        
        // 检查是否有子组织
        List<Organization> children = organizationRepository.findByParentId(id);
        if (!children.isEmpty()) {
            throw new RuntimeException("存在子组织，无法删除");
        }
        
        organizationRepository.delete(organization);
        log.info("组织删除成功: {}", organization.getName());
    }
    
    /**
     * 获取组织及其所有子组织的ID列表
     */
    public List<Long> getAllChildOrganizationIds(Long orgId) {
        return organizationRepository.findAllChildOrganizationIds(orgId);
    }
    
    /**
     * 根据ID列表获取组织列表
     */
    public List<Organization> getOrganizationsByIds(List<Long> ids) {
        return organizationRepository.findAllById(ids);
    }
    
    /**
     * 根据父组织ID获取子组织
     */
    public List<Organization> getChildOrganizations(Long parentId) {
        return organizationRepository.findByParentId(parentId);
    }
}