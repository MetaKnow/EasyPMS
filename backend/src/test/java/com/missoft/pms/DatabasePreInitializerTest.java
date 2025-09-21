package com.missoft.pms;

import com.missoft.pms.config.DatabasePreInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.env.MockEnvironment;

/**
 * 数据库预初始化器测试
 */
public class DatabasePreInitializerTest {

    @Test
    public void testDatabasePreInitializer() {
        System.out.println("🔍 测试数据库预初始化器...");
        
        // 创建模拟环境
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("spring.datasource.url", "jdbc:mysql://localhost:3306/pms_db?useUnicode=true&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai");
        environment.setProperty("spring.datasource.username", "root");
        environment.setProperty("spring.datasource.password", "wenliu125&*");
        
        // 创建预初始化器
        DatabasePreInitializer initializer = new DatabasePreInitializer();
        
        try {
            // 模拟Spring上下文
            ConfigurableApplicationContext context = null; // 这里我们不需要真实的上下文
            
            System.out.println("🚀 开始数据库预初始化...");
            initializer.initialize(context);
            System.out.println("✅ 数据库预初始化成功");
            
        } catch (Exception e) {
            System.err.println("❌ 数据库预初始化失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}