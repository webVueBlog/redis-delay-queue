# 设备注册中心

## 功能概述

设备注册中心是一个基于Redis的设备管理系统，提供设备注册、状态监控、心跳检测等功能。支持多种设备类型的统一管理，实现设备的全生命周期监控。

## 主要功能

### 1. 设备注册
- 支持设备基本信息注册（设备ID、名称、类型、IP地址等）
- 设备配置信息管理
- 设备标签分类
- 设备组织归属管理

### 2. 设备状态监控
- 实时设备状态显示（在线/离线/维护/错误）
- 设备心跳监控
- 设备上线/离线时间记录
- 设备在线率统计

### 3. 设备管理
- 设备信息查询和搜索
- 设备信息更新
- 设备删除
- 批量设备操作

### 4. 统计分析
- 设备总数统计
- 在线/离线设备数量
- 设备类型分布
- 厂商设备分布
- 组织设备分布

## 技术架构

### 后端技术栈
- **Spring Boot 2.x** - 主框架
- **Spring Data JPA** - 数据持久化
- **Redis** - 缓存和会话存储
- **MySQL** - 主数据库
- **Jackson** - JSON序列化
- **Lombok** - 代码简化
- **Validation** - 数据校验

### 前端技术栈
- **Vue 3** - 前端框架
- **Element Plus** - UI组件库
- **Axios** - HTTP客户端
- **Vue Router** - 路由管理

## 数据模型

### 设备实体 (Device)
```java
- id: 主键ID
- deviceId: 设备唯一标识
- deviceName: 设备名称
- deviceType: 设备类型 (SENSOR/CONTROLLER/GATEWAY/CAMERA)
- status: 设备状态 (ONLINE/OFFLINE/MAINTENANCE/ERROR)
- ipAddress: IP地址
- port: 端口号
- macAddress: MAC地址
- serialNumber: 序列号
- manufacturer: 厂商
- model: 型号
- firmwareVersion: 固件版本
- location: 位置
- organizationId: 组织ID
- description: 描述
- configData: 配置数据 (JSON)
- tags: 标签
- lastHeartbeat: 最后心跳时间
- lastOnlineTime: 最后上线时间
- lastOfflineTime: 最后离线时间
- enabled: 是否启用
- createdAt: 创建时间
- updatedAt: 更新时间
```

## API接口

### 设备注册
```http
POST /api/devices/register
Content-Type: application/json

{
  "deviceId": "DEVICE_001",
  "deviceName": "温度传感器01",
  "deviceType": "SENSOR",
  "ipAddress": "192.168.1.100",
  "port": 8080,
  "manufacturer": "华为",
  "model": "HW-TEMP-001",
  "location": "机房A-01"
}
```

### 设备心跳
```http
POST /api/devices/heartbeat
Content-Type: application/json

{
  "deviceId": "DEVICE_001",
  "status": "ONLINE",
  "ipAddress": "192.168.1.100"
}
```

### 查询设备列表
```http
GET /api/devices?page=1&size=20&deviceType=SENSOR&status=ONLINE
```

### 获取设备详情
```http
GET /api/devices/{deviceId}
```

### 更新设备信息
```http
PUT /api/devices/{deviceId}
Content-Type: application/json

{
  "deviceName": "温度传感器01-更新",
  "location": "机房B-01"
}
```

### 删除设备
```http
DELETE /api/devices/{deviceId}
```

### 获取统计信息
```http
GET /api/devices/statistics
```

## Redis缓存策略

### 缓存键设计
- `device:{deviceId}` - 设备信息缓存
- `device:heartbeat:{deviceId}` - 设备心跳时间缓存
- `device:status:{deviceId}` - 设备状态缓存

### 缓存过期时间
- 设备信息缓存：30分钟
- 心跳时间缓存：30分钟
- 状态缓存：30分钟

## 心跳检测机制

### 心跳超时检测
- 定时任务每分钟执行一次
- 心跳超时时间：5分钟
- 超时后自动将设备状态设置为离线

### 心跳处理流程
1. 接收设备心跳请求
2. 验证设备是否存在
3. 更新设备状态和心跳时间
4. 记录状态变化日志
5. 更新Redis缓存

