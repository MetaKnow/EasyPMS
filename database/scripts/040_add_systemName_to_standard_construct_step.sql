-- 为标准步骤表添加档案系统名称字段
-- 版本: 040
-- 描述: 为standard_construct_step表添加systemName字段（档案系统名称）

-- 添加systemName字段
ALTER TABLE standard_construct_step 
ADD COLUMN systemName VARCHAR(100) COMMENT '档案系统名称' AFTER type;

-- 创建索引以提高查询性能
CREATE INDEX idx_standard_step_system_name ON standard_construct_step(systemName);