-- =============================================
-- Migration 026: Insert default roles
-- Description: Insert default system roles for the application
-- =============================================

USE `pms_db`;

-- Insert default roles (using INSERT IGNORE to avoid duplicates)
INSERT IGNORE INTO `role` (`roleName`, `description`) VALUES 
('销售角色', '销售人员角色，负责客户开发、项目销售等业务'),
('售后角色', '售后服务人员角色，负责项目维护、客户服务等工作'),
('项目经理角色', '项目经理角色，负责项目的规划、执行和管理'),
('销售总监角色', '销售总监角色，负责销售团队管理和销售策略制定'),
('项目总监角色', '项目总监角色，负责项目团队管理和项目战略规划'),
('公司领导角色', '公司领导角色，拥有公司级别的决策和管理权限');

-- Update existing admin role to have description if it exists
UPDATE `role` SET `description` = '管理员角色，拥有系统管理权限' 
WHERE `roleName` = '管理员' AND (`description` IS NULL OR `description` = '');

-- Report
SELECT 'Migration 026 executed: Default roles inserted' AS message;