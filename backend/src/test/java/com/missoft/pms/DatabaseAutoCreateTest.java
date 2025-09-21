package com.missoft.pms;

import com.missoft.pms.config.DatabasePreInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 数据库自动创建测试类
 * 用于测试数据库自动创建和迁移功能
 * 
 * @author MissoftPMS
 * @version 1.0.0
 */
public class DatabaseAutoCreateTest {

    @Test
    public void testDatabaseAutoCreation() {
        System.out.println("🧪 开始测试数据库自动创建功能...");
        
        try {
            // 1. 首先删除数据库（如果存在）
            dropDatabaseIfExists();
            
            // 2. 创建模拟的Spring应用上下文
            ConfigurableApplicationContext context = new AnnotationConfigApplicationContext();
            ConfigurableEnvironment env = context.getEnvironment();
            
            // 3. 设置数据库配置
            Map<String, Object> properties = new HashMap<>();
            properties.put("spring.datasource.url", "jdbc:mysql://localhost:3306/pms_db?useUnicode=true&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai");
            properties.put("spring.datasource.username", "root");
            properties.put("spring.datasource.password", "wenliu125&*");
            
            env.getPropertySources().addFirst(new MapPropertySource("test", properties));
            
            // 4. 执行数据库预初始化
            DatabasePreInitializer initializer = new DatabasePreInitializer();
            initializer.initialize(context);
            
            // 5. 验证数据库是否创建成功
            verifyDatabaseExists();
            
            // 6. 验证迁移表是否创建成功
            verifyMigrationTableExists();
            
            System.out.println("✅ 数据库自动创建测试通过！");
            
        } catch (Exception e) {
            System.err.println("❌ 数据库自动创建测试失败: " + e.getMessage());
            e.printStackTrace();
            fail("数据库自动创建测试失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除数据库（如果存在）
     */
    private void dropDatabaseIfExists() throws Exception {
        String serverUrl = "jdbc:mysql://localhost:3306?useUnicode=true&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai";
        String username = "root";
        String password = "wenliu125&*";
        
        try (Connection connection = DriverManager.getConnection(serverUrl, username, password);
             Statement statement = connection.createStatement()) {
            
            statement.executeUpdate("DROP DATABASE IF EXISTS `pms_db`");
            System.out.println("🗑️ 已删除现有数据库（如果存在）");
        }
    }
    
    /**
     * 验证数据库是否存在
     */
    private void verifyDatabaseExists() throws Exception {
        String datasourceUrl = "jdbc:mysql://localhost:3306/pms_db?useUnicode=true&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai";
        String username = "root";
        String password = "wenliu125&*";
        
        try (Connection connection = DriverManager.getConnection(datasourceUrl, username, password)) {
            assertNotNull(connection, "数据库连接应该成功");
            System.out.println("✅ 数据库 pms_db 创建成功");
        }
    }
    
    /**
     * 验证迁移表是否存在
     */
    private void verifyMigrationTableExists() throws Exception {
        String datasourceUrl = "jdbc:mysql://localhost:3306/pms_db?useUnicode=true&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai";
        String username = "root";
        String password = "wenliu125&*";
        
        try (Connection connection = DriverManager.getConnection(datasourceUrl, username, password);
             Statement statement = connection.createStatement()) {
            
            String checkTableSQL = "SELECT COUNT(*) FROM information_schema.tables " +
                                  "WHERE table_schema = 'pms_db' AND table_name = 'migration_history'";
            
            ResultSet resultSet = statement.executeQuery(checkTableSQL);
            resultSet.next();
            
            int tableCount = resultSet.getInt(1);
            assertTrue(tableCount > 0, "迁移历史表应该存在");
            System.out.println("✅ 迁移历史表创建成功");
        }
    }
}