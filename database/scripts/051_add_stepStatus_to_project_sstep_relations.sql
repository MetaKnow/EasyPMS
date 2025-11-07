-- 为项目标准步骤关系表添加步骤状态字段
-- 版本: 051
-- 描述: 为project_sstep_relations表添加stepStatus字段，用于记录步骤状态（未开始/进行中/已完成）

-- 添加stepStatus字段
ALTER TABLE project_sstep_relations 
ADD COLUMN stepStatus VARCHAR(20) COMMENT '步骤状态（未开始/进行中/已完成）' AFTER actualPeriod;