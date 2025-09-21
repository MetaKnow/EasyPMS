-- =============================================
-- 软件项目管理系统 - 数据库初始化脚本
-- 创建时间: 2024
-- 描述: 创建PMS数据库和基础表结构
-- =============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `pms_db` 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci
COMMENT '软件项目管理系统数据库';

-- 使用数据库
USE `pms_db`;

-- =============================================
-- 用户相关表
-- =============================================

-- 用户表
CREATE TABLE IF NOT EXISTS `users` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `email` VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（加密）',
    `real_name` VARCHAR(100) COMMENT '真实姓名',
    `phone` VARCHAR(20) COMMENT '电话号码',
    `avatar` VARCHAR(255) COMMENT '头像URL',
    `status` TINYINT DEFAULT 1 COMMENT '状态：1-正常，0-禁用',
    `role` VARCHAR(20) DEFAULT 'USER' COMMENT '角色：ADMIN-管理员，PM-项目经理，DEV-开发者，TESTER-测试员，USER-普通用户',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_username` (`username`),
    INDEX `idx_email` (`email`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =============================================
-- 项目相关表
-- =============================================

-- 项目表
CREATE TABLE IF NOT EXISTS `projects` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '项目ID',
    `name` VARCHAR(200) NOT NULL COMMENT '项目名称',
    `description` TEXT COMMENT '项目描述',
    `status` VARCHAR(20) DEFAULT 'PLANNING' COMMENT '项目状态：PLANNING-规划中，ACTIVE-进行中，COMPLETED-已完成，CANCELLED-已取消',
    `priority` VARCHAR(10) DEFAULT 'MEDIUM' COMMENT '优先级：HIGH-高，MEDIUM-中，LOW-低',
    `start_date` DATE COMMENT '开始日期',
    `end_date` DATE COMMENT '结束日期',
    `budget` DECIMAL(15,2) COMMENT '预算',
    `progress` TINYINT DEFAULT 0 COMMENT '进度百分比（0-100）',
    `manager_id` BIGINT COMMENT '项目经理ID',
    `created_by` BIGINT NOT NULL COMMENT '创建人ID',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_status` (`status`),
    INDEX `idx_manager` (`manager_id`),
    INDEX `idx_created_by` (`created_by`),
    FOREIGN KEY (`manager_id`) REFERENCES `users`(`id`) ON DELETE SET NULL,
    FOREIGN KEY (`created_by`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目表';

-- 项目成员表
CREATE TABLE IF NOT EXISTS `project_members` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    `project_id` BIGINT NOT NULL COMMENT '项目ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role` VARCHAR(20) DEFAULT 'MEMBER' COMMENT '项目角色：MANAGER-经理，DEVELOPER-开发者，TESTER-测试员，MEMBER-成员',
    `joined_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    UNIQUE KEY `uk_project_user` (`project_id`, `user_id`),
    INDEX `idx_project` (`project_id`),
    INDEX `idx_user` (`user_id`),
    FOREIGN KEY (`project_id`) REFERENCES `projects`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目成员表';

-- =============================================
-- 任务相关表
-- =============================================

-- 任务表
CREATE TABLE IF NOT EXISTS `tasks` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '任务ID',
    `project_id` BIGINT NOT NULL COMMENT '项目ID',
    `title` VARCHAR(200) NOT NULL COMMENT '任务标题',
    `description` TEXT COMMENT '任务描述',
    `status` VARCHAR(20) DEFAULT 'TODO' COMMENT '任务状态：TODO-待办，IN_PROGRESS-进行中，TESTING-测试中，DONE-已完成',
    `priority` VARCHAR(10) DEFAULT 'MEDIUM' COMMENT '优先级：HIGH-高，MEDIUM-中，LOW-低',
    `type` VARCHAR(20) DEFAULT 'FEATURE' COMMENT '任务类型：FEATURE-功能，BUG-缺陷，IMPROVEMENT-改进，TASK-任务',
    `assignee_id` BIGINT COMMENT '负责人ID',
    `reporter_id` BIGINT NOT NULL COMMENT '报告人ID',
    `estimated_hours` DECIMAL(5,2) COMMENT '预估工时',
    `actual_hours` DECIMAL(5,2) COMMENT '实际工时',
    `start_date` DATE COMMENT '开始日期',
    `due_date` DATE COMMENT '截止日期',
    `completed_at` TIMESTAMP NULL COMMENT '完成时间',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_project` (`project_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_assignee` (`assignee_id`),
    INDEX `idx_reporter` (`reporter_id`),
    INDEX `idx_due_date` (`due_date`),
    FOREIGN KEY (`project_id`) REFERENCES `projects`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`assignee_id`) REFERENCES `users`(`id`) ON DELETE SET NULL,
    FOREIGN KEY (`reporter_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务表';

-- =============================================
-- 系统配置表
-- =============================================

-- 系统配置表
CREATE TABLE IF NOT EXISTS `system_config` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    `config_key` VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    `config_value` TEXT COMMENT '配置值',
    `description` VARCHAR(255) COMMENT '配置描述',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- =============================================
-- 插入初始数据
-- =============================================

-- 插入默认管理员用户（密码: admin123，需要在应用中加密）
INSERT IGNORE INTO `users` (`username`, `email`, `password`, `real_name`, `role`, `status`) VALUES 
('admin', 'admin@pms.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iIdeal9sM6dM9BF6.1qHOjJy.Oy6', '系统管理员', 'ADMIN', 1);

-- 插入系统配置
INSERT IGNORE INTO `system_config` (`config_key`, `config_value`, `description`) VALUES 
('system.name', '软件项目管理系统', '系统名称'),
('system.version', '1.0.0', '系统版本'),
('system.company', 'Missoft', '公司名称'),
('file.upload.path', '/uploads/', '文件上传路径'),
('file.upload.max.size', '10485760', '文件上传最大大小（字节）');

-- =============================================
-- 创建视图（可选）
-- =============================================

-- 项目概览视图
CREATE OR REPLACE VIEW `project_overview` AS
SELECT 
    p.id,
    p.name,
    p.status,
    p.priority,
    p.progress,
    p.start_date,
    p.end_date,
    u.real_name as manager_name,
    COUNT(DISTINCT pm.user_id) as member_count,
    COUNT(DISTINCT t.id) as task_count,
    COUNT(DISTINCT CASE WHEN t.status = 'DONE' THEN t.id END) as completed_tasks
FROM projects p
LEFT JOIN users u ON p.manager_id = u.id
LEFT JOIN project_members pm ON p.id = pm.project_id
LEFT JOIN tasks t ON p.id = t.project_id
GROUP BY p.id, p.name, p.status, p.priority, p.progress, p.start_date, p.end_date, u.real_name;

SELECT '✅ 数据库初始化完成!' as message;