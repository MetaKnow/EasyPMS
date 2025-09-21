package com.missoft.pms;

import com.missoft.pms.config.DatabaseMigrationManager;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * 数据库迁移测试类
 * 用于测试数据库迁移功能
 * 
 * @author MissoftPMS
 * @version 1.0.0
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application.properties")
public class MigrationTest {

    @Test
    public void testDatabaseMigration() {
        try {
            // 数据库连接信息
            String datasourceUrl = "jdbc:mysql://localhost:3306/pms_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai";
            String username = "root";
            String password = "wenliu125&*";
            
            // 创建迁移管理器并执行迁移
            DatabaseMigrationManager migrationManager = new DatabaseMigrationManager(datasourceUrl, username, password);
            migrationManager.migrate();
            
            System.out.println("✅ 数据库迁移测试完成");
            
        } catch (Exception e) {
            System.err.println("❌ 数据库迁移测试失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("数据库迁移测试失败", e);
        }
    }
}