## 部署说明

### 环境要求
- JDK 8+
- MySQL 5.7+
- Redis 5.0+
- Node.js 14+

### 数据库配置
```sql
-- 创建设备表
CREATE TABLE device (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id VARCHAR(100) UNIQUE NOT NULL,
    device_name VARCHAR(200) NOT NULL,
    device_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OFFLINE',
    ip_address VARCHAR(45),
    port INT,
    mac_address VARCHAR(17),
    serial_number VARCHAR(100),
    manufacturer VARCHAR(100),
    model VARCHAR(100),
    firmware_version VARCHAR(50),
    location VARCHAR(200),
    organization_id BIGINT,
    description TEXT,
    config_data JSON,
    tags VARCHAR(500),
    last_heartbeat DATETIME,
    last_online_time DATETIME,
    last_offline_time DATETIME,
    enabled BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    INDEX idx_device_id (device_id),
    INDEX idx_device_type (device_type),
    INDEX idx_status (status),
    INDEX idx_organization_id (organization_id),
    INDEX idx_last_heartbeat (last_heartbeat)
);
```

### 应用配置
```yaml
# application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/redis_delay_queue?useUnicode=true&characterEncoding=utf8&useSSL=false
    username: root
    password: your_password
  
  redis:
    host: localhost
    port: 6379
    password: your_redis_password
    database: 0
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

## 使用指南

### 1. 访问设备注册中心
- 登录系统后，在左侧菜单选择「监控管理」→「设备注册中心」

### 2. 注册新设备
- 点击「注册设备」按钮
- 填写设备基本信息
- 点击「注册设备」完成注册

### 3. 查看设备列表
- 支持按设备ID、名称、类型、状态等条件搜索
- 实时显示设备在线状态
- 支持分页浏览

### 4. 设备详情查看
- 点击设备列表中的「详情」按钮
- 查看设备完整信息和状态历史

### 5. 设备管理操作
- 编辑设备信息
- 删除设备
- 批量操作

## 监控告警

### 设备离线告警
- 当设备心跳超时时，系统自动将设备状态设置为离线
- 可配置离线设备告警通知

### 统计监控
- 实时显示设备总数、在线数、离线数
- 计算设备在线率
- 按类型、厂商、组织统计设备分布

## 扩展功能

### 1. 设备分组管理
- 支持按组织、位置、类型等维度分组
- 分组权限控制

### 2. 设备配置管理
- 远程配置下发
- 配置版本管理
- 配置回滚

### 3. 设备日志收集
- 设备运行日志收集
- 日志分析和检索
- 异常日志告警

### 4. 设备固件管理
- 固件版本管理
- 远程固件升级
- 升级进度监控

## 故障排查

### 常见问题

1. **设备注册失败**
   - 检查设备ID是否重复
   - 验证必填字段是否完整
   - 确认数据库连接正常

2. **设备状态不更新**
   - 检查Redis连接状态
   - 验证心跳接口调用
   - 查看定时任务执行日志

3. **页面加载缓慢**
   - 检查数据库查询性能
   - 优化分页查询
   - 增加适当的数据库索引

### 日志查看
```bash
# 查看应用日志
tail -f logs/application.log

# 查看设备心跳日志
grep "设备心跳" logs/application.log

# 查看设备注册日志
grep "设备注册" logs/application.log
```

## 性能优化

### 数据库优化
- 添加适当的索引
- 定期清理历史数据
- 分库分表（大规模部署）

### Redis优化
- 合理设置缓存过期时间
- 使用Redis集群（高可用）
- 监控Redis内存使用

### 应用优化
- 异步处理心跳请求
- 批量更新设备状态
- 连接池优化

## 安全考虑

### 接口安全
- JWT token认证
- 接口访问权限控制
- 请求频率限制

### 数据安全
- 敏感信息加密存储
- 数据库访问权限控制
- 定期数据备份

---

**注意事项：**
- 设备ID必须全局唯一
- 心跳间隔建议不超过3分钟
- 定期清理离线设备的历史数据
- 监控Redis内存使用情况