package com.missoft.pms.config;

import org.springframework.util.DigestUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 数据库迁移管理器
 * 负责扫描迁移脚本并执行数据库升级
 * 
 * @author MissoftPMS
 * @version 1.0.0
 */
public class DatabaseMigrationManager {
    
    private static final String MIGRATION_TABLE = "migration_history";

    private static final Pattern VERSION_PATTERN = Pattern.compile("^(\\d{3})_.*\\.sql$");
    
    private final String datasourceUrl;
    private final String username;
    private final String password;
    
    public DatabaseMigrationManager(String datasourceUrl, String username, String password) {
        this.datasourceUrl = datasourceUrl;
        this.username = username;
        this.password = password;
    }
    
    /**
     * 执行数据库迁移
     */
    public void migrate() {
        System.out.println("🔄 开始数据库迁移检查...");
        
        try {
            // 1. 确保迁移历史表存在
            ensureMigrationTableExists();
            
            // 2. 获取已执行的迁移版本
            Set<String> executedVersions = getExecutedVersions();
            System.out.println("📋 已执行的迁移版本: " + executedVersions);
            
            // 3. 扫描迁移脚本
            List<MigrationScript> pendingScripts = scanPendingMigrations(executedVersions);
            
            if (pendingScripts.isEmpty()) {
                System.out.println("✅ 数据库已是最新版本，无需迁移");
                return;
            }
            
            System.out.println("📦 发现 " + pendingScripts.size() + " 个待执行的迁移脚本");
            
            // 4. 按版本号排序执行迁移
            pendingScripts.sort(Comparator.comparing(MigrationScript::getVersion));
            
            for (MigrationScript script : pendingScripts) {
                executeMigration(script);
            }
            
            System.out.println("🎉 数据库迁移完成!");
            
        } catch (Exception e) {
            System.err.println("❌ 数据库迁移失败: " + e.getMessage());
            throw new RuntimeException("数据库迁移失败", e);
        }
    }
    
    /**
     * 确保迁移历史表存在
     */
    private void ensureMigrationTableExists() throws SQLException, IOException {
        try (Connection connection = DriverManager.getConnection(datasourceUrl, username, password);
             Statement statement = connection.createStatement()) {
            
            // 检查迁移表是否存在
            String checkTableSQL = "SELECT COUNT(*) FROM information_schema.tables " +
                                  "WHERE table_schema = DATABASE() AND table_name = '" + MIGRATION_TABLE + "'";
            
            ResultSet resultSet = statement.executeQuery(checkTableSQL);
            resultSet.next();
            
            if (resultSet.getInt(1) == 0) {
                System.out.println("📋 迁移历史表不存在，正在创建...");
                // 执行创建迁移表的脚本
                executeMigrationTableScript();
            } else {
                System.out.println("✅ 迁移历史表已存在");
            }
        }
    }
    
    /**
     * 执行创建迁移表的脚本
     */
    private void executeMigrationTableScript() throws SQLException, IOException {
        Path scriptPath = getScriptsDirectory().resolve("001_create_migration_table.sql");
        
        if (!Files.exists(scriptPath)) {
            throw new RuntimeException("迁移表创建脚本不存在: " + scriptPath);
        }
        
        String scriptContent = Files.readString(scriptPath, StandardCharsets.UTF_8);
        executeScript(scriptContent, "001", "001_create_migration_table.sql", "Create migration history table");
    }
    
    /**
     * 获取已执行的迁移版本
     */
    private Set<String> getExecutedVersions() throws SQLException {
        Set<String> versions = new HashSet<>();
        
        try (Connection connection = DriverManager.getConnection(datasourceUrl, username, password);
             Statement statement = connection.createStatement()) {
            
            String query = "SELECT version FROM " + MIGRATION_TABLE + " WHERE success = TRUE";
            ResultSet resultSet = statement.executeQuery(query);
            
            while (resultSet.next()) {
                versions.add(resultSet.getString("version"));
            }
        }
        
        return versions;
    }
    
    /**
     * 获取脚本目录路径
     */
    private Path getScriptsDirectory() {
        // 尝试多个可能的路径
        String[] possiblePaths = {
            "../database/scripts",
            "database/scripts",
            "../../database/scripts"
        };
        
        for (String pathStr : possiblePaths) {
            Path path = Paths.get(pathStr);
            if (Files.exists(path)) {
                return path;
            }
        }
        
        // 如果都不存在，返回默认路径
        return Paths.get("../database/scripts");
    }
    
    /**
     * 扫描待执行的迁移脚本
     */
    private List<MigrationScript> scanPendingMigrations(Set<String> executedVersions) throws IOException {
        List<MigrationScript> pendingScripts = new ArrayList<>();
        Path scriptsDir = getScriptsDirectory();
        
        if (!Files.exists(scriptsDir)) {
            System.out.println("⚠️  迁移脚本目录不存在: " + scriptsDir.toAbsolutePath());
            return pendingScripts;
        }
        
        Files.list(scriptsDir)
            .filter(Files::isRegularFile)
            .filter(path -> path.toString().endsWith(".sql"))
            .forEach(path -> {
                String fileName = path.getFileName().toString();
                Matcher matcher = VERSION_PATTERN.matcher(fileName);
                
                if (matcher.matches()) {
                    String version = matcher.group(1);
                    
                    if (!executedVersions.contains(version)) {
                        try {
                            String content = Files.readString(path, StandardCharsets.UTF_8);
                            String checksum = DigestUtils.md5DigestAsHex(content.getBytes(StandardCharsets.UTF_8));
                            
                            pendingScripts.add(new MigrationScript(
                                version, fileName, extractDescription(content), content, checksum
                            ));
                        } catch (IOException e) {
                            System.err.println("⚠️  读取迁移脚本失败: " + fileName + ", " + e.getMessage());
                        }
                    }
                } else {
                    System.out.println("⚠️  跳过不符合命名规范的脚本: " + fileName);
                }
            });
        
        return pendingScripts;
    }
    
    /**
     * 从脚本内容中提取描述
     */
    private String extractDescription(String content) {
        String[] lines = content.split("\\n");
        for (String line : lines) {
            if (line.trim().startsWith("-- Description:")) {
                return line.substring(line.indexOf(":") + 1).trim();
            }
        }
        return "No description";
    }
    
    /**
     * 执行迁移脚本
     */
    private void executeMigration(MigrationScript script) {
        System.out.println("🔄 执行迁移: " + script.getFileName() + " (版本: " + script.getVersion() + ")");
        
        long startTime = System.currentTimeMillis();
        
        try {
            executeScript(script.getContent(), script.getVersion(), script.getFileName(), script.getDescription());
            
            long executionTime = System.currentTimeMillis() - startTime;
            recordMigration(script, executionTime, true, null);
            
            System.out.println("✅ 迁移完成: " + script.getFileName() + " (耗时: " + executionTime + "ms)");
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            recordMigration(script, executionTime, false, e.getMessage());
            
            System.err.println("❌ 迁移失败: " + script.getFileName() + ", 错误: " + e.getMessage());
            throw new RuntimeException("迁移脚本执行失败: " + script.getFileName(), e);
        }
    }
    
    /**
     * 执行SQL脚本
     */
    private void executeScript(String scriptContent, String version, String fileName, String description) throws SQLException {
        try (Connection connection = DriverManager.getConnection(datasourceUrl, username, password)) {
            connection.setAutoCommit(false);
            
            try (Statement statement = connection.createStatement()) {
                List<String> sqlStatements = splitSqlStatements(scriptContent);
                
                for (String sql : sqlStatements) {
                    sql = sql.trim();
                    if (!sql.isEmpty() && !sql.startsWith("--") && !sql.toUpperCase().startsWith("USE")) {
                        System.out.println("🔄 执行SQL: " + sql.substring(0, Math.min(sql.length(), 50)) + "...");
                        try {
                            statement.execute(sql);
                        } catch (SQLException sqlException) {
                            if (isIgnorableSqlError(sqlException)) {
                                System.out.println("⚠️  SQL已存在，跳过: " + sqlException.getMessage());
                                continue;
                            }
                            throw sqlException;
                        }
                    }
                }
                
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    private boolean isIgnorableSqlError(SQLException exception) {
        int errorCode = exception.getErrorCode();
        return errorCode == 1060 || errorCode == 1061 || errorCode == 1826 || errorCode == 1359;
    }
    
    /**
     * 智能分割SQL语句
     */
    private List<String> splitSqlStatements(String scriptContent) {
        List<String> statements = new ArrayList<>();
        StringBuilder currentStatement = new StringBuilder();
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        boolean inComment = false;
        
        String[] lines = scriptContent.split("\\n");
        
        for (String line : lines) {
            line = line.trim();
            
            // 跳过空行和注释行
            if (line.isEmpty() || line.startsWith("--")) {
                continue;
            }
            
            // 处理每个字符
            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                
                if (c == '\'' && !inDoubleQuote) {
                    inSingleQuote = !inSingleQuote;
                } else if (c == '"' && !inSingleQuote) {
                    inDoubleQuote = !inDoubleQuote;
                } else if (c == ';' && !inSingleQuote && !inDoubleQuote) {
                    // 找到语句结束符
                    String statement = currentStatement.toString().trim();
                    if (!statement.isEmpty()) {
                        statements.add(statement);
                    }
                    currentStatement = new StringBuilder();
                    continue;
                }
                
                currentStatement.append(c);
            }
            
            // 添加换行符（除非是最后一行）
            currentStatement.append(" ");
        }
        
        // 添加最后一个语句（如果有）
        String lastStatement = currentStatement.toString().trim();
        if (!lastStatement.isEmpty()) {
            statements.add(lastStatement);
        }
        
        return statements;
    }
    
    /**
     * 记录迁移执行结果
     */
    private void recordMigration(MigrationScript script, long executionTime, boolean success, String errorMessage) {
        try (Connection connection = DriverManager.getConnection(datasourceUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(
                 "INSERT INTO " + MIGRATION_TABLE + 
                 " (version, script_name, description, checksum, execution_time_ms, success, error_message) " +
                 "VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            
            statement.setString(1, script.getVersion());
            statement.setString(2, script.getFileName());
            statement.setString(3, script.getDescription());
            statement.setString(4, script.getChecksum());
            statement.setLong(5, executionTime);
            statement.setBoolean(6, success);
            statement.setString(7, errorMessage);
            
            statement.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("⚠️  记录迁移历史失败: " + e.getMessage());
        }
    }
    
    /**
     * 迁移脚本信息类
     */
    private static class MigrationScript {
        private final String version;
        private final String fileName;
        private final String description;
        private final String content;
        private final String checksum;
        
        public MigrationScript(String version, String fileName, String description, String content, String checksum) {
            this.version = version;
            this.fileName = fileName;
            this.description = description;
            this.content = content;
            this.checksum = checksum;
        }
        
        public String getVersion() { return version; }
        public String getFileName() { return fileName; }
        public String getDescription() { return description; }
        public String getContent() { return content; }
        public String getChecksum() { return checksum; }
    }
}
