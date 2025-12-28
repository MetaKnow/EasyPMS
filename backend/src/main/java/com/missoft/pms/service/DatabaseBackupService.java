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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    // 每天凌晨1点执行
    @Scheduled(cron = "0 0 1 * * ?")
    public void scheduledBackup() {
        logger.info("Starting scheduled database backup...");
        performBackup();
        cleanupOldBackups();
    }

    public void performBackup() {
        LocalDateTime now = LocalDateTime.now();
        String fileName = "pms_backup_" + now.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".sql";
        
        Path backupDir = getBackupDirPath();
        try {
            Files.createDirectories(backupDir);
        } catch (Exception e) {
            logger.error("Failed to create backup directory: " + backupDir.toAbsolutePath(), e);
            saveBackupRecord(fileName, now, "失败");
            return;
        }

        File backupFile = backupDir.resolve(fileName).toFile();
        
        try {
            String dbName = extractDatabaseName(dbUrl);
            String host = extractHost(dbUrl);
            String port = extractPort(dbUrl);

            // 构建 mysqldump 命令
            // 注意：生产环境应考虑安全性，避免在命令行暴露密码。但此处为简化实现。
            // 使用 ProcessBuilder
            ProcessBuilder pb = new ProcessBuilder(
                    "mysqldump",
                    "-h" + host,
                    "-P" + port,
                    "-u" + dbUser,
                    "-p" + dbPassword,
                    dbName
            );
            
            // 重定向输出到文件
            pb.redirectOutput(backupFile);
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);

            Process process = pb.start();
            boolean finished = process.waitFor(10, TimeUnit.MINUTES);

            if (finished && process.exitValue() == 0) {
                logger.info("Database backup successful: " + backupFile.getAbsolutePath());
                saveBackupRecord(fileName, now, "成功");
            } else {
                logger.error("Database backup failed. Exit code: " + (finished ? process.exitValue() : "timeout"));
                saveBackupRecord(fileName, now, "失败");
                // 如果是空文件或失败，可能需要删除
                if (backupFile.exists() && backupFile.length() == 0) {
                    backupFile.delete();
                }
            }

        } catch (Exception e) {
            logger.error("Error during database backup", e);
            saveBackupRecord(fileName, now, "失败");
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
}
