package com.missoft.pms;

import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * 测试DatabasePreInitializer的URL解析逻辑
 */
public class DatabasePreInitializerUrlTest {

    @Test
    public void testUrlParsing() {
        String originalUrl = "jdbc:mysql://localhost:3306/pms_db?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true";
        
        // 模拟DatabasePreInitializer的逻辑
        String databaseName = extractDatabaseName(originalUrl);
        String baseUrl = getBaseUrl(originalUrl);
        
        System.out.println("原始URL: " + originalUrl);
        System.out.println("数据库名: " + databaseName);
        System.out.println("基础URL: " + baseUrl);
        
        // 测试基础连接
        testConnection(baseUrl, "root", "wenliu125&*");
    }
    
    private void testConnection(String url, String username, String password) {
        System.out.println("测试连接: " + url);
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            System.out.println("✅ 连接成功");
        } catch (Exception e) {
            System.out.println("❌ 连接失败: " + e.getMessage());
        }
    }
    
    /**
     * 从数据库URL中提取数据库名
     */
    private String extractDatabaseName(String url) {
        try {
            // 先找到协议部分的结束位置 (jdbc:mysql://)
            String protocolPrefix = "jdbc:mysql://";
            if (!url.startsWith(protocolPrefix)) {
                return "pms_db";
            }
            
            // 从协议后开始查找
            String afterProtocol = url.substring(protocolPrefix.length());
            
            // 找到第一个斜杠（主机:端口后的斜杠）
            int firstSlashIndex = afterProtocol.indexOf('/');
            if (firstSlashIndex == -1) {
                return "pms_db";
            }
            
            // 从第一个斜杠后开始提取数据库名
            String afterFirstSlash = afterProtocol.substring(firstSlashIndex + 1);
            
            // 查找问号位置（参数开始）
            int questionMarkIndex = afterFirstSlash.indexOf('?');
            if (questionMarkIndex != -1) {
                return afterFirstSlash.substring(0, questionMarkIndex);
            }
            
            return afterFirstSlash;
        } catch (Exception e) {
            System.err.println("提取数据库名失败: " + e.getMessage());
            return "pms_db"; // 默认数据库名
        }
    }
    
    /**
     * 获取基础URL（不包含数据库名）
     */
    private String getBaseUrl(String url) {
        try {
            // 先找到协议部分的结束位置 (jdbc:mysql://)
            String protocolPrefix = "jdbc:mysql://";
            if (!url.startsWith(protocolPrefix)) {
                return url;
            }
            
            // 从协议后开始查找
            String afterProtocol = url.substring(protocolPrefix.length());
            
            // 找到第一个斜杠（主机:端口后的斜杠）
            int firstSlashIndex = afterProtocol.indexOf('/');
            if (firstSlashIndex == -1) {
                return url; // 如果没有斜杠，返回原URL
            }
            
            // 构建基础URL：协议 + 主机:端口 + 斜杠
            String baseUrl = protocolPrefix + afterProtocol.substring(0, firstSlashIndex + 1);
            
            // 查找参数部分
            int questionMarkIndex = url.indexOf('?');
            if (questionMarkIndex != -1) {
                // 如果有参数，添加参数部分
                String params = url.substring(questionMarkIndex);
                return baseUrl + params;
            }
            
            return baseUrl;
        } catch (Exception e) {
            System.err.println("构建基础URL失败: " + e.getMessage());
            return url;
        }
    }
}