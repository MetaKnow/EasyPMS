-- 迁移版本: 048
-- 描述: 为 constructing_project 表新增 constructContent 字段（VARCHAR(100)）
-- 创建时间: 2025-10-08

USE `pms_db`;

-- 新增项目建设内容简要字段
ALTER TABLE `constructing_project`
  ADD COLUMN `constructContent` VARCHAR(100) NULL COMMENT '项目建设内容（最多100字符）';

-- 完成提示
SELECT 'constructing_project 表已新增字段 constructContent(VARCHAR(100))' AS message;