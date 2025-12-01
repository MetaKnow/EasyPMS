-- 迁移版本: 066
-- 描述: 在 afterService_project 表中添加 constructingProjectId 字段，关联 constructing_project 表
-- 创建时间: 2024-02-23

USE pms_db;

-- 添加 constructingProjectId 字段
ALTER TABLE afterService_project
ADD COLUMN constructingProjectId BIGINT COMMENT '关联的在建项目ID，外键';

-- 添加外键约束
ALTER TABLE afterService_project
ADD CONSTRAINT fk_afterservice_constructing_project
FOREIGN KEY (constructingProjectId) REFERENCES constructing_project(projectId)
ON DELETE SET NULL ON UPDATE CASCADE;

-- 创建索引
CREATE INDEX idx_afterservice_constructing_project ON afterService_project(constructingProjectId);

SELECT 'afterService_project 表添加 constructingProjectId 字段成功' AS message;
