# 现代化网站项目

一个响应式、现代化的企业网站模板，采用纯HTML、CSS和JavaScript构建。

## 🌟 特性

- **响应式设计** - 完美适配桌面、平板和移动设备
- **现代化UI** - 渐变背景、毛玻璃效果、流畅动画
- **交互丰富** - 平滑滚动、悬停效果、动态计数器
- **性能优化** - 轻量级代码、懒加载、防抖节流
- **无障碍访问** - 键盘导航、语义化HTML
- **SEO友好** - 结构化数据、语义化标签

## 📁 项目结构

```
new-website/
├── index.html          # 主页面文件
├── css/
│   └── style.css      # 样式文件
├── js/
│   └── main.js        # JavaScript功能文件
└── README.md          # 项目说明文档
```

## 🚀 快速开始

1. **直接打开**
   ```bash
   # 在浏览器中打开 index.html
   open index.html
   ```

2. **本地服务器**（推荐）
   ```bash
   # 使用Python启动本地服务器
   python -m http.server 8000
   
   # 或使用Node.js
   npx serve .
   
   # 然后访问 http://localhost:8000
   ```

## 📱 页面结构

### 导航栏
- 固定顶部导航
- 响应式移动端菜单
- 滚动时自动隐藏/显示
- 毛玻璃背景效果

### 主要部分
1. **首页 (Hero)** - 吸引人的首屏展示
2. **关于我们** - 公司介绍和统计数据
3. **服务** - 服务项目展示
4. **作品集** - 项目案例展示
5. **联系我们** - 联系信息和表单
6. **页脚** - 链接和社交媒体

## 🎨 设计特色

### 颜色方案
- 主色调：渐变紫蓝色 (#667eea → #764ba2)
- 辅助色：白色、灰色系
- 强调色：用于悬停和交互状态

### 字体
- 主字体：Inter (Google Fonts)
- 图标：Font Awesome 6.0

### 动画效果
- 页面加载动画
- 滚动触发动画
- 悬停交互效果
- 浮动卡片动画
- 数字计数动画

## 💻 技术栈

- **HTML5** - 语义化标签、无障碍访问
- **CSS3** - Flexbox、Grid、动画、渐变
- **JavaScript ES6+** - 模块化、异步处理
- **外部资源**：
  - Google Fonts (Inter)
  - Font Awesome 6.0

## 🔧 功能特性

### JavaScript功能
- 移动端导航菜单切换
- 平滑滚动到锚点
- 滚动动画观察器
- 数字计数动画
- 表单验证和提交
- 通知系统
- 返回顶部按钮
- 性能优化（防抖、节流）

### CSS特性
- 响应式布局（移动优先）
- CSS Grid 和 Flexbox
- 自定义滚动条
- 毛玻璃效果（backdrop-filter）
- CSS动画和过渡
- 渐变背景

## 📱 响应式断点

- **桌面**: > 768px
- **平板**: 481px - 768px
- **手机**: ≤ 480px

## 🛠️ 自定义指南

### 修改颜色
在 `css/style.css` 中搜索并替换：
```css
/* 主渐变色 */
background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);

/* 修改为你的颜色 */
background: linear-gradient(135deg, #your-color-1 0%, #your-color-2 100%);
```

### 修改内容
编辑 `index.html` 中的文本内容：
- 公司名称
- 服务描述
- 联系信息
- 统计数据

### 添加新部分
1. 在 `index.html` 中添加新的 `<section>`
2. 在 `css/style.css` 中添加对应样式
3. 在导航菜单中添加链接

## 🔍 SEO优化

- 语义化HTML标签
- Meta标签优化
- 图片Alt属性
- 结构化数据
- 快速加载速度

## 🌐 浏览器支持

- Chrome 60+
- Firefox 55+
- Safari 12+
- Edge 79+

## 📄 许可证

MIT License - 可自由使用和修改

## 🤝 贡献

欢迎提交Issue和Pull Request来改进这个项目！

## 📞 支持

如果你在使用过程中遇到问题，可以：
1. 查看浏览器控制台错误信息
2. 检查文件路径是否正确
3. 确保所有文件都在正确位置

---

**享受构建美丽网站的过程！** ✨