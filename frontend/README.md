# Vue 3 + Element Plus 表格展示应用

这是一个独立的前端项目，基于 Vue 3 + Element Plus 的数据库表字段查看器，能够友好地展示 `/api/tables/columns` 接口返回的数据。

## 功能特性

- 📊 **表格展示**: 清晰展示数据库表结构信息
- 📁 **按表分组**: 自动按表名分组显示
- 🔽 **可折叠设计**: 每个表可独立折叠/展开查看字段详情
- 🏷️ **字段标识**: 清晰标识主键(PK)、唯一键(UK)等
- 🎨 **现代UI**: 基于 Element Plus 的美观界面
- 📱 **响应式设计**: 适配不同屏幕尺寸

## 技术栈

- **前端框架**: Vue 3 (Composition API)
- **UI组件库**: Element Plus
- **构建工具**: Vite
- **HTTP客户端**: Axios

## 项目结构

```
frontend/
├── index.html              # 入口HTML文件
├── package.json            # 前端依赖配置
├── vite.config.js          # Vite配置文件
├── src/
│   ├── main.js            # Vue应用入口
│   └── App.vue            # 主组件
└── README.md              # 项目说明
```

## 快速开始

### 1. 安装依赖

```bash
npm install
```

### 2. 启动开发服务器

```bash
npm run dev
```

前端应用将在 `http://localhost:3000` 启动

### 3. 访问应用

打开浏览器访问 `http://localhost:3000` 即可使用应用。

**注意**: 确保后端API服务已在 `http://localhost:8080` 启动，否则数据无法正常加载。

## API接口说明

### GET /api/tables/columns

获取数据库表字段信息

**响应数据格式**:
```json
[
  {
    "tableName": "users",
    "tableComment": "用户表",
    "ordinalPosition": 1,
    "columnName": "id",
    "dataType": "bigint",
    "isNullable": "NO",
    "columnDefault": null,
    "columnComment": "主键ID",
    "extra": "auto_increment",
    "isPrimaryKey": true,
    "isUnique": false,
    "numericPrecision": 20,
    "numericScale": null,
    "characterMaximumLength": null
  }
]
```

## 字段说明

| 字段名 | 类型 | 说明 |
|--------|------|------|
| tableName | String | 表名 |
| tableComment | String | 表注释 |
| ordinalPosition | Integer | 字段在表中的位置 |
| columnName | String | 字段名 |
| dataType | String | 数据类型 |
| isNullable | String | 是否允许NULL (YES/NO) |
| columnDefault | String | 默认值 |
| columnComment | String | 字段注释 |
| extra | String | 额外信息 (如auto_increment) |
| isPrimaryKey | Boolean | 是否为主键 |
| isUnique | Boolean | 是否为唯一键 |
| numericPrecision | Integer | 数值精度 |
| numericScale | Integer | 数值小数位数 |
| characterMaximumLength | Integer | 字符最大长度 |

## 功能截图

应用界面包含以下功能：

1. **表格列表**: 按表名分组显示所有表
2. **折叠面板**: 点击表名可展开/折叠查看字段详情
3. **字段详情**: 显示字段的完整信息，包括类型、约束、注释等
4. **标识标签**: 用不同颜色标识主键、唯一键等特殊字段
5. **刷新功能**: 点击刷新按钮重新加载数据

## 自定义配置

### 修改API地址

如需修改后端API地址，请编辑 `vite.config.js` 文件中的代理配置：

```javascript
server: {
  proxy: {
    '/api': {
      target: 'http://your-backend-url:port',
      changeOrigin: true
    }
  }
}
```

### 添加新的表格列

如需添加新的表格列，请修改 `src/App.vue` 文件中的 `el-table-column` 组件。

## 构建生产版本

```bash
npm run build
```

构建完成后，生成的文件将在 `dist` 目录中。

## 浏览器支持

- Chrome >= 87
- Firefox >= 78
- Safari >= 14
- Edge >= 88

## 与后端项目的关系

这个前端项目是独立的，可以与任何提供 `/api/tables/columns` 接口的后端服务配合使用。

如果你需要完整的后端服务，请参考父目录中的 Spring Boot 项目。

## 许可证

MIT License