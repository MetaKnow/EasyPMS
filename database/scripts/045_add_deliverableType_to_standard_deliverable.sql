-- 为标准交付物表添加交付物类型字段
-- 版本: 045
-- 描述: 为standard_deliverable表添加deliverableType字段

-- 添加deliverableType字段
ALTER TABLE standard_deliverable 
ADD COLUMN deliverableType VARCHAR(50) COMMENT '交付物类型' AFTER systemName;

-- 创建deliverableType字段的索引
CREATE INDEX idx_standard_deliverable_type ON standard_deliverable(deliverableType);