USE pms_db;

SET @create_exists := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'customer_follow_up_deliverableFiles'
    AND COLUMN_NAME = 'createTime'
);
SET @sql := IF(@create_exists = 0,
  'ALTER TABLE `customer_follow_up_deliverableFiles` ADD COLUMN `createTime` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT ''创建时间''',
  'SELECT ''Column createTime already exists'' AS message'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @update_exists := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'customer_follow_up_deliverableFiles'
    AND COLUMN_NAME = 'updateTime'
);
SET @sql := IF(@update_exists = 0,
  'ALTER TABLE `customer_follow_up_deliverableFiles` ADD COLUMN `updateTime` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''更新时间''',
  'SELECT ''Column updateTime already exists'' AS message'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SELECT 'Migration 078 executed: customer_follow_up_deliverableFiles timestamps added' AS message;
