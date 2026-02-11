1. 请把我每次提的需求都记录到项目根目录的docs/dialogs/dialogs.md中，包括对话内容及对话时间。只记录我提的需求，不需要记录我反馈的报错信息、调试信息。
2. 项目为前后端分离：前端 Vue 3 + Vite，后端 Spring Boot 3.2.x（Java 17），数据库 MySQL 8。
3. 主要目录：frontend/ 为前端，backend/ 为后端，database/ 为数据库脚本，docs/ 为项目文档，start-full-stack.bat 与 start-dev.bat 为启动脚本。
4. 开发启动：
   - 一键启动优先使用 start-full-stack.bat。
   - 快速开发可用 start-dev.bat。
   - 手动启动：前端在 frontend/ 下执行 npm run dev；后端在 backend/ 下执行 mvn spring-boot:run。
5. 构建与打包：
   - 后端打包：backend/ 下执行 mvn -DskipTests package，产物为 backend/target/pms-1.0.0.jar。
   - 前端构建：frontend/ 下执行 npm run build，产物在 frontend/dist。
6. 运行与 JDK：
   - 项目内置 JDK 位于 tools/jdk/，启动脚本默认使用内置 JDK。
   - 后端可通过 tools/jdk/bin/java.exe -jar backend/target/pms-1.0.0.jar 运行。
7. 端口与配置：
   - 默认前端端口 5173，后端端口 8081。
   - start-full-stack.bat 会读取项目根目录 pms-config.json 的前后端端口与主机；无配置则使用默认值。
   - 后端配置集中在 backend/src/main/resources/application.properties。
8. API 约定：
   - 后端 REST 接口统一以 /api 为前缀。
   - 前端接口集中在 frontend/src/api/ 下，通过 request.js 统一封装请求。
9. 数据库迁移：
   - 脚本位于 database/scripts/，命名规则为三位数字前缀，例如 001_xxx.sql。
   - 已执行的迁移脚本禁止修改，新增脚本按序号递增。
10. 日志与产物：
   - 后端日志默认输出到 backend/logs/ 与控制台。
   - 后端构建产物在 backend/target/，前端构建产物在 frontend/dist/。
11. 安全要求：
   - 不在代码与文档中输出或提交密钥、密码等敏感信息。
   - 避免在修改中新增明文凭据与调试性泄露。
