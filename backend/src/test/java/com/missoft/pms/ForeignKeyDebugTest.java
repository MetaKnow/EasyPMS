package com.missoft.pms;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 外键约束调试测试
 */
@SpringBootTest
@ActiveProfiles("test")
public class ForeignKeyDebugTest {

    @Autowired
    private DataSource dataSource;

    /**
     * 列出所有外键约束
     */
    @Test
    public void listAllForeignKeys() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            String[] tables = {
                "standard_construct_step",
                "afterService_event", 
                "afterService_deliverable"
            };
            
            for (String tableName : tables) {
                System.out.println("\n📋 表 " + tableName + " 的所有外键约束:");
                ResultSet foreignKeys = metaData.getImportedKeys(null, null, tableName);
                
                boolean hasAnyFK = false;
                while (foreignKeys.next()) {
                    hasAnyFK = true;
                    String fkName = foreignKeys.getString("FK_NAME");
                    String fkColumn = foreignKeys.getString("FKCOLUMN_NAME");
                    String pkTable = foreignKeys.getString("PKTABLE_NAME");
                    String pkColumn = foreignKeys.getString("PKCOLUMN_NAME");
                    
                    System.out.println("  🔗 " + fkName + ": " + tableName + "." + fkColumn + " -> " + pkTable + "." + pkColumn);
                }
                
                if (!hasAnyFK) {
                    System.out.println("  ❌ 没有找到任何外键约束");
                }
                
                foreignKeys.close();
            }
        }
    }

    /**
     * 检查表是否存在
     */
    @Test
    public void checkTablesExist() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            String[] tables = {
                "afterService_project",
                "afterService_event",
                "afterService_deliverable",
                "standard_construct_step"
            };
            
            System.out.println("🔍 检查表是否存在:");
            for (String tableName : tables) {
                ResultSet rs = metaData.getTables(null, null, tableName, new String[]{"TABLE"});
                if (rs.next()) {
                    System.out.println("  ✅ " + tableName + " 存在");
                } else {
                    System.out.println("  ❌ " + tableName + " 不存在");
                }
                rs.close();
            }
        }
    }
}