-- =============================================
-- Add Timestamps to Missing Tables Script
-- Version: 038
-- Create Time: 2024
-- Description: Add createTime and updateTime fields to tables that are missing them
-- =============================================

-- Use database
USE `pms_db`;

-- =============================================
-- Add timestamps to role table
-- =============================================
ALTER TABLE `role` 
ADD COLUMN `createTime` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
ADD COLUMN `updateTime` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- =============================================
-- Add timestamps to organ table
-- =============================================
ALTER TABLE `organ` 
ADD COLUMN `createTime` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
ADD COLUMN `updateTime` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- =============================================
-- Add timestamps to user table
-- =============================================
ALTER TABLE `user` 
ADD COLUMN `createTime` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
ADD COLUMN `updateTime` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';





-- =============================================
-- Create indexes for the new timestamp columns
-- =============================================
CREATE INDEX `idx_role_create_time` ON `role`(`createTime`);
CREATE INDEX `idx_role_update_time` ON `role`(`updateTime`);

CREATE INDEX `idx_organ_create_time` ON `organ`(`createTime`);
CREATE INDEX `idx_organ_update_time` ON `organ`(`updateTime`);

CREATE INDEX `idx_user_create_time` ON `user`(`createTime`);
CREATE INDEX `idx_user_update_time` ON `user`(`updateTime`);

SELECT 'Timestamps added to all missing tables successfully!' as message;