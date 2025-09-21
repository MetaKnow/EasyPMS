-- =============================================
-- Insert Admin Default User Script
-- Version: 021
-- Create Time: 2024
-- Description: Insert admin default user with initial password 'admin'
-- =============================================

-- Use database
USE `pms_db`;

-- =============================================
-- Insert Default Admin User
-- =============================================

-- First, insert a default admin role if it doesn't exist
INSERT IGNORE INTO `role` (`roleName`) VALUES ('管理员');

-- Get the role ID for admin role
SET @admin_role_id = (SELECT `roleId` FROM `role` WHERE `roleName` = '管理员' LIMIT 1);

-- Insert admin user with password 'admin'
-- Using BCrypt hash of 'admin' = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9P8UE5Ey.Ew6V4W'
-- This is a secure BCrypt hash that should be used in production
INSERT IGNORE INTO `user` (
    `userName`,
    `password`,
    `roleId`,
    `name`,
    `locked`
) VALUES (
    'admin',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9P8UE5Ey.Ew6V4W',
    @admin_role_id,
    '系统管理员',
    0
);

SELECT 'Admin default user inserted successfully!' as message;