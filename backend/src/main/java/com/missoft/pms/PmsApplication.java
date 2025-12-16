package com.missoft.pms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.nio.file.Paths;

/**
 * 软件项目管理系统主启动类
 * 
 * @author MissoftPMS
 * @version 1.0.0
 */
@SpringBootApplication
public class PmsApplication {

    public static void main(String[] args) {
        System.out.println("🚀 启动软件项目管理系统...");
        System.out.println("📦 扫描包路径: com.missoft.pms");
        try {
            String cwd = System.getProperty("user.dir");
            var base = Paths.get(cwd);
            var logDirPath = base.resolve("backend").resolve("logs");
            if (base.getFileName() != null && base.getFileName().toString().equalsIgnoreCase("backend")) {
                logDirPath = base.resolve("logs");
            }
            System.setProperty("pms.log.dir", logDirPath.toString());
        } catch (Exception ignored) {
        }
        
        // 尝试加载 pms-config.json
        try {
            // 支持显式路径（优先级最高）
            String explicitPath = System.getenv("PMS_CONFIG_PATH");
            if (explicitPath == null || explicitPath.isBlank()) {
                explicitPath = System.getProperty("pms.config.path");
            }
            File configFile = null;
            if (explicitPath != null && !explicitPath.isBlank()) {
                configFile = new File(explicitPath);
            } else {
                // 逐级回溯查找（当前、上一级、上两级）
                File[] candidates = new File[] {
                    new File("pms-config.json"),
                    new File("../pms-config.json"),
                    new File("../../pms-config.json")
                };
                for (File c : candidates) {
                    if (c.exists()) { configFile = c; break; }
                }
                // 如果仍未找到，尝试根据当前路径推断项目根
                if (configFile == null) {
                    String cwd = System.getProperty("user.dir");
                    var base = Paths.get(cwd).toAbsolutePath();
                    var maybeRoot = base.getParent() != null ? base.getParent().getParent() : null;
                    if (maybeRoot != null) {
                        File inferred = maybeRoot.resolve("pms-config.json").toFile();
                        if (inferred.exists()) configFile = inferred;
                    }
                }
            }
            
            if (configFile != null && configFile.exists()) {
                System.out.println("📄 发现配置文件: " + configFile.getAbsolutePath());
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(configFile);
                if (rootNode.has("backend")) {
                    JsonNode be = rootNode.get("backend");
                    // 内网监听端口优先：backend.internalPort -> 环境变量 PMS_SERVER_PORT -> backend.port -> 8081
                    int bindPort = be.path("internalPort").asInt(-1);
                    String envPort = System.getenv("PMS_SERVER_PORT");
                    if (bindPort <= 0 && envPort != null && !envPort.isBlank()) {
                        try { bindPort = Integer.parseInt(envPort.trim()); } catch (Exception ignored) {}
                    }
                    if (bindPort <= 0) bindPort = be.path("port").asInt(8081);
                    System.setProperty("server.port", String.valueOf(bindPort));
                    System.out.println("🔌 服务监听端口: " + bindPort);

                    // 内网绑定地址优先：env PMS_BIND_ADDRESS -> backend.internalHost -> 0.0.0.0
                    String bindAddr = System.getenv("PMS_BIND_ADDRESS");
                    if (bindAddr == null || bindAddr.isBlank()) bindAddr = be.path("internalHost").asText("");
                    if (bindAddr == null || bindAddr.isBlank()) bindAddr = "0.0.0.0";
                    System.setProperty("server.address", bindAddr);
                    System.out.println("🖧 服务绑定地址: " + bindAddr);
                }
                // 读取数据库配置并覆盖 Spring 数据源属性（函数级注释：支持url或host/port/name组合）
                if (rootNode.has("database")) {
                    JsonNode db = rootNode.get("database");
                    String url = null;
                    if (db.has("url") && db.get("url").isTextual()) {
                        url = db.get("url").asText();
                    } else {
                        String host = db.path("host").asText("localhost");
                        int dbPort = db.path("port").asInt(3306);
                        String name = db.path("name").asText("pms_db");
                        String params = "useUnicode=true&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai";
                        url = String.format("jdbc:mysql://%s:%d/%s?%s", host, dbPort, name, params);
                        System.out.println("🔧 使用数据库配置: " + host + ":" + dbPort + "/" + name);
                    }
                    String username = db.path("username").asText(null);
                    String password = db.path("password").asText(null);
                    if (url != null) System.setProperty("spring.datasource.url", url);
                    if (username != null) System.setProperty("spring.datasource.username", username);
                    if (password != null) System.setProperty("spring.datasource.password", password);
                }
            } else {
                 System.out.println("⚠️ 未找到 pms-config.json，使用默认配置");
                 System.out.println("   查找路径支持: 当前/父级/祖父级目录，或通过 PMS_CONFIG_PATH / -Dpms.config.path 指定绝对路径");
            }
        } catch (Exception e) {
            System.err.println("⚠️ 读取配置文件失败: " + e.getMessage());
        }
        
        System.out.println("🔧 数据库将通过DatabasePreInitializer自动创建");
        
        ConfigurableApplicationContext context = SpringApplication.run(PmsApplication.class, args);
        
        System.out.println("🎉 应用启动完成!");
        String port = context.getEnvironment().getProperty("server.port", "8081");
        String addr = context.getEnvironment().getProperty("server.address", "localhost");
        System.out.println("🌐 访问地址: http://" + addr + ":" + port);
    }

}
