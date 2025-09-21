package com.missoft.pms;

import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * 简单的URL解析和连接测试
 */
public class SimpleUrlTest {

    @Test
    public void testDirectConnection() {
        String url = "jdbc:mysql://localhost:3306/pms_db?useUnicode=true&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai";
        String baseUrl = getCorrectBaseUrl(url);
        String username = "root";
        String password = "wenliu125&*";
        
        System.out.println("原始URL: " + url);
        System.out.println("基础URL: " + baseUrl);
        
        // 测试基础连接
        try (Connection connection = DriverManager.getConnection(baseUrl, username, password)) {
            System.out.println("✅ 基础连接成功");
            
            // 创建数据库
            var statement = connection.createStatement();
            statement.execute("CREATE DATABASE IF NOT EXISTS pms_db");
            System.out.println("✅ 数据库创建成功");
            
        } catch (Exception e) {
            System.err.println("❌ 连接失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        // 测试完整连接
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            System.out.println("✅ 完整连接成功");
            
        } catch (Exception e) {
            System.err.println("❌ 完整连接失败: " + e.getMessage());
        }
    }
    
    private String getCorrectBaseUrl(String url) {
        // jdbc:mysql://localhost:3306/pms_db?params -> jdbc:mysql://localhost:3306?params
        int lastSlashIndex = url.lastIndexOf('/');
        if (lastSlashIndex > 0) {
            String beforeDatabase = url.substring(0, lastSlashIndex);
            int questionMarkIndex = url.indexOf('?');
            if (questionMarkIndex > 0) {
                return beforeDatabase + url.substring(questionMarkIndex);
            }
            return beforeDatabase;
        }
        return url;
    }
}