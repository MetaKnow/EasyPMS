-- 修复标准交付物表的里程碑外键约束
-- 版本: 046
-- 描述: 修复standard_deliverable表的milestoneId外键约束，使其指向standard_milestone表而不是construct_milestone表

-- 删除现有的外键约束
ALTER TABLE standard_deliverable 
DROP FOREIGN KEY fk_construct_standard_deliverable_milestone;

-- 删除现有的索引
DROP INDEX idx_construct_standard_deliverable_milestone ON standard_deliverable;

-- 添加新的外键约束，指向standard_milestone表
ALTER TABLE standard_deliverable 
ADD CONSTRAINT fk_standard_deliverable_standard_milestone 
FOREIGN KEY (milestoneId) REFERENCES standard_milestone(milestone_id) 
ON DELETE SET NULL ON UPDATE CASCADE;

-- 创建新的索引
CREATE INDEX idx_standard_deliverable_milestone ON standard_deliverable(milestoneId);