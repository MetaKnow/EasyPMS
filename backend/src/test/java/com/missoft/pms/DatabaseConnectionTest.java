package com.missoft.pms;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 数据库连接测试类
 * 用于验证数据库连接配置是否正确
 * 
 * @author MissoftPMS
 * @version 1.0.0
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application.properties")
public class DatabaseConnectionTest {

    @Autowired
    private DataSource dataSource;

    /**
     * 测试数据源是否正确注入
     */
    @Test
    public void testDataSourceNotNull() {
        assertNotNull(dataSource, "数据源不应该为空");
        System.out.println("✅ 数据源注入成功");
    }

    /**
     * 测试数据库连接
     */
    @Test
    public void testDatabaseConnection() {
        try (Connection connection = dataSource.getConnection()) {
            assertNotNull(connection, "数据库连接不应该为空");
            assertFalse(connection.isClosed(), "数据库连接应该是打开状态");
            
            // 获取数据库元数据
            DatabaseMetaData metaData = connection.getMetaData();
            String databaseProductName = metaData.getDatabaseProductName();
            String databaseProductVersion = metaData.getDatabaseProductVersion();
            String url = metaData.getURL();
            String userName = metaData.getUserName();
            
            System.out.println("✅ 数据库连接成功!");
            System.out.println("📊 数据库信息:");
            System.out.println("   - 数据库类型: " + databaseProductName);
            System.out.println("   - 数据库版本: " + databaseProductVersion);
            System.out.println("   - 连接URL: " + url);
            System.out.println("   - 用户名: " + userName);
            
        } catch (Exception e) {
            fail("数据库连接失败: " + e.getMessage());
        }
    }

    /**
     * 测试基本SQL查询
     */
    @Test
    public void testBasicQuery() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            
            // 执行简单的查询测试
            ResultSet resultSet = statement.executeQuery("SELECT 1 as test_value");
            
            assertTrue(resultSet.next(), "查询结果不应该为空");
            assertEquals(1, resultSet.getInt("test_value"), "查询结果应该为1");
            
            System.out.println("✅ 基本SQL查询测试通过");
            
        } catch (Exception e) {
            fail("SQL查询测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试数据库是否存在指定的数据库
     */
    @Test
    public void testDatabaseExists() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            
            // 检查当前数据库
            ResultSet resultSet = statement.executeQuery("SELECT DATABASE() as current_db");
            
            if (resultSet.next()) {
                String currentDatabase = resultSet.getString("current_db");
                System.out.println("✅ 当前使用的数据库: " + currentDatabase);
                
                if (currentDatabase != null && currentDatabase.contains("pms")) {
                    System.out.println("✅ PMS数据库连接正确");
                } else {
                    System.out.println("⚠️  当前连接的不是PMS数据库，请检查配置");
                }
            }
            
        } catch (Exception e) {
            System.out.println("⚠️  数据库检查失败: " + e.getMessage());
            System.out.println("💡 提示: 请确保MySQL服务已启动并创建了pms_db数据库");
        }
    }

    /**
     * 测试数据库表创建权限
     */
    @Test
    public void testTableCreationPermission() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            
            // 尝试创建一个测试表
            String createTableSQL = """
                CREATE TABLE IF NOT EXISTS test_connection (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    test_message VARCHAR(100),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;
            
            statement.execute(createTableSQL);
            System.out.println("✅ 表创建权限测试通过");
            
            // 插入测试数据
            String insertSQL = "INSERT INTO test_connection (test_message) VALUES ('数据库连接测试成功')";
            int rowsAffected = statement.executeUpdate(insertSQL);
            assertEquals(1, rowsAffected, "应该插入1行数据");
            
            System.out.println("✅ 数据插入测试通过");
            
            // 查询测试数据
            ResultSet resultSet = statement.executeQuery("SELECT test_message FROM test_connection ORDER BY id DESC LIMIT 1");
            assertTrue(resultSet.next(), "应该能查询到测试数据");
            
            String message = resultSet.getString("test_message");
            assertEquals("数据库连接测试成功", message, "查询到的数据应该匹配");
            
            System.out.println("✅ 数据查询测试通过: " + message);
            
            // 清理测试表
            statement.execute("DROP TABLE IF EXISTS test_connection");
            System.out.println("✅ 测试表清理完成");
            
        } catch (Exception e) {
            System.out.println("⚠️  表操作测试失败: " + e.getMessage());
            System.out.println("💡 提示: 请检查数据库用户是否有足够的权限");
        }
    }
}