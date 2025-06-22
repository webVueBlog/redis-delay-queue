# 茶叶网站图片资源清单

## 主要图片资源

### 主页横幅图片
- **茶园风景**: https://images.unsplash.com/photo-1558618666-fcd25c85cd64?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80
- **茶叶特写**: https://images.unsplash.com/photo-1544787219-7f47ccb76574?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&q=80

### 产品展示大图
- **精选茶叶展示**: https://images.unsplash.com/photo-1563822249548-9a72b6353cd1?ixlib=rb-4.0.3&auto=format&fit=crop&w=600&q=80

### 产品卡片图片
1. **西湖龙井**: https://images.unsplash.com/photo-1571934811356-5cc061b6821f?ixlib=rb-4.0.3&auto=format&fit=crop&w=300&q=80
2. **安溪铁观音**: https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?ixlib=rb-4.0.3&auto=format&fit=crop&w=300&q=80
3. **云南普洱**: https://images.unsplash.com/photo-1576092768241-dec231879fc3?ixlib=rb-4.0.3&auto=format&fit=crop&w=300&q=80
4. **福鼎白茶**: https://images.unsplash.com/photo-1594736797933-d0401ba2fe65?ixlib=rb-4.0.3&auto=format&fit=crop&w=300&q=80
5. **正山小种**: https://images.unsplash.com/photo-1559056199-641a0ac8b55e?ixlib=rb-4.0.3&auto=format&fit=crop&w=300&q=80
6. **茉莉花茶**: https://images.unsplash.com/photo-1587080266227-677cc2a4e76e?ixlib=rb-4.0.3&auto=format&fit=crop&w=300&q=80

### 关于我们图片
- **茶艺师**: https://images.unsplash.com/photo-1563822249548-9a72b6353cd1?ixlib=rb-4.0.3&auto=format&fit=crop&w=600&q=80

## 备用图片资源

### 本地占位符
- **通用占位符**: `./images/placeholder.svg`
  - SVG格式，轻量级
  - 茶叶主题设计
  - 渐变背景和茶叶元素

## 图片优化特性

### 懒加载支持
- 所有图片都配置了 `data-src` 属性
- 使用 IntersectionObserver API 实现懒加载
- 提升页面加载性能

### 错误处理
- 图片加载失败时自动使用占位符
- 控制台错误日志记录
- 用户友好的错误提示

### 响应式设计
- 不同屏幕尺寸的图片适配
- 移动端优化的图片尺寸
- 自适应网格布局

## 图片加载状态

### CSS 类名管理
- `.loaded`: 图片加载完成
- `.error`: 图片加载失败
- 淡入动画效果

### 性能优化
- 关键图片预加载
- 图片压缩和格式优化
- CDN 加速（Unsplash）

## 维护说明

1. **定期检查外部链接**: 确保 Unsplash 图片链接有效
2. **更新占位符**: 根据需要更新本地占位符设计
3. **性能监控**: 监控图片加载时间和成功率
4. **备用方案**: 考虑添加更多本地图片资源

## 图片版权说明

- 所有 Unsplash 图片均为免费商用授权
- 本地 SVG 占位符为原创设计
- 遵循相关版权和使用条款