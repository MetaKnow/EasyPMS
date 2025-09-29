-- =============================================
-- Insert Default Organization Script
-- Version: 024
-- Create Time: 2024
-- Description: Insert default top-level organization "管软信息技术（北京）有限公司"
-- =============================================

-- Use database
USE `pms_db`;

-- Set character set for this session
SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;

-- =============================================
-- Insert Default Top-Level Organization
-- =============================================

-- Delete existing default organization if exists
DELETE FROM `organ` WHERE `organId` = 1;

-- Insert the default top-level organization with explicit character set
INSERT INTO `organ` (
    `organId`,
    `organName`,
    `parentOrganId`,
    `path`
) VALUES (
    1,
    _utf8mb4'管软信息技术（北京）有限公司' COLLATE utf8mb4_unicode_ci,
    NULL,
    '/1'
);

-- Get the organization ID for the default organization
SET @default_organ_id = (SELECT `organId` FROM `organ` WHERE `organName` = '管软信息技术（北京）有限公司' AND `parentOrganId` IS NULL LIMIT 1);

-- Update the path with the actual organId
UPDATE `organ` 
SET `path` = CONCAT('/', @default_organ_id)
WHERE `organId` = @default_organ_id;

-- Update admin user to belong to the default organization
UPDATE `user` 
SET `organId` = @default_organ_id 
WHERE `userName` = 'admin' AND `organId` IS NULL;

SELECT 'Default organization inserted successfully!' as message;
SELECT `organId`, `organName`, `parentOrganId`, `path` FROM `organ` WHERE `organId` = @default_organ_id;