-- =============================================
-- Archive Software Table Creation Script
-- Version: 035
-- Create Time: 2024
-- Description: Create archieveSoft table for software archive management
-- =============================================

-- Use database
USE `pms_db`;

-- =============================================
-- Archive Software Table
-- =============================================
CREATE TABLE IF NOT EXISTS `archieveSoft` (
    `softId` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Software ID',
    `softName` VARCHAR(200) NOT NULL COMMENT 'Software Name',
    `softVersion` VARCHAR(50) NOT NULL COMMENT 'Software Version',
    INDEX `idx_soft_name` (`softName`),
    INDEX `idx_soft_version` (`softVersion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Archive Software Table';

SELECT 'Archive Software Table Created Successfully!' as message;