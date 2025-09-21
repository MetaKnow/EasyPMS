-- =============================================
-- Cleanup User Table Extra Columns Script
-- Version: 022
-- Create Time: 2025-09-21
-- Description: Remove extra columns that were added by Hibernate auto-ddl
-- =============================================

-- Use database
USE `pms_db`;

-- =============================================
-- Check and remove extra columns from user table
-- =============================================

-- Check if organ_id column exists and drop it (this is duplicate of organId)
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = 'pms_db' 
     AND TABLE_NAME = 'user' 
     AND COLUMN_NAME = 'organ_id') > 0,
    'ALTER TABLE `user` DROP COLUMN `organ_id`',
    'SELECT "Column organ_id does not exist" as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Check if role_id column exists and drop it (this is duplicate of roleId)
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = 'pms_db' 
     AND TABLE_NAME = 'user' 
     AND COLUMN_NAME = 'role_id') > 0,
    'ALTER TABLE `user` DROP COLUMN `role_id`',
    'SELECT "Column role_id does not exist" as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Check if user_name column exists and drop it (this is duplicate of userName)
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = 'pms_db' 
     AND TABLE_NAME = 'user' 
     AND COLUMN_NAME = 'user_name') > 0,
    'ALTER TABLE `user` DROP COLUMN `user_name`',
    'SELECT "Column user_name does not exist" as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Verify the final table structure
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'pms_db' 
AND TABLE_NAME = 'user'
ORDER BY ORDINAL_POSITION;

SELECT 'User table extra columns cleanup completed!' as message;