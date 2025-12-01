-- 迁移版本: 067
-- 描述: 为constructing_project表添加isCommitAfterSale字段
-- 创建时间: 2025-11-30

USE pms_db;

-- 添加 isCommitAfterSale 字段
ALTER TABLE constructing_project
ADD COLUMN isCommitAfterSale TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否移交运维（0-未移交，1-已移交）';
