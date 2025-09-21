package com.missoft.pms;

import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * 数据库连接调试测试
 */
public class DatabaseConnectionDebugTest {

    @Test
    public void testDatabaseConnection() {
        String fullUrl = "jdbc:mysql://localhost:3306/pms_db?useUnicode=true&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai";
        String baseUrl = "jdbc:mysql://localhost:3306?useUnicode=true&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai";
        String username = "root";
        String password = "wenliu125&*";
        
        System.out.println("🔍 测试数据库连接...");
        System.out.println("完整URL: " + fullUrl);
        System.out.println("基础URL: " + baseUrl);
        
        // 测试基础连接（不指定数据库）
        System.out.println("\n🔗 测试基础MySQL连接...");
        try (Connection connection = DriverManager.getConnection(baseUrl, username, password)) {
            System.out.println("✅ 基础MySQL连接成功");
            
            // 测试查询
            var statement = connection.createStatement();
            var resultSet = statement.executeQuery("SELECT VERSION() as version");
            if (resultSet.next()) {
                System.out.println("✅ MySQL版本: " + resultSet.getString("version"));
            }
            
        } catch (Exception e) {
            System.err.println("❌ 基础MySQL连接失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        // 测试完整连接（指定数据库）
        System.out.println("\n🔗 测试完整数据库连接...");
        try (Connection connection = DriverManager.getConnection(fullUrl, username, password)) {
            System.out.println("✅ 完整数据库连接成功");
            
        } catch (Exception e) {
            System.err.println("❌ 完整数据库连接失败: " + e.getMessage());
            System.err.println("💡 这是正常的，因为pms_db数据库可能不存在");
        }
    }
    
    @Test
    public void testUrlParsing() {
        String url = "jdbc:mysql://localhost:3306/pms_db?useUnicode=true&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai";
        
        System.out.println("🔍 测试URL解析...");
        System.out.println("原始URL: " + url);
        
        // 提取数据库名
        String databaseName = extractDatabaseName(url);
        System.out.println("数据库名: " + databaseName);
        
        // 构建基础URL
        String baseUrl = getBaseUrl(url);
        System.out.println("基础URL: " + baseUrl);
    }
    
    private String extractDatabaseName(String url) {
        String[] parts = url.split("/");
        if (parts.length >= 4) {
            String dbPart = parts[3];
            int questionMarkIndex = dbPart.indexOf('?');
            if (questionMarkIndex > 0) {
                return dbPart.substring(0, questionMarkIndex);
            }
            return dbPart;
        }
        return "pms_db";
    }
    
    private String getBaseUrl(String url) {
        // 找到最后一个斜杠的位置（数据库名之前）
        int lastSlashIndex = url.lastIndexOf('/');
        if (lastSlashIndex > 0) {
            String baseUrl = url.substring(0, lastSlashIndex);
            
            // 检查原URL是否包含参数（参数应该在数据库名之后）
            int questionMarkIndex = url.indexOf('?', lastSlashIndex);
            if (questionMarkIndex > 0) {
                // 将参数部分添加到基础URL
                baseUrl += url.substring(questionMarkIndex);
            }
            return baseUrl;
        }
        return url;
    }
}