USE pms_db;

-- 修改 constructing_project_comment 表的 content 字段长度为 2000
ALTER TABLE constructing_project_comment MODIFY COLUMN content VARCHAR(2000) NOT NULL;
