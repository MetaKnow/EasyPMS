-- 为项目标准步骤关系表设置步骤状态默认值并回填
-- 版本: 052
-- 描述: 将project_sstep_relations.stepStatus设置默认值为'未开始'，并为现有空值记录回填

-- 设置列默认值（保持列位置与注释一致）
ALTER TABLE project_sstep_relations
MODIFY COLUMN stepStatus VARCHAR(20) DEFAULT '未开始' COMMENT '步骤状态（未开始/进行中/已完成）' AFTER actualPeriod;

-- 回填现有记录中的空值
UPDATE project_sstep_relations
SET stepStatus = '未开始'
WHERE stepStatus IS NULL OR stepStatus = '';

SELECT 'Default stepStatus set to 未开始 and existing nulls backfilled.' AS message;