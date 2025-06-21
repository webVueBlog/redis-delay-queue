package com.example.service;

import com.example.dto.UserDTO;
import com.example.entity.User;
import com.example.entity.Menu;
import com.example.repository.UserRepository;
import com.example.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService implements UserDetailsService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final MenuService menuService;
    private final OrganizationService organizationService;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));
    }
    
    /**
     * 用户登录
     */
    public UserDTO.LoginResponse login(UserDTO.LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));
        //打印日志返回的user所有
        log.info("用户登录: {}", user);

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        if (user.getStatus() != 1) {
            throw new RuntimeException("账户已被禁用");
        }
        
        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userRepository.save(user);
        
        // 生成JWT token
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole());
        claims.put("organizationIds", user.getOrganizationIdList());
        
        String token = jwtUtil.generateToken(user, claims);
        
        // 获取用户菜单
        List<Menu> menus = getUserMenus(user.getRole());
        
        return new UserDTO.LoginResponse(token, "Bearer", convertToDTO(user), menus);
    }
    
    /**
     * 用户注册
     */
    public UserDTO register(UserDTO.RegisterRequest request) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("邮箱已存在");
        }
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() != null ? request.getRole() : "USER");
        user.setStatus(1);
        
        if (request.getOrganizationIds() != null) {
            user.setOrganizationIdList(request.getOrganizationIds());
        }
        
        User savedUser = userRepository.save(user);
        log.info("用户注册成功: {}", savedUser.getUsername());
        
        return convertToDTO(savedUser);
    }
    
    /**
     * 更新用户信息
     */
    public UserDTO updateUser(Long userId, UserDTO.UpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 检查邮箱是否已被其他用户使用
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("邮箱已被其他用户使用");
            }
            user.setEmail(request.getEmail());
        }
        
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        
        if (request.getOrganizationIds() != null) {
            user.setOrganizationIdList(request.getOrganizationIds());
        }
        
        User updatedUser = userRepository.save(user);
        log.info("用户信息更新成功: {}", updatedUser.getUsername());
        
        return convertToDTO(updatedUser);
    }
    
    /**
     * 分页查询用户
     */
    public UserDTO.PageResponse getUsers(UserDTO.PageRequest request) {
        Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDir()), request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        
        Page<User> userPage = userRepository.findByConditions(
                request.getUsername(),
                request.getEmail(),
                request.getStatus(),
                request.getRole(),
                pageable
        );
        
        List<UserDTO> userDTOs = userPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return new UserDTO.PageResponse(
                userDTOs,
                userPage.getNumber(),
                userPage.getSize(),
                userPage.getTotalElements(),
                userPage.getTotalPages(),
                userPage.isFirst(),
                userPage.isLast()
        );
    }
    
    /**
     * 删除用户
     */
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        userRepository.delete(user);
        log.info("用户删除成功: {}", user.getUsername());
    }
    
    /**
     * 修改密码
     */
    public void changePassword(Long userId, UserDTO.ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }
        
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        log.info("用户密码修改成功: {}", user.getUsername());
    }
    
    /**
     * 重置密码（管理员功能）
     */
    public void resetPassword(UserDTO.ResetPasswordRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        log.info("用户密码重置成功: {}", user.getUsername());
    }
    
    /**
     * 获取用户菜单
     */
    public List<Menu> getUserMenus(String role) {
        return menuService.getUserMenuTree(role);
    }
    
    /**
     * 获取用户权限组织ID
     */
    public List<Long> getUserOrganizationIds(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        List<Long> orgIds = user.getOrganizationIdList();
        if (orgIds == null || orgIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 获取用户组织及其所有子组织的ID
        List<Long> allOrgIds = new ArrayList<>();
        for (Long orgId : orgIds) {
            allOrgIds.add(orgId);
            allOrgIds.addAll(organizationService.getAllChildOrganizationIds(orgId));
        }
        
        return allOrgIds;
    }
    
    /**
     * 根据ID获取用户
     */
    public UserDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        return convertToDTO(user);
    }
    
    /**
     * 根据用户名获取用户
     */
    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        return convertToDTO(user);
    }
    
    /**
     * 转换实体为DTO
     */
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setStatus(user.getStatus());
        dto.setRole(user.getRole());
        dto.setOrganizationIds(user.getOrganizationIdList());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setLastLoginTime(user.getLastLoginTime());
        return dto;
    }
}