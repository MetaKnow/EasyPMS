-- 创建数据库备份表
-- 文件: 070_create_databaseBackup_table.sql
-- 描述: 创建databaseBackup表，用于存储数据库备份记录

USE `pms_db`;

CREATE TABLE IF NOT EXISTS `databaseBackup` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `fileName` VARCHAR(255) NOT NULL COMMENT '备份文件名',
    `backupDate` DATETIME NOT NULL COMMENT '备份时间',
    `backupState` VARCHAR(50) NOT NULL COMMENT '备份状态',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据库备份记录表';

-- 添加索引
CREATE INDEX `idx_backup_date` ON `databaseBackup`(`backupDate`);
CREATE INDEX `idx_backup_state` ON `databaseBackup`(`backupState`);

SELECT 'Database backup table created successfully!' as message;
