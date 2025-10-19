-- =============================================
-- Add softType to archieveSoft Table
-- Version: 049
-- Create Time: 2025
-- Description: Add 产品类型 (softType) field to archieveSoft (base product module)
-- =============================================

-- Use database
USE `pms_db`;

-- =============================================
-- Add softType column to archieveSoft
-- =============================================
ALTER TABLE `archieveSoft`
  ADD COLUMN `softType` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '产品类型' AFTER `softVersion`;

-- Create index on softType
CREATE INDEX `idx_soft_type` ON `archieveSoft`(`softType`);

-- Done message
SELECT 'softType column added to archieveSoft successfully!' AS message;