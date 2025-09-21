package com.missoft.pms;

import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * MySQL服务连接测试
 */
public class MySQLServiceTest {

    @Test
    public void testMySQLService() {
        String baseUrl = "jdbc:mysql://localhost:3306/?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true";
        String username = "root";
        String password = "wenliu125&*";
        
        System.out.println("测试MySQL服务连接...");
        System.out.println("URL: " + baseUrl);
        System.out.println("用户名: " + username);
        
        try (Connection connection = DriverManager.getConnection(baseUrl, username, password)) {
            System.out.println("✅ MySQL服务连接成功！");
            System.out.println("数据库产品名: " + connection.getMetaData().getDatabaseProductName());
            System.out.println("数据库版本: " + connection.getMetaData().getDatabaseProductVersion());
        } catch (SQLException e) {
            System.out.println("❌ MySQL服务连接失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Test
    public void testCreateDatabase() {
        String baseUrl = "jdbc:mysql://localhost:3306/?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true";
        String username = "root";
        String password = "wenliu125&*";
        String databaseName = "pms_db";
        
        System.out.println("测试数据库创建...");
        
        try (Connection connection = DriverManager.getConnection(baseUrl, username, password)) {
            // 检查数据库是否存在
            String checkSql = "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = '" + databaseName + "'";
            var stmt = connection.createStatement();
            var rs = stmt.executeQuery(checkSql);
            
            if (rs.next()) {
                System.out.println("✅ 数据库 " + databaseName + " 已存在");
            } else {
                System.out.println("📝 数据库 " + databaseName + " 不存在，尝试创建...");
                String createSql = "CREATE DATABASE IF NOT EXISTS " + databaseName + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci";
                stmt.executeUpdate(createSql);
                System.out.println("✅ 数据库 " + databaseName + " 创建成功！");
            }
        } catch (SQLException e) {
            System.out.println("❌ 数据库操作失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}