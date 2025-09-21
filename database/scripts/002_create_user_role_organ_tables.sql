-- =============================================
-- User Role Organization Tables Creation Script
-- Version: 002
-- Create Time: 2024
-- Description: Create user, role, organ tables with foreign key relationships
-- =============================================

-- Use database
USE `pms_db`;

-- =============================================
-- Role Table
-- =============================================
CREATE TABLE IF NOT EXISTS `role` (
    `roleId` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Role ID',
    `roleName` VARCHAR(100) NOT NULL COMMENT 'Role Name',
    INDEX `idx_role_name` (`roleName`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Role Table';

-- =============================================
-- Organization Table
-- =============================================
CREATE TABLE IF NOT EXISTS `organ` (
    `organId` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Organization ID',
    `organName` VARCHAR(200) NOT NULL COMMENT 'Organization Name',
    `parentOrganId` BIGINT COMMENT 'Parent Organization ID',
    `path` VARCHAR(500) COMMENT 'Organization Path',
    INDEX `idx_organ_name` (`organName`),
    INDEX `idx_parent_organ` (`parentOrganId`),
    FOREIGN KEY (`parentOrganId`) REFERENCES `organ`(`organId`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Organization Table';

-- =============================================
-- User Table
-- =============================================
CREATE TABLE IF NOT EXISTS `user` (
    `userId` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'User ID',
    `userName` VARCHAR(50) NOT NULL UNIQUE COMMENT 'Username',
    `password` VARCHAR(255) NOT NULL COMMENT 'Password',
    `organId` BIGINT COMMENT 'Organization ID',
    `roleId` BIGINT COMMENT 'Role ID',
    `name` VARCHAR(100) COMMENT 'Real Name',
    `locked` TINYINT DEFAULT 0 COMMENT 'Lock Status: 0-Normal, 1-Locked',
    INDEX `idx_user_name` (`userName`),
    INDEX `idx_organ_id` (`organId`),
    INDEX `idx_role_id` (`roleId`),
    FOREIGN KEY (`organId`) REFERENCES `organ`(`organId`) ON DELETE SET NULL,
    FOREIGN KEY (`roleId`) REFERENCES `role`(`roleId`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User Table';

SELECT 'User Role Organization Tables Created Successfully!' as message;