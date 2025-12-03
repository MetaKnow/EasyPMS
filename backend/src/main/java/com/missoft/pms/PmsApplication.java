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
            // 查找配置文件的逻辑：
            // 1. 检查当前目录（如果是从根目录运行）
            // 2. 检查上级目录（如果是从backend目录运行）
            File configFile = new File("pms-config.json");
            if (!configFile.exists()) {
                configFile = new File("../pms-config.json");
            }
            
            if (configFile.exists()) {
                System.out.println("📄 发现配置文件: " + configFile.getAbsolutePath());
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(configFile);
                if (rootNode.has("backend") && rootNode.get("backend").has("port")) {
                    int port = rootNode.get("backend").get("port").asInt();
                    System.setProperty("server.port", String.valueOf(port));
                    System.out.println("🔌 使用配置端口: " + port);
                }
            } else {
                 System.out.println("⚠️ 未找到 pms-config.json，使用默认配置");
            }
        } catch (Exception e) {
            System.err.println("⚠️ 读取配置文件失败: " + e.getMessage());
        }

        System.out.println("🔧 数据库将通过DatabasePreInitializer自动创建");
        
        ConfigurableApplicationContext context = SpringApplication.run(PmsApplication.class, args);
        
        System.out.println("🎉 应用启动完成!");
        String port = context.getEnvironment().getProperty("server.port", "8081");
        System.out.println("🌐 访问地址: http://localhost:" + port);
    }

}
