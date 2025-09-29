# MissoftPMS - 软件项目管理系统

一个基于Vue3 + Spring Boot的现代化项目管理系统

## 🚀 快速启动

### 方式一：完整启动（推荐）
```bash
# 双击运行或在命令行执行
start-full-stack.bat
```

**特点：**
- ✅ 完整的环境检查
- ✅ 自动安装前端依赖
- ✅ 自动创建数据库
- ✅ 统一的服务管理
- ✅ 一键停止所有服务

### 方式二：快速启动
```bash
# 适合开发环境，快速启动
start-dev.bat
```

**特点：**
- ⚡ 快速启动，无环境检查
- 🔧 适合开发调试
- 📝 需要手动管理服务

## 📋 环境要求

### 必需环境
- **Java 17+** - 后端运行环境
- **Maven 3.6+** - 后端构建工具
- **Node.js 16+** - 前端运行环境
- **MySQL 8.0+** - 数据库

### 数据库配置
- 主机: `localhost:3306`
- 用户名: `root`
- 密码: `wenliu125&*`
- 数据库: `pms_db` (自动创建)

## 🌐 访问地址

- **前端界面**: http://localhost:5173
- **后端API**: http://localhost:8081
- **API文档**: http://localhost:8081/swagger-ui.html (待开发)

## 📁 项目结构

```
MissoftPMS/
├── frontend/          # Vue3前端项目
│   ├── src/
│   │   ├── components/
│   │   ├── views/
│   │   └── ...
│   └── package.json
├── backend/           # Spring Boot后端项目
│   ├── src/
│   │   ├── main/
│   │   └── test/
│   └── pom.xml
├── database/          # 数据库脚本
│   ├── init/
│   ├── migrations/
│   └── scripts/
├── docs/              # 项目文档
├── start-full-stack.bat  # 完整启动脚本
├── start-dev.bat         # 快速启动脚本
└── README.md
```

## 🛠️ 开发指南

### 前端开发
```bash
cd frontend
npm install
npm run dev
```

### 后端开发
```bash
cd backend
mvn spring-boot:run
```

### 数据库管理
- 自动创建: 启动时自动检查并创建数据库
- 手动创建: 执行 `database/scripts/quick_setup.sql`
- 迁移脚本: `database/migrations/` 目录

## 🔧 故障排除

### 常见问题

1. **端口占用**
   - 后端端口8080被占用: 修改 `backend/src/main/resources/application.properties`
   - 前端端口5173被占用: 修改 `frontend/vite.config.js`

2. **数据库连接失败**
   - 检查MySQL服务是否启动
   - 验证用户名密码是否正确
   - 确认端口3306是否开放

3. **前端依赖安装失败**
   ```bash
   cd frontend
   npm cache clean --force
   npm install
   ```

4. **后端启动失败**
   - 检查Java版本是否为17+
   - 确认Maven配置正确
   - 查看控制台错误信息

### 日志查看
- **后端日志**: 控制台输出 + `backend/logs/`
- **前端日志**: 浏览器控制台
- **数据库日志**: MySQL错误日志

## 📝 开发规范

### 代码提交
- 使用语义化提交信息
- 提交前运行测试
- 保持代码格式一致

### 分支管理
- `main`: 主分支，稳定版本
- `develop`: 开发分支
- `feature/*`: 功能分支
- `hotfix/*`: 热修复分支

## 📞 技术支持

如有问题，请联系开发团队或提交Issue。

---

**MissoftPMS Team** © 2024