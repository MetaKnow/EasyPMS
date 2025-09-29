-- =============================================
-- Migration 025: Add description column to role table
-- Description: Align database schema with Role entity by adding optional description field
-- =============================================

USE `pms_db`;

-- Idempotent add column if not exists
SET @col_exists := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'role' AND COLUMN_NAME = 'description'
);
SET @ddl := IF(@col_exists = 0,
  'ALTER TABLE `role` ADD COLUMN `description` TEXT NULL COMMENT ''Role Description''',
  'SELECT 0 FROM DUAL'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Report
SELECT 'Migration 025 executed: role.description ensured' AS message;