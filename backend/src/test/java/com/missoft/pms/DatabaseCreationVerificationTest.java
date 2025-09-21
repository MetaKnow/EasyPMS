package com.missoft.pms;

import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 验证数据库创建和迁移功能
 */
public class DatabaseCreationVerificationTest {

    private static final String URL = "jdbc:mysql://localhost:3306/pms_db?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "wenliu125&*";

    @Test
    public void testDatabaseExists() {
        System.out.println("🔍 验证数据库是否存在...");
        
        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
            System.out.println("✅ 数据库 pms_db 连接成功！");
            
            // 获取数据库信息
            String catalog = connection.getCatalog();
            System.out.println("📊 当前数据库: " + catalog);
            
        } catch (SQLException e) {
            System.out.println("❌ 数据库连接失败: " + e.getMessage());
            throw new RuntimeException("数据库验证失败", e);
        }
    }
    
    @Test
    public void testMigrationTableExists() {
        System.out.println("🔍 验证迁移历史表是否存在...");
        
        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
            // 检查migration_history表是否存在
            String checkTableSql = "SHOW TABLES LIKE 'migration_history'";
            var stmt = connection.createStatement();
            var rs = stmt.executeQuery(checkTableSql);
            
            if (rs.next()) {
                System.out.println("✅ migration_history 表已存在");
                
                // 查看表结构
                String describeTableSql = "DESCRIBE migration_history";
                var describeRs = stmt.executeQuery(describeTableSql);
                System.out.println("📋 表结构:");
                while (describeRs.next()) {
                    String field = describeRs.getString("Field");
                    String type = describeRs.getString("Type");
                    String nullable = describeRs.getString("Null");
                    String key = describeRs.getString("Key");
                    String extra = describeRs.getString("Extra");
                    System.out.println("   - " + field + " (" + type + ") " + 
                                     (nullable.equals("NO") ? "NOT NULL" : "NULL") +
                                     (key != null && !key.isEmpty() ? " " + key : "") +
                                     (extra != null && !extra.isEmpty() ? " " + extra : ""));
                }
                
                // 查看迁移记录
                String selectSql = "SELECT * FROM migration_history ORDER BY executed_at";
                var selectRs = stmt.executeQuery(selectSql);
                System.out.println("📝 迁移记录:");
                while (selectRs.next()) {
                    String filename = selectRs.getString("filename");
                    String executedAt = selectRs.getString("executed_at");
                    System.out.println("   - " + filename + " (执行时间: " + executedAt + ")");
                }
                
            } else {
                System.out.println("❌ migration_history 表不存在");
                throw new RuntimeException("迁移历史表未创建");
            }
            
        } catch (SQLException e) {
            System.out.println("❌ 迁移表验证失败: " + e.getMessage());
            throw new RuntimeException("迁移表验证失败", e);
        }
    }
    
    @Test
    public void testAllTables() {
        System.out.println("🔍 查看数据库中的所有表...");
        
        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
            String showTablesSql = "SHOW TABLES";
            var stmt = connection.createStatement();
            var rs = stmt.executeQuery(showTablesSql);
            
            System.out.println("📊 数据库表列表:");
            int tableCount = 0;
            while (rs.next()) {
                String tableName = rs.getString(1);
                System.out.println("   - " + tableName);
                tableCount++;
            }
            System.out.println("📈 总计: " + tableCount + " 个表");
            
        } catch (SQLException e) {
            System.out.println("❌ 查看表列表失败: " + e.getMessage());
            throw new RuntimeException("查看表列表失败", e);
        }
    }
}