-- =============================================
-- 迁移：为 construct_deliverableFiles 表新增 milestoneId 外键列（幂等）
-- 版本: 057
-- 描述：新增列 milestoneId (BIGINT, NULL)，并建立外键到 construct_milestone(milestoneId)
-- 约束：ON UPDATE CASCADE, ON DELETE SET NULL
-- 注意：仅进行表结构变更，不插入或更新任何数据
-- =============================================

-- 幂等添加列：如果不存在则新增
SET @col_exists := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'construct_deliverableFiles' AND COLUMN_NAME = 'milestoneId'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE construct_deliverableFiles ADD COLUMN milestoneId BIGINT NULL COMMENT ''里程碑ID，construct_milestone表外键'' AFTER nstepId;',
  'SELECT ''Column milestoneId already exists'' AS message;'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 幂等创建索引：如果不存在则创建
SET @idx_exists := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'construct_deliverableFiles' AND INDEX_NAME = 'idx_construct_deliverableFiles_milestone'
);
SET @sql := IF(@idx_exists = 0,
  'CREATE INDEX idx_construct_deliverableFiles_milestone ON construct_deliverableFiles(milestoneId);',
  'SELECT ''Index idx_construct_deliverableFiles_milestone already exists'' AS message;'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 幂等创建外键约束：如果不存在则创建
SET @fk_exists := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'construct_deliverableFiles' AND CONSTRAINT_NAME = 'fk_construct_deliverableFiles_milestone'
);
SET @sql := IF(@fk_exists = 0,
  'ALTER TABLE construct_deliverableFiles ADD CONSTRAINT fk_construct_deliverableFiles_milestone FOREIGN KEY (milestoneId) REFERENCES construct_milestone(milestoneId) ON UPDATE CASCADE ON DELETE SET NULL;',
  'SELECT ''Constraint fk_construct_deliverableFiles_milestone already exists'' AS message;'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 报告
SELECT 'Migration 057 executed: construct_deliverableFiles.milestoneId ensured' AS message;