package com.missoft.pms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

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
        System.out.println("🔧 数据库将通过DatabasePreInitializer自动创建");
        
        ConfigurableApplicationContext context = SpringApplication.run(PmsApplication.class, args);
        
        System.out.println("🎉 应用启动完成!");
        System.out.println("🌐 访问地址: http://localhost:8080");
    }

}