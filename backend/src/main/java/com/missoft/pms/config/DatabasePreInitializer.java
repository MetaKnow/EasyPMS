package com.missoft.pms.config;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * 数据库预初始化器
 * 在Spring容器初始化之前执行，确保数据库存在
 * 
 * @author MissoftPMS
 * @version 1.0.0
 */
public class DatabasePreInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        System.out.println("🚀 数据库预初始化器开始执行...");
        
        Environment env = applicationContext.getEnvironment();
        
        // 从环境变量中获取数据库配置
        String datasourceUrl = env.getProperty("spring.datasource.url");
        String username = env.getProperty("spring.datasource.username");
        String password = env.getProperty("spring.datasource.password");
        
        System.out.println("📋 数据库配置信息:");
        System.out.println("   - URL: " + (datasourceUrl != null ? datasourceUrl.replaceAll("password=[^&]*", "password=***") : "未配置"));
        System.out.println("   - 用户名: " + (username != null ? username : "未配置"));
        System.out.println("   - 密码: " + (password != null ? "***" : "未配置"));
        
        if (datasourceUrl == null || username == null || password == null) {
            System.err.println("❌ 数据库配置不完整，无法进行预初始化");
            System.err.println("💡 请检查application.properties中的数据库配置");
            throw new RuntimeException("数据库配置不完整，应用启动失败");
        }
        
        try {
            createDatabaseIfNotExists(datasourceUrl, username, password);
            
            // 执行数据库迁移
            System.out.println("🔄 开始数据库迁移检查...");
            DatabaseMigrationManager migrationManager = new DatabaseMigrationManager(datasourceUrl, username, password);
            migrationManager.migrate();
            
            System.out.println("✅ 数据库预初始化和迁移完成，应用可以正常启动");
        } catch (Exception e) {
            System.err.println("❌ 数据库预初始化失败: " + e.getMessage());
            throw new RuntimeException("数据库预初始化失败，应用启动终止", e);
        }
    }
    
    private void createDatabaseIfNotExists(String datasourceUrl, String username, String password) {
        String databaseName = extractDatabaseName(datasourceUrl);
        String baseUrl = getBaseUrl(datasourceUrl);
        
        System.out.println("🔍 目标数据库: " + databaseName);
        System.out.println("🔗 原始URL: " + datasourceUrl);
        System.out.println("🔗 基础URL: " + baseUrl);
        System.out.println("🔗 用户名: " + username);
        
        // 第一步：检查MySQL服务连接
        if (!checkMySQLConnection(baseUrl, username, password)) {
            throw new RuntimeException("MySQL服务连接失败，请检查服务状态和连接配置");
        }
        
        // 第二步：检查数据库权限
        if (!checkDatabasePermissions(baseUrl, username, password)) {
            throw new RuntimeException("数据库权限不足，请检查用户权限配置");
        }
        
        // 第三步：检查并创建数据库
        try (Connection connection = DriverManager.getConnection(baseUrl, username, password);
             Statement statement = connection.createStatement()) {
            
            // 检查数据库是否存在
            String checkDbSQL = "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = '" + databaseName + "'";
            var resultSet = statement.executeQuery(checkDbSQL);
            
            if (resultSet.next()) {
                System.out.println("✅ 数据库 '" + databaseName + "' 已存在");
                // 验证数据库连接
                validateDatabaseConnection(datasourceUrl, username, password);
            } else {
                System.out.println("📋 数据库不存在，开始自动创建...");
                // 创建数据库
                String createDbSQL = "CREATE DATABASE IF NOT EXISTS `" + databaseName + "` " +
                                    "CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci";
                statement.executeUpdate(createDbSQL);
                System.out.println("🎉 数据库 '" + databaseName + "' 创建成功!");
                
                // 验证创建结果
                validateDatabaseConnection(datasourceUrl, username, password);
            }
            
        } catch (Exception e) {
            System.err.println("❌ 数据库操作失败: " + e.getMessage());
            throw new RuntimeException("数据库操作失败: " + e.getMessage(), e);
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
    
    /**
     * 检查MySQL服务连接
     */
    private boolean checkMySQLConnection(String baseUrl, String username, String password) {
        System.out.println("🔗 检查MySQL服务连接...");
        try (Connection connection = DriverManager.getConnection(baseUrl, username, password)) {
            System.out.println("✅ MySQL服务连接正常");
            return true;
        } catch (Exception e) {
            System.err.println("❌ MySQL连接失败: " + e.getMessage());
            System.err.println("💡 请检查:");
            System.err.println("   1. MySQL服务是否已启动");
            System.err.println("   2. 连接地址和端口是否正确");
            System.err.println("   3. 用户名密码是否正确");
            return false;
        }
    }
    
    /**
     * 检查数据库权限
     */
    private boolean checkDatabasePermissions(String baseUrl, String username, String password) {
        System.out.println("🔐 检查数据库权限...");
        try (Connection connection = DriverManager.getConnection(baseUrl, username, password);
             Statement statement = connection.createStatement()) {
            
            // 检查创建数据库权限
            var resultSet = statement.executeQuery("SHOW GRANTS FOR CURRENT_USER()");
            boolean hasCreatePrivilege = false;
            
            while (resultSet.next()) {
                String grant = resultSet.getString(1).toUpperCase();
                if (grant.contains("ALL PRIVILEGES") || grant.contains("CREATE")) {
                    hasCreatePrivilege = true;
                    break;
                }
            }
            
            if (hasCreatePrivilege) {
                System.out.println("✅ 数据库权限检查通过");
                return true;
            } else {
                System.err.println("❌ 用户没有创建数据库的权限");
                System.err.println("💡 请使用具有CREATE权限的用户，或联系管理员授权");
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("⚠️  权限检查失败: " + e.getMessage());
            System.err.println("💡 假设用户有足够权限，继续执行...");
            return true; // 权限检查失败时假设有权限，让后续操作验证
        }
    }
    
    /**
     * 验证数据库连接
     */
    private void validateDatabaseConnection(String datasourceUrl, String username, String password) {
        System.out.println("🔍 验证数据库连接...");
        try (Connection connection = DriverManager.getConnection(datasourceUrl, username, password);
             Statement statement = connection.createStatement()) {
            
            // 执行简单查询验证连接
            var resultSet = statement.executeQuery("SELECT 1");
            if (resultSet.next()) {
                System.out.println("✅ 数据库连接验证成功");
            }
            
        } catch (Exception e) {
            System.err.println("❌ 数据库连接验证失败: " + e.getMessage());
            throw new RuntimeException("数据库连接验证失败", e);
        }
    }
}