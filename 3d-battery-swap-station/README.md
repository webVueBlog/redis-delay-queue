# 3D换电柜网站

一个现代化的3D换电柜展示网站，采用Three.js技术实现交互式3D演示，展示智能换电解决方案。

python -m http.server 8000

## 🚀 功能特性

### 3D可视化
- **真实感渲染**：采用Three.js WebGL技术，呈现逼真的3D换电柜模型
- **动态光照**：多层次光照系统，营造专业的视觉效果
- **材质细节**：精细的材质贴图和反射效果
- **PBR材质**：物理渲染材质，增强视觉真实感

### 双重演示模式
- **🤖 机械臂演示**：展示自动化机械臂的电池更换流程
  - 机械臂旋转定位
  - 精确抓取电池
  - 自动运输安装
- **🏍️ 骑手换电演示**：模拟真实的骑手换电场景
  - 骑手走向换电柜
  - 取出旧电池
  - 机械臂准备新电池
  - 完成电池更换
  - 骑手返回电动车

### 交互控制
- **实时操作**：支持开始、暂停、重置等操作控制
- **多场景切换**：可在机械臂和骑手演示间自由切换
- **状态指示**：实时显示设备状态和电池电量

### 响应式设计
- **多设备适配**：完美支持桌面、平板、手机等不同设备
- **流畅体验**：优化的性能表现，确保流畅的3D渲染

### 📱 页面结构
1. **首页横幅**：3D换电柜预览和产品介绍
2. **产品特色**：四大核心优势展示
3. **3D演示区**：完整的换电流程3D动画
4. **联系我们**：企业信息和联系表单

## 🛠️ 技术栈

- **前端框架**：原生HTML5 + CSS3 + JavaScript
- **3D引擎**：Three.js r128
- **动画库**：CSS3 Transitions + JavaScript动画
- **响应式**：CSS Grid + Flexbox
- **字体图标**：Unicode Emoji

## 📦 项目结构

```
3d-battery-swap-station/
├── index.html              # 主页面
├── css/
│   └── style.css          # 样式文件
├── js/
│   └── main.js            # 主要JavaScript逻辑
└── README.md              # 项目说明
```

## 🎮 3D演示功能

### 交互控制
- **鼠标左键拖拽**：旋转3D场景视角
- **鼠标滚轮**：缩放场景
- **鼠标右键拖拽**：平移场景

### 动画演示
1. **机械臂旋转**：机械臂转向电池位置
2. **电池拾取**：抓取待更换的电池
3. **电池运输**：将电池运输到换电柜
4. **电池安装**：将电池安装到指定槽位

## 🎨 设计特色

### 视觉设计
- **渐变背景**：紫色到蓝色的科技感渐变
- **玻璃拟态**：半透明背景和模糊效果
- **霓虹色彩**：青蓝色主题色调
- **阴影效果**：立体感卡片设计

### 动画效果
- **滚动动画**：元素进入视窗时的淡入效果
- **悬停效果**：按钮和卡片的交互反馈
- **3D动画**：流畅的换电流程演示
- **导航动画**：平滑的页面滚动

## 🚀 快速开始

### 本地运行
1. 下载项目文件到本地
2. 使用现代浏览器打开 `index.html`
3. 确保网络连接正常（需要加载Three.js CDN）

### 在线部署
1. 将项目文件上传到Web服务器
2. 确保服务器支持静态文件访问
3. 访问域名即可查看网站

## 📱 浏览器兼容性

- ✅ Chrome 60+
- ✅ Firefox 55+
- ✅ Safari 12+
- ✅ Edge 79+
- ❌ Internet Explorer（不支持）

## 🔧 自定义配置

### 修改3D模型
在 `js/main.js` 中找到以下函数进行修改：
- `createBatterySwapStation()`：换电柜模型
- `createBattery()`：电池模型
- `createRobotArm()`：机械臂模型

### 调整动画参数
```javascript
// 动画持续时间（毫秒）
const duration = 2000;

// 缓动函数
function easeInOut(t) {
    return t < 0.5 ? 2 * t * t : -1 + (4 - 2 * t) * t;
}
```

### 修改颜色主题
在 `css/style.css` 中修改CSS变量：
```css
:root {
    --primary-color: #00d4ff;
    --secondary-color: #0099cc;
    --background-gradient: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
```

## 📊 性能优化

### 已实现的优化
- **Three.js CDN**：使用CDN加速资源加载
- **抗锯齿渲染**：提升3D场景视觉质量
- **阴影优化**：合理的阴影贴图尺寸
- **响应式渲染**：根据容器大小调整渲染尺寸

### 建议的优化
- 使用本地Three.js文件减少网络依赖
- 添加模型LOD（细节层次）系统
- 实现纹理压缩和优化
- 添加加载进度指示器

## 🎯 应用场景

- **产品展示**：换电设备制造商官网
- **技术演示**：向客户展示换电技术
- **教育培训**：换电流程教学演示
- **营销推广**：吸引投资者和合作伙伴

## 🔮 未来扩展

### 计划功能
- [ ] VR/AR支持
- [ ] 更详细的3D模型
- [ ] 实时数据集成
- [ ] 多语言支持
- [ ] 移动端优化
- [ ] 音效和背景音乐

### 技术升级
- [ ] 使用React/Vue重构
- [ ] 添加TypeScript支持
- [ ] 集成WebGL 2.0特性
- [ ] 实现物理引擎

## 📄 许可证

MIT License - 详见 LICENSE 文件

## 🤝 贡献

欢迎提交Issue和Pull Request来改进这个项目！

## 📞 联系方式

- 📧 邮箱：
- 📱 电话：
- 🌐 网站：

---

**注意**：这是一个演示项目，用于展示3D换电柜技术。实际的换电设备可能与演示模型有所不同。