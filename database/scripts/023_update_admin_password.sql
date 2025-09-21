-- =============================================
-- Update Admin Password Script
-- Version: 023
-- Create Time: 2025-09-21
-- Description: Update admin user password to 'admin123'
-- =============================================

-- Use database
USE `pms_db`;

-- =============================================
-- Update Admin User Password
-- =============================================

-- Update admin user password to 'admin123'
-- Using BCrypt hash of 'admin123' = '$2a$10$83xFBa.d70Dr2M0xUnUD0OAZNweDHCs.WoKqgcWytnjrUWQFiiv.i'
UPDATE `user` 
SET `password` = '$2a$10$83xFBa.d70Dr2M0xUnUD0OAZNweDHCs.WoKqgcWytnjrUWQFiiv.i'
WHERE `userName` = 'admin';

-- Verify the update
SELECT 
    `userId`,
    `userName`,
    `password`,
    `name`,
    `locked`
FROM `user` 
WHERE `userName` = 'admin';

SELECT 'Admin password updated successfully! New password is: admin123' as message;