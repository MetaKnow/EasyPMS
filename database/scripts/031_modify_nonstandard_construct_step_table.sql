-- 修改非标准步骤表
-- 版本: 031
-- 描述: 修改nonstandard_construct_step表，增加projectId字段（constructing_project表外键）

-- 添加projectId字段
ALTER TABLE nonstandard_construct_step 
ADD COLUMN projectId BIGINT COMMENT '项目ID，constructing_project表外键' AFTER nstepName;

-- 添加外键约束
ALTER TABLE nonstandard_construct_step 
ADD CONSTRAINT fk_nonstandard_step_project 
FOREIGN KEY (projectId) REFERENCES constructing_project(projectId) 
ON DELETE CASCADE ON UPDATE CASCADE;

-- 创建索引
CREATE INDEX idx_nonstandard_step_project ON nonstandard_construct_step(projectId);