package com.example.service;

import com.example.entity.Menu;
import com.example.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MenuService {
    
    private final MenuRepository menuRepository;
    
    /**
     * 获取用户菜单树（根据角色）
     */
    public List<Menu> getUserMenuTree(String role) {
        // 获取根菜单
        List<Menu> rootMenus = menuRepository.findRootMenusByRole(role);
        
        // 为每个根菜单构建子菜单树
        for (Menu rootMenu : rootMenus) {
            buildMenuTree(rootMenu, role);
        }
        
        return rootMenus;
    }
    
    /**
     * 递归构建菜单树
     */
    private void buildMenuTree(Menu parentMenu, String role) {
        List<Menu> children = menuRepository.findChildMenusByParentIdAndRole(parentMenu.getId(), role);
        parentMenu.setChildren(children);
        
        // 递归构建子菜单的子菜单
        for (Menu child : children) {
            buildMenuTree(child, role);
        }
    }
    
    /**
     * 获取所有菜单（管理员用）
     */
    public List<Menu> getAllMenus() {
        return menuRepository.findByStatusOrderBySortOrder(1);
    }
    
    /**
     * 获取菜单树（管理员用）
     */
    public List<Menu> getMenuTree() {
        List<Menu> rootMenus = menuRepository.findByParentIdIsNullOrderBySortOrder();
        
        for (Menu rootMenu : rootMenus) {
            buildFullMenuTree(rootMenu);
        }
        
        return rootMenus;
    }
    
    /**
     * 构建完整菜单树（不考虑角色限制）
     */
    private void buildFullMenuTree(Menu parentMenu) {
        List<Menu> children = menuRepository.findByParentIdOrderBySortOrder(parentMenu.getId());
        parentMenu.setChildren(children);
        
        for (Menu child : children) {
            buildFullMenuTree(child);
        }
    }
    
    /**
     * 分页查询菜单
     */
    public Page<Menu> getMenus(String name, String code, Integer status, String requiredRole, Pageable pageable) {
        return menuRepository.findByConditions(name, code, status, requiredRole, pageable);
    }
    
    /**
     * 根据ID获取菜单
     */
    public Menu getMenuById(Long id) {
        return menuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("菜单不存在"));
    }
    
    /**
     * 根据编码获取菜单
     */
    public Menu getMenuByCode(String code) {
        return menuRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("菜单不存在"));
    }
    
    /**
     * 创建菜单
     */
    public Menu createMenu(Menu menu) {
        // 检查编码是否已存在
        if (menuRepository.existsByCode(menu.getCode())) {
            throw new RuntimeException("菜单编码已存在");
        }
        
        // 验证父菜单是否存在
        if (menu.getParentId() != null) {
            if (!menuRepository.existsById(menu.getParentId())) {
                throw new RuntimeException("父菜单不存在");
            }
        }
        
        Menu savedMenu = menuRepository.save(menu);
        log.info("菜单创建成功: {}", savedMenu.getName());
        return savedMenu;
    }
    
    /**
     * 更新菜单
     */
    public Menu updateMenu(Long id, Menu menuUpdate) {
        Menu existingMenu = getMenuById(id);
        
        // 检查编码是否被其他菜单使用
        if (!existingMenu.getCode().equals(menuUpdate.getCode()) && 
            menuRepository.existsByCode(menuUpdate.getCode())) {
            throw new RuntimeException("菜单编码已被其他菜单使用");
        }
        
        // 验证父菜单
        if (menuUpdate.getParentId() != null) {
            if (menuUpdate.getParentId().equals(id)) {
                throw new RuntimeException("不能将自己设置为父菜单");
            }
            if (!menuRepository.existsById(menuUpdate.getParentId())) {
                throw new RuntimeException("父菜单不存在");
            }
        }
        
        // 更新字段
        existingMenu.setName(menuUpdate.getName());
        existingMenu.setCode(menuUpdate.getCode());
        existingMenu.setPath(menuUpdate.getPath());
        existingMenu.setIcon(menuUpdate.getIcon());
        existingMenu.setParentId(menuUpdate.getParentId());
        existingMenu.setSortOrder(menuUpdate.getSortOrder());
        existingMenu.setStatus(menuUpdate.getStatus());
        existingMenu.setRequiredRole(menuUpdate.getRequiredRole());
        
        Menu updatedMenu = menuRepository.save(existingMenu);
        log.info("菜单更新成功: {}", updatedMenu.getName());
        return updatedMenu;
    }
    
    /**
     * 删除菜单
     */
    public void deleteMenu(Long id) {
        Menu menu = getMenuById(id);
        
        // 检查是否有子菜单
        List<Menu> children = menuRepository.findByParentIdOrderBySortOrder(id);
        if (!children.isEmpty()) {
            throw new RuntimeException("存在子菜单，无法删除");
        }
        
        menuRepository.delete(menu);
        log.info("菜单删除成功: {}", menu.getName());
    }
    
    /**
     * 获取用户菜单代码列表（用于权限验证）
     */
    public List<String> getUserMenuCodes(String role) {
        List<Menu> menus = menuRepository.findByRole(role);
        return menus.stream()
                .map(Menu::getCode)
                .collect(Collectors.toList());
    }
}