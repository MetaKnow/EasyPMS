package com.missoft.pms.service;

import com.missoft.pms.entity.DatabaseBackup;
import com.missoft.pms.repository.DatabaseBackupRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class DatabaseBackupService {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseBackupService.class);

    @Autowired
    private DatabaseBackupRepository backupRepository;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${pms.backup.mysqldump:}")
    private String mysqldumpPath;

    // 每天凌晨1点执行
    @Scheduled(cron = "0 0 1 * * ?")
    public void scheduledBackup() {
        logger.info("Starting scheduled database backup...");
        performBackup();
        cleanupOldBackups();
    }

    public BackupResult performBackup() {
        LocalDateTime now = LocalDateTime.now();
        String fileName = "pms_backup_" + now.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".sql";
        
        Path backupDir = getBackupDirPath();
        try {
            Files.createDirectories(backupDir);
        } catch (Exception e) {
            logger.error("Failed to create backup directory: " + backupDir.toAbsolutePath(), e);
            saveBackupRecord(fileName, now, "失败");
            return BackupResult.failure("备份目录创建失败");
        }

        File backupFile = backupDir.resolve(fileName).toFile();
        
        try {
            String dbName = extractDatabaseName(dbUrl);
            String host = extractHost(dbUrl);
            String port = extractPort(dbUrl);

            String mysqldumpExe = resolveMysqldumpExecutable();
            List<String> command = new ArrayList<>();
            command.add(mysqldumpExe);
            command.add("-h");
            command.add(host);
            command.add("-P");
            command.add(port);
            command.add("-u");
            command.add(dbUser);
            command.add("--single-transaction");
            command.add(dbName);

            ProcessBuilder pb = new ProcessBuilder(command);
            if (dbPassword != null) {
                pb.environment().put("MYSQL_PWD", dbPassword);
            }
            
            // 重定向输出到文件
            pb.redirectOutput(backupFile);
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);

            Process process = pb.start();
            boolean finished = process.waitFor(10, TimeUnit.MINUTES);

            if (finished && process.exitValue() == 0) {
                logger.info("Database backup successful: " + backupFile.getAbsolutePath());
                saveBackupRecord(fileName, now, "成功");
                return BackupResult.success(fileName, backupFile.getAbsolutePath());
            } else {
                logger.error("Database backup failed. Exit code: " + (finished ? process.exitValue() : "timeout"));
                saveBackupRecord(fileName, now, "失败");
                // 如果是空文件或失败，可能需要删除
                if (backupFile.exists() && backupFile.length() == 0) {
                    backupFile.delete();
                }
                return BackupResult.failure("备份失败");
            }

        } catch (IOException e) {
            String msg = e.getMessage() == null ? "" : e.getMessage();
            logger.error("Error during database backup", e);
            saveBackupRecord(fileName, now, "失败");
            if (msg.contains("CreateProcess error=2") || msg.contains("error=2")) {
                return BackupResult.failure("系统未找到mysqldump，请安装MySQL客户端工具或配置pms.backup.mysqldump为mysqldump绝对路径");
            }
            return BackupResult.failure("备份执行失败：" + (msg.isEmpty() ? "IO异常" : msg));
        } catch (Exception e) {
            logger.error("Error during database backup", e);
            saveBackupRecord(fileName, now, "失败");
            return BackupResult.failure("备份执行失败");
        }
    }

    private void saveBackupRecord(String fileName, LocalDateTime date, String state) {
        DatabaseBackup backup = new DatabaseBackup();
        backup.setFileName(fileName);
        backup.setBackupDate(date);
        backup.setBackupState(state);
        backupRepository.save(backup);
    }

    public void cleanupOldBackups() {
        // 保留最近15天备份
        List<DatabaseBackup> allBackups = backupRepository.findByBackupStateOrderByBackupDateDesc("成功");
        if (allBackups.size() > 15) {
            List<DatabaseBackup> toDelete = allBackups.subList(15, allBackups.size());
            for (DatabaseBackup backup : toDelete) {
                try {
                    // 删除文件
                    File file = getBackupDirPath().resolve(backup.getFileName()).toFile();
                    if (file.exists()) {
                        file.delete();
                    }
                    // 删除记录
                    backupRepository.delete(backup);
                    logger.info("Deleted old backup: " + backup.getFileName());
                } catch (Exception e) {
                    logger.error("Failed to delete old backup: " + backup.getFileName(), e);
                }
            }
        }
    }

    public List<DatabaseBackup> getRecentSuccessBackups() {
        // 获取最近15条成功记录
        List<DatabaseBackup> all = backupRepository.findByBackupStateOrderByBackupDateDesc("成功");
        return all.size() > 15 ? all.subList(0, 15) : all;
    }

    public File getBackupFile(Long id) {
        DatabaseBackup backup = backupRepository.findById(id).orElse(null);
        if (backup != null) {
            return getBackupDirPath().resolve(backup.getFileName()).toFile();
        }
        return null;
    }

    private Path getBackupDirPath() {
        return resolveProjectRootPath().resolve("database").resolve("backup");
    }

    private Path resolveProjectRootPath() {
        Path current = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
        for (int i = 0; current != null && i < 10; i++) {
            if (Files.isDirectory(current.resolve("database")) && Files.isDirectory(current.resolve("backend"))) {
                return current;
            }
            current = current.getParent();
        }
        return Paths.get(System.getProperty("user.dir")).toAbsolutePath();
    }

    private String resolveMysqldumpExecutable() {
        String configured = mysqldumpPath == null ? "" : mysqldumpPath.trim();
        if (!configured.isEmpty()) {
            boolean looksLikePath = configured.contains(File.separator) || configured.contains("/") || configured.matches("^[A-Za-z]:\\\\.*");
            if (looksLikePath) {
                File f = new File(configured);
                if (!f.exists() || !f.isFile()) {
                    throw new IllegalStateException("配置的mysqldump路径不存在：" + configured);
                }
                return f.getAbsolutePath();
            }
            return configured;
        }
        return "mysqldump";
    }

    private String extractDatabaseName(String url) {
        // jdbc:mysql://localhost:3306/pms_db?...
        int queryStart = url.indexOf('?');
        String cleanUrl = (queryStart > 0) ? url.substring(0, queryStart) : url;
        return cleanUrl.substring(cleanUrl.lastIndexOf('/') + 1);
    }

    private String extractHost(String url) {
        // jdbc:mysql://localhost:3306/...
        String clean = url.substring(13); // remove jdbc:mysql://
        int portIdx = clean.indexOf(':');
        if (portIdx > 0) {
            return clean.substring(0, portIdx);
        }
        int slashIdx = clean.indexOf('/');
        if (slashIdx > 0) {
            return clean.substring(0, slashIdx);
        }
        return "localhost";
    }

    private String extractPort(String url) {
        // jdbc:mysql://localhost:3306/...
        String clean = url.substring(13);
        int portIdx = clean.indexOf(':');
        if (portIdx > 0) {
            int slashIdx = clean.indexOf('/', portIdx);
            if (slashIdx > 0) {
                return clean.substring(portIdx + 1, slashIdx);
            }
            return clean.substring(portIdx + 1);
        }
        return "3306";
    }

    public static class BackupResult {
        private final boolean success;
        private final String message;
        private final String fileName;
        private final String filePath;

        private BackupResult(boolean success, String message, String fileName, String filePath) {
            this.success = success;
            this.message = message;
            this.fileName = fileName;
            this.filePath = filePath;
        }

        public static BackupResult success(String fileName, String filePath) {
            return new BackupResult(true, "备份成功", fileName, filePath);
        }

        public static BackupResult failure(String message) {
            return new BackupResult(false, message == null ? "备份失败" : message, null, null);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public String getFileName() {
            return fileName;
        }

        public String getFilePath() {
            return filePath;
        }
    }
}
