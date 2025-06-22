-- 用户管理系统初始化数据

-- 创建用户表（如果不存在）
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    email VARCHAR(100) COMMENT '邮箱地址',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常，0-禁用',
    role VARCHAR(50) DEFAULT 'USER' COMMENT '角色：USER, ADMIN',
    organization_ids VARCHAR(500) COMMENT '组织ID列表，用逗号分隔',
    last_login_time TIMESTAMP NULL COMMENT '最后登录时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 插入默认管理员用户（密码：admin123）
INSERT IGNORE INTO users (username, email, password, role, status) VALUES 
('admin', 'admin@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBaLO.oj2R6YzO', 'ADMIN', 1);

-- 插入测试用户（密码：user123）
INSERT IGNORE INTO users (username, email, password, role, status, organization_ids) VALUES 
('testuser', 'user@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'USER', 1, '1,2,3');

-- 插入更多测试用户
INSERT IGNORE INTO users (username, email, password, role, status, organization_ids) VALUES 
('user1', 'user1@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'USER', 1, '1'),
('user2', 'user2@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'USER', 1, '2'),
('user3', 'user3@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'USER', 0, '3'),
('manager', 'manager@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'ADMIN', 1, '1,2');

-- 创建组织表（示例）
CREATE TABLE IF NOT EXISTS organizations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '组织ID',
    name VARCHAR(100) NOT NULL COMMENT '组织名称',
    description TEXT COMMENT '组织描述',
    parent_id BIGINT COMMENT '父组织ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常，0-禁用'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='组织表';

-- 插入示例组织数据
INSERT IGNORE INTO organizations (id, name, description, parent_id) VALUES 
(1, '总公司', '集团总部', NULL),
(2, '技术部', '负责技术研发', 1),
(3, '市场部', '负责市场营销', 1),
(4, '前端组', '前端开发团队', 2),
(5, '后端组', '后端开发团队', 2);

-- 创建菜单表（示例）
CREATE TABLE IF NOT EXISTS menus (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '菜单ID',
    name VARCHAR(100) NOT NULL COMMENT '菜单名称',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '菜单编码',
    path VARCHAR(200) COMMENT '菜单路径',
    icon VARCHAR(50) COMMENT '菜单图标',
    parent_id BIGINT COMMENT '父菜单ID',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常，0-禁用',
    required_role VARCHAR(50) COMMENT '所需角色',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- 插入菜单数据
INSERT IGNORE INTO menus (name, code, path, icon, parent_id, sort_order, required_role) VALUES 
('仪表盘', 'dashboard', '/dashboard', 'dashboard', NULL, 1, 'USER'),
('个人中心', 'profile', '/profile', 'user', NULL, 2, 'USER'),
('表格查看器', 'table-viewer', '/table-viewer', 'table', NULL, 3, 'USER'),
('用户管理', 'user-management', '/user-management', 'users', NULL, 4, 'ADMIN'),
('菜单管理', 'menu-management', '/menus', 'menu', NULL, 5, 'ADMIN'),
('操作日志', 'operation-logs', '/operation-logs', 'document', NULL, 6, 'ADMIN'),
('系统设置', 'system-settings', '/system-settings', 'settings', NULL, 7, 'ADMIN'),
('健康检查', 'health-check', '/health-check', 'monitor', NULL, 8, 'ADMIN');

-- 创建角色权限表（示例）
CREATE TABLE IF NOT EXISTS role_permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '权限ID',
    role VARCHAR(50) NOT NULL COMMENT '角色',
    permission VARCHAR(100) NOT NULL COMMENT '权限',
    description VARCHAR(200) COMMENT '权限描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限表';

-- 插入角色权限数据
INSERT IGNORE INTO role_permissions (role, permission, description) VALUES 
('USER', 'user:read', '查看用户信息'),
('USER', 'user:update', '更新用户信息'),
('USER', 'table:read', '查看表格数据'),
('ADMIN', 'user:read', '查看用户信息'),
('ADMIN', 'user:create', '创建用户'),
('ADMIN', 'user:update', '更新用户信息'),
('ADMIN', 'user:delete', '删除用户'),
('ADMIN', 'user:reset-password', '重置用户密码'),
('ADMIN', 'system:read', '查看系统信息'),
('ADMIN', 'system:update', '更新系统设置'),
('ADMIN', 'table:read', '查看表格数据'),
('ADMIN', 'health:read', '查看健康状态');

-- 创建系统设置表
CREATE TABLE IF NOT EXISTS system_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    category VARCHAR(50) NOT NULL COMMENT '设置分类',
    setting_key VARCHAR(100) NOT NULL COMMENT '设置键',
    setting_value TEXT COMMENT '设置值',
    description VARCHAR(500) COMMENT '设置描述',
    data_type VARCHAR(20) DEFAULT 'STRING' COMMENT '数据类型',
    is_sensitive BOOLEAN DEFAULT FALSE COMMENT '是否敏感数据',
    is_editable BOOLEAN DEFAULT TRUE COMMENT '是否可编辑',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by VARCHAR(100) COMMENT '创建者',
    updated_by VARCHAR(100) COMMENT '更新者',
    UNIQUE KEY uk_category_key (category, setting_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统设置表';

-- 插入默认系统设置数据
-- 基础设置
INSERT IGNORE INTO system_settings (category, setting_key, setting_value, description, data_type, created_by, updated_by) VALUES 
('basic', 'systemName', 'Redis延迟队列系统', '系统名称', 'STRING', 'system', 'system'),
('basic', 'systemDescription', '基于Redis的延迟队列管理系统', '系统描述', 'STRING', 'system', 'system'),
('basic', 'systemVersion', '1.0.0', '系统版本', 'STRING', 'system', 'system'),
('basic', 'adminEmail', 'admin@example.com', '管理员邮箱', 'STRING', 'system', 'system'),
('basic', 'timezone', 'Asia/Shanghai', '时区设置', 'STRING', 'system', 'system'),
('basic', 'logLevel', 'INFO', '日志级别', 'STRING', 'system', 'system');

-- Redis配置
INSERT IGNORE INTO system_settings (category, setting_key, setting_value, description, data_type, created_by, updated_by) VALUES 
('redis', 'host', 'localhost', 'Redis主机地址', 'STRING', 'system', 'system'),
('redis', 'port', '6379', 'Redis端口', 'INTEGER', 'system', 'system'),
('redis', 'database', '0', 'Redis数据库索引', 'INTEGER', 'system', 'system'),
('redis', 'password', '', 'Redis密码', 'STRING', 'system', 'system'),
('redis', 'timeout', '5000', '连接超时时间(ms)', 'INTEGER', 'system', 'system'),
('redis', 'maxActive', '20', '最大活跃连接数', 'INTEGER', 'system', 'system'),
('redis', 'maxIdle', '10', '最大空闲连接数', 'INTEGER', 'system', 'system'),
('redis', 'minIdle', '5', '最小空闲连接数', 'INTEGER', 'system', 'system');

-- 队列设置
INSERT IGNORE INTO system_settings (category, setting_key, setting_value, description, data_type, created_by, updated_by) VALUES 
('queue', 'defaultQueueName', 'default', '默认队列名称', 'STRING', 'system', 'system'),
('queue', 'maxRetryCount', '3', '最大重试次数', 'INTEGER', 'system', 'system'),
('queue', 'taskTimeout', '300', '任务超时时间(秒)', 'INTEGER', 'system', 'system'),
('queue', 'batchSize', '100', '批处理大小', 'INTEGER', 'system', 'system'),
('queue', 'scanInterval', '1000', '扫描间隔(ms)', 'INTEGER', 'system', 'system'),
('queue', 'enablePriority', 'true', '启用优先级', 'BOOLEAN', 'system', 'system');

-- 安全设置
INSERT IGNORE INTO system_settings (category, setting_key, setting_value, description, data_type, is_sensitive, created_by, updated_by) VALUES 
('security', 'jwtSecret', 'mySecretKey', 'JWT密钥', 'STRING', TRUE, 'system', 'system'),
('security', 'tokenExpiration', '86400', 'Token过期时间(秒)', 'INTEGER', FALSE, 'system', 'system'),
('security', 'passwordMinLength', '6', '密码最小长度', 'INTEGER', FALSE, 'system', 'system'),
('security', 'loginFailureLockCount', '5', '登录失败锁定次数', 'INTEGER', FALSE, 'system', 'system'),
('security', 'enableTwoFactor', 'false', '启用双因子认证', 'BOOLEAN', FALSE, 'system', 'system'),
('security', 'ipWhitelist', '', 'IP白名单', 'STRING', FALSE, 'system', 'system');

-- 通知设置
INSERT IGNORE INTO system_settings (category, setting_key, setting_value, description, data_type, is_sensitive, created_by, updated_by) VALUES 
('notification', 'enableEmailNotification', 'false', '启用邮件通知', 'BOOLEAN', FALSE, 'system', 'system'),
('notification', 'smtpHost', '', 'SMTP主机', 'STRING', FALSE, 'system', 'system'),
('notification', 'smtpPort', '587', 'SMTP端口', 'INTEGER', FALSE, 'system', 'system'),
('notification', 'smtpUsername', '', 'SMTP用户名', 'STRING', FALSE, 'system', 'system'),
('notification', 'smtpPassword', '', 'SMTP密码', 'STRING', TRUE, 'system', 'system'),
('notification', 'enableWebhookNotification', 'false', '启用Webhook通知', 'BOOLEAN', FALSE, 'system', 'system'),
('notification', 'webhookUrl', '', 'Webhook URL', 'STRING', FALSE, 'system', 'system'),
('notification', 'notificationEvents', 'task_failed,task_completed', '通知事件', 'STRING', FALSE, 'system', 'system');

-- 性能优化设置
INSERT IGNORE INTO system_settings (category, setting_key, setting_value, description, data_type, created_by, updated_by) VALUES 
('performance', 'threadPoolSize', '10', '线程池大小', 'INTEGER', 'system', 'system'),
('performance', 'queueCapacity', '1000', '队列容量', 'INTEGER', 'system', 'system'),
('performance', 'enableCache', 'true', '启用缓存', 'BOOLEAN', 'system', 'system'),
('performance', 'enableCompression', 'false', '启用压缩', 'BOOLEAN', 'system', 'system'),
('performance', 'connectionPoolSize', '20', '连接池大小', 'INTEGER', 'system', 'system'),
('performance', 'maxConcurrentRequests', '100', '最大并发请求数', 'INTEGER', 'system', 'system'),
('performance', 'requestTimeout', '30', '请求超时时间(秒)', 'INTEGER', 'system', 'system');

-- 创建索引
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_system_settings_category ON system_settings(category);
CREATE INDEX idx_system_settings_key ON system_settings(setting_key);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_created_at ON users(created_at);
CREATE INDEX idx_organizations_parent_id ON organizations(parent_id);
CREATE INDEX idx_menus_parent_id ON menus(parent_id);
CREATE INDEX idx_role_permissions_role ON role_permissions(role);

-- 显示创建结果
SELECT 'Database initialization completed successfully!' as message;