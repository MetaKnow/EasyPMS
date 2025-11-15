-- =============================================
-- 迁移：construct_deliverableFiles.sstepId → projectStepId（外键改为 project_sstep_relations）
-- 版本: 058
-- 目的：将交付物文件表中原指向 standard_construct_step 的 sstepId 改为指向
--       project_sstep_relations 的 projectStepId（relationId 主键），并更新索引与外键约束。
-- 范围：仅进行表结构变更，不插入或更新任何业务数据。
-- =============================================

-- 1) 幂等删除旧外键（fk_construct_deliverableFiles_sstep）
SET @fk_old_exists := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'construct_deliverableFiles' AND CONSTRAINT_NAME = 'fk_construct_deliverableFiles_sstep'
);
SET @sql := IF(@fk_old_exists > 0,
  'ALTER TABLE construct_deliverableFiles DROP FOREIGN KEY fk_construct_deliverableFiles_sstep;',
  'SELECT ''Old FK fk_construct_deliverableFiles_sstep not exists'' AS message;'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 2) 幂等删除旧索引（idx_construct_deliverableFiles_sstep）
SET @idx_old_exists := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'construct_deliverableFiles' AND INDEX_NAME = 'idx_construct_deliverableFiles_sstep'
);
SET @sql := IF(@idx_old_exists > 0,
  'DROP INDEX idx_construct_deliverableFiles_sstep ON construct_deliverableFiles;',
  'SELECT ''Old index idx_construct_deliverableFiles_sstep not exists'' AS message;'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 3) 幂等重命名列：sstepId → projectStepId（如 sstepId 存在且 projectStepId 不存在）
SET @has_sstep := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'construct_deliverableFiles' AND COLUMN_NAME = 'sstepId'
);
SET @has_projectStep := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'construct_deliverableFiles' AND COLUMN_NAME = 'projectStepId'
);
SET @sql := IF(@has_projectStep = 0 AND @has_sstep > 0,
  'ALTER TABLE construct_deliverableFiles CHANGE COLUMN sstepId projectStepId BIGINT NULL COMMENT ''项目步骤关系ID，project_sstep_relations表外键'';',
  'SELECT ''Column rename skipped (already renamed or missing)'' AS message;'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 4) 幂等创建新索引（idx_construct_deliverableFiles_projectStep）
SET @idx_new_exists := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'construct_deliverableFiles' AND INDEX_NAME = 'idx_construct_deliverableFiles_projectStep'
);
SET @sql := IF(@idx_new_exists = 0,
  'CREATE INDEX idx_construct_deliverableFiles_projectStep ON construct_deliverableFiles(projectStepId);',
  'SELECT ''Index idx_construct_deliverableFiles_projectStep already exists'' AS message;'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 5) 幂等创建新外键约束（fk_construct_deliverableFiles_projectStep）指向 project_sstep_relations(relationId)
SET @fk_new_exists := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'construct_deliverableFiles' AND CONSTRAINT_NAME = 'fk_construct_deliverableFiles_projectStep'
);
SET @sql := IF(@fk_new_exists = 0,
  'ALTER TABLE construct_deliverableFiles ADD CONSTRAINT fk_construct_deliverableFiles_projectStep FOREIGN KEY (projectStepId) REFERENCES project_sstep_relations(relationId) ON DELETE SET NULL ON UPDATE CASCADE;',
  'SELECT ''Constraint fk_construct_deliverableFiles_projectStep already exists'' AS message;'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 报告
SELECT 'Migration 058 executed: sstepId → projectStepId with FK to project_sstep_relations(relationId)' AS message;