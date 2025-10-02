-- 修改标准步骤表
-- 版本: 027
-- 描述: 修改standard_construct_step表，删除director、planStartDate、planEndDate、actualStartDate、actualEndDate、planPeriod、actualPeriod、afterServiceProjectId字段，增加type字段

-- 删除外键约束
ALTER TABLE standard_construct_step DROP FOREIGN KEY fk_standard_step_director;
ALTER TABLE standard_construct_step DROP FOREIGN KEY fk_standard_step_afterservice;

-- 删除索引
DROP INDEX idx_standard_step_director ON standard_construct_step;
DROP INDEX idx_standard_step_plan_start ON standard_construct_step;
DROP INDEX idx_standard_step_plan_end ON standard_construct_step;
DROP INDEX idx_standard_step_afterservice ON standard_construct_step;

-- 删除字段
ALTER TABLE standard_construct_step 
DROP COLUMN director,
DROP COLUMN planStartDate,
DROP COLUMN planEndDate,
DROP COLUMN actualStartDate,
DROP COLUMN actualEndDate,
DROP COLUMN planPeriod,
DROP COLUMN actualPeriod,
DROP COLUMN afterServiceProjectId;

-- 添加type字段
ALTER TABLE standard_construct_step 
ADD COLUMN type VARCHAR(50) COMMENT '步骤类型' AFTER sstepName;

-- 创建新索引
CREATE INDEX idx_standard_step_type ON standard_construct_step(type);