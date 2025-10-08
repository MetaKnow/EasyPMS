-- 为非标准步骤表添加标准里程碑外键字段
-- 版本: 042
-- 描述: 为nonstandard_construct_step表添加smilestoneId字段（standard_milestone表外键）

-- 检查并添加smilestoneId字段（如果不存在）
SET @column_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'nonstandard_construct_step' 
    AND COLUMN_NAME = 'smilestoneId'
);

SET @sql = IF(@column_exists = 0, 
    'ALTER TABLE nonstandard_construct_step ADD COLUMN smilestoneId BIGINT COMMENT ''标准里程碑ID（外键）'' AFTER nstepName;', 
    'SELECT ''Column smilestoneId already exists'' AS message;'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并创建外键约束（如果不存在）
SET @constraint_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'nonstandard_construct_step' 
    AND CONSTRAINT_NAME = 'fk_nonstandard_step_milestone'
);

SET @sql = IF(@constraint_exists = 0, 
    'ALTER TABLE nonstandard_construct_step ADD CONSTRAINT fk_nonstandard_step_milestone FOREIGN KEY (smilestoneId) REFERENCES standard_milestone(milestone_id) ON DELETE SET NULL ON UPDATE CASCADE;', 
    'SELECT ''Constraint fk_nonstandard_step_milestone already exists'' AS message;'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并创建索引（如果不存在）
SET @index_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'nonstandard_construct_step' 
    AND INDEX_NAME = 'idx_nonstandard_step_milestone'
);

SET @sql = IF(@index_exists = 0, 
    'CREATE INDEX idx_nonstandard_step_milestone ON nonstandard_construct_step(smilestoneId);', 
    'SELECT ''Index idx_nonstandard_step_milestone already exists'' AS message;'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;