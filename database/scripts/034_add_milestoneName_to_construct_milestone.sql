-- 为construct_milestone表添加milestoneName字段
-- 版本: 034
-- 描述: 为construct_milestone表添加milestoneName字段，必填

-- 添加milestoneName字段
ALTER TABLE construct_milestone 
ADD COLUMN milestoneName VARCHAR(200) NOT NULL COMMENT '里程碑名称，必填' AFTER milestoneId;

-- 创建索引
CREATE INDEX idx_construct_milestone_name ON construct_milestone(milestoneName);