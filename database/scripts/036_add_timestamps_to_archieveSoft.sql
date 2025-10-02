-- =============================================
-- Add Timestamps to Archive Software Table
-- Version: 036
-- Create Time: 2024
-- Description: Add createTime and updateTime columns to archieveSoft table
-- =============================================

-- Use database
USE `pms_db`;

-- =============================================
-- Add timestamp columns to archieveSoft table
-- =============================================

-- Add createTime column
ALTER TABLE `archieveSoft` 
ADD COLUMN `createTime` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation Time';

-- Add updateTime column  
ALTER TABLE `archieveSoft` 
ADD COLUMN `updateTime` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time';

-- Add index for createTime (useful for sorting)
ALTER TABLE `archieveSoft` 
ADD INDEX `idx_create_time` (`createTime`);

SELECT 'Timestamp columns added to archieveSoft table successfully!' as message;