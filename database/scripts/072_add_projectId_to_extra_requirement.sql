-- =============================================
-- Migration 072: Add projectId to extra_requirement
-- Description: Link extra requirements to constructing_project via projectId FK
-- =============================================

USE `pms_db`;

-- 幂等添加 projectId 列
SET @col_exists := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'extra_requirement' AND COLUMN_NAME = 'projectId'
);
SET @ddl := IF(@col_exists = 0,
  'ALTER TABLE `extra_requirement` ADD COLUMN `projectId` BIGINT NOT NULL COMMENT ''所属在建项目ID，constructing_project表外键'' AFTER `requirementId`',
  'SELECT 0 FROM DUAL'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 幂等创建索引
SET @idx_exists := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'extra_requirement' AND INDEX_NAME = 'idx_extra_requirement_project'
);
SET @ddl := IF(@idx_exists = 0,
  'CREATE INDEX `idx_extra_requirement_project` ON `extra_requirement`(`projectId`)',
  'SELECT 0 FROM DUAL'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 幂等添加外键约束
SET @fk_exists := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'extra_requirement' AND CONSTRAINT_NAME = 'fk_extra_requirement_project'
);
SET @ddl := IF(@fk_exists = 0,
  'ALTER TABLE `extra_requirement` ADD CONSTRAINT `fk_extra_requirement_project` FOREIGN KEY (`projectId`) REFERENCES `constructing_project`(`projectId`) ON DELETE CASCADE ON UPDATE CASCADE',
  'SELECT 0 FROM DUAL'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SELECT 'Migration 072 executed: extra_requirement.projectId ensured with FK' AS message;
