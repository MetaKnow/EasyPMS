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
import java.util.ArrayList;
import java.util.List;

/**
 * 数据库表结构验证测试
 */
@SpringBootTest
@ActiveProfiles("test")
public class DatabaseStructureTest {

    @Autowired
    private DataSource dataSource;

    /**
     * 验证所有新增表是否存在
     */
    @Test
    public void testTablesExist() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            // 需要验证的表列表
            String[] expectedTables = {
                "customer",
                "afterservice_project", 
                "standard_construct_step",
                "construct_deliverable",
                "construct_milestone",
                "nonstandard_construct_step",
                "afterservice_event",
                "afterservice_deliverable",
                "project_relations"
            };
            
            System.out.println("🔍 验证数据库表结构...");
            
            for (String tableName : expectedTables) {
                ResultSet tables = metaData.getTables(null, null, tableName, new String[]{"TABLE"});
                if (tables.next()) {
                    System.out.println("✅ 表 " + tableName + " 存在");
                } else {
                    System.out.println("❌ 表 " + tableName + " 不存在");
                }
                tables.close();
            }
        }
    }

    /**
     * 验证外键约束
     */
    @Test
    public void testForeignKeys() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            System.out.println("\n🔗 验证外键约束...");
            
            // 验证constructing_project表的customerId外键
            verifySpecificForeignKey(metaData, "constructing_project", "customerId", "customer", "customerId");
            
            // 验证standard_construct_step表的外键
            verifySpecificForeignKey(metaData, "standard_construct_step", "director", "user", "userId");
            verifySpecificForeignKey(metaData, "standard_construct_step", "afterServiceProjectId", "afterservice_project", "projectId");
            
            // 验证construct_deliverable表的外键
            verifySpecificForeignKey(metaData, "construct_deliverable", "uploadUser", "user", "userId");
            
            // 验证nonstandard_construct_step表的外键
            verifySpecificForeignKey(metaData, "nonstandard_construct_step", "director", "user", "userId");
            
            // 验证afterService_project表的外键
            verifySpecificForeignKey(metaData, "afterservice_project", "director", "user", "userId");
            
            // 验证afterService_event表的外键
            verifySpecificForeignKey(metaData, "afterservice_event", "director", "user", "userId");
            verifySpecificForeignKey(metaData, "afterservice_event", "afterServiceProjectId", "afterservice_project", "projectId");
            
            // 验证afterService_deliverable表的外键
            verifySpecificForeignKey(metaData, "afterservice_deliverable", "projectId", "afterservice_project", "projectId");
            verifySpecificForeignKey(metaData, "afterservice_deliverable", "eventId", "afterservice_event", "eventId");
            verifySpecificForeignKey(metaData, "afterservice_deliverable", "uploadUser", "user", "userId");
            
            // 验证project_relations表的外键
            verifySpecificForeignKey(metaData, "project_relations", "projectId", "constructing_project", "projectId");
            verifySpecificForeignKey(metaData, "project_relations", "sstepId", "standard_construct_step", "sstepId");
            verifySpecificForeignKey(metaData, "project_relations", "milestoneId", "construct_milestone", "milestoneId");
            verifySpecificForeignKey(metaData, "project_relations", "nstepId", "nonstandard_construct_step", "nstepId");
            verifySpecificForeignKey(metaData, "project_relations", "deliverableId", "construct_deliverable", "deliverableId");
        }
    }

    /**
     * 验证表的字段结构
     */
    @Test
    public void testTableColumns() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            System.out.println("\n📋 验证表字段结构...");
            
            // 验证customer表字段
            verifyTableColumns(metaData, "customer", new String[]{
                "customerId", "customerName", "contact", "phoneNumber", "province", "customerRank"
            });
            
            // 验证afterService_project表字段
            verifyTableColumns(metaData, "afterservice_project", new String[]{
                "projectId", "projectName", "arcSystem", "director", "ServiceYear", 
                "startDate", "endDate", "serviceState", "totalHours"
            });
            
            // 验证standard_construct_step表字段
            verifyTableColumns(metaData, "standard_construct_step", new String[]{
                "sstepId", "sstepName", "director", "planStartDate", "planEndDate",
                "actualStartDate", "actualEndDate", "planPeriod", "actualPeriod", "afterServiceProjectId"
            });
            
            System.out.println("✅ 表字段结构验证完成");
        }
    }

    /**
     * 验证外键约束是否存在
     */
    private void verifyForeignKey(DatabaseMetaData metaData, String tableName, String referencedTable) throws SQLException {
        ResultSet foreignKeys = metaData.getImportedKeys(null, null, tableName);
        boolean found = false;
        
        while (foreignKeys.next()) {
            String pkTableName = foreignKeys.getString("PKTABLE_NAME");
            if (referencedTable.equals(pkTableName)) {
                found = true;
                String fkColumnName = foreignKeys.getString("FKCOLUMN_NAME");
                String pkColumnName = foreignKeys.getString("PKCOLUMN_NAME");
                System.out.println("✅ " + tableName + "." + fkColumnName + " -> " + referencedTable + "." + pkColumnName);
                break;
            }
        }
        
        if (!found) {
            System.out.println("❌ " + tableName + " 缺少对 " + referencedTable + " 的外键约束");
        }
        
        foreignKeys.close();
    }

    /**
     * 验证特定的外键约束是否存在
     */
    private void verifySpecificForeignKey(DatabaseMetaData metaData, String tableName, String fkColumn, String referencedTable, String pkColumn) throws SQLException {
        ResultSet foreignKeys = metaData.getImportedKeys(null, null, tableName);
        boolean found = false;
        
        while (foreignKeys.next()) {
            String pkTableName = foreignKeys.getString("PKTABLE_NAME");
            String fkColumnName = foreignKeys.getString("FKCOLUMN_NAME");
            String pkColumnName = foreignKeys.getString("PKCOLUMN_NAME");
            
            if (referencedTable.equals(pkTableName) && fkColumn.equals(fkColumnName) && pkColumn.equals(pkColumnName)) {
                found = true;
                System.out.println("✅ " + tableName + "." + fkColumnName + " -> " + referencedTable + "." + pkColumnName);
                break;
            }
        }
        
        if (!found) {
            System.out.println("❌ " + tableName + "." + fkColumn + " -> " + referencedTable + "." + pkColumn + " (外键约束缺失)");
        }
        
        foreignKeys.close();
    }

    /**
     * 验证表的字段是否存在
     */
    private void verifyTableColumns(DatabaseMetaData metaData, String tableName, String[] expectedColumns) throws SQLException {
        ResultSet columns = metaData.getColumns(null, null, tableName, null);
        List<String> actualColumns = new ArrayList<>();
        
        while (columns.next()) {
            actualColumns.add(columns.getString("COLUMN_NAME"));
        }
        
        System.out.println("📋 表 " + tableName + " 的字段:");
        for (String expectedColumn : expectedColumns) {
            if (actualColumns.contains(expectedColumn)) {
                System.out.println("  ✅ " + expectedColumn);
            } else {
                System.out.println("  ❌ " + expectedColumn + " (缺失)");
            }
        }
        
        columns.close();
    }
}