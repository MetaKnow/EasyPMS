-- =============================================
-- Database Migration Management Table
-- Version: 001
-- Description: Create table to track executed migration scripts
-- =============================================

-- Use database
USE `pms_db`;

-- =============================================
-- Migration History Table
-- =============================================
CREATE TABLE IF NOT EXISTS `migration_history` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Migration ID',
    `version` VARCHAR(50) NOT NULL UNIQUE COMMENT 'Migration Version (e.g., 001, 002)',
    `script_name` VARCHAR(255) NOT NULL COMMENT 'Script File Name',
    `description` TEXT COMMENT 'Migration Description',
    `checksum` VARCHAR(64) COMMENT 'Script Content Checksum (MD5)',
    `executed_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Execution Time',
    `execution_time_ms` BIGINT COMMENT 'Execution Time in Milliseconds',
    `success` BOOLEAN DEFAULT TRUE COMMENT 'Execution Success Status',
    `error_message` TEXT COMMENT 'Error Message if Failed',
    INDEX `idx_version` (`version`),
    INDEX `idx_executed_at` (`executed_at`),
    INDEX `idx_success` (`success`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Database Migration History Table';

-- Insert initial migration record for this script
INSERT IGNORE INTO `migration_history` (`version`, `script_name`, `description`, `success`) VALUES 
('001', '001_create_migration_table.sql', 'Create migration history table for database version management', TRUE);

SELECT 'Migration history table created successfully!' as message;