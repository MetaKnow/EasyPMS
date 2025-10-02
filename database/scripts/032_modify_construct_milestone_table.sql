-- 修改里程碑表
-- 版本: 032
-- 描述: 修改construct_milestone表，增加projectId字段（constructing_project表外键）

-- 添加projectId字段
ALTER TABLE construct_milestone 
ADD COLUMN projectId BIGINT COMMENT '项目ID，constructing_project表外键' AFTER milestoneId;

-- 添加外键约束
ALTER TABLE construct_milestone 
ADD CONSTRAINT fk_construct_milestone_project 
FOREIGN KEY (projectId) REFERENCES constructing_project(projectId) 
ON DELETE CASCADE ON UPDATE CASCADE;

-- 创建索引
CREATE INDEX idx_construct_milestone_project ON construct_milestone(projectId);