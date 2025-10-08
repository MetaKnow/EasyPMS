-- 重命名标准交付物表并添加系统名称字段
-- 版本: 044
-- 描述: 将construct_standard_deliverable表重命名为standard_deliverable，并添加systemName字段

-- 添加systemName字段
ALTER TABLE construct_standard_deliverable 
ADD COLUMN systemName VARCHAR(100) COMMENT '系统名称' AFTER deliverableName;

-- 重命名表
RENAME TABLE construct_standard_deliverable TO standard_deliverable;

-- 创建systemName字段的索引
CREATE INDEX idx_standard_deliverable_system_name ON standard_deliverable(systemName);