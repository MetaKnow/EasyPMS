-- 修改项目步骤关系表
-- 版本: 028
-- 描述: 修改project_relations表，重命名为project_sstep_relations，删除milestoneId、nstepId字段，增加director、planStartDate、planEndDate、actualStartDate、actualEndDate、planPeriod、actualPeriod字段

-- 删除外键约束
ALTER TABLE project_relations DROP FOREIGN KEY fk_project_relations_milestone;
ALTER TABLE project_relations DROP FOREIGN KEY fk_project_relations_nstep;

-- 删除索引
DROP INDEX idx_project_relations_milestone ON project_relations;
DROP INDEX idx_project_relations_nstep ON project_relations;

-- 删除字段
ALTER TABLE project_relations 
DROP COLUMN milestoneId,
DROP COLUMN nstepId;

-- 添加新字段
ALTER TABLE project_relations 
ADD COLUMN director BIGINT COMMENT '负责人，user表外键' AFTER deliverableId,
ADD COLUMN planStartDate DATE COMMENT '计划开始日期' AFTER director,
ADD COLUMN planEndDate DATE COMMENT '计划结束日期' AFTER planStartDate,
ADD COLUMN actualStartDate DATE COMMENT '实际开始日期' AFTER planEndDate,
ADD COLUMN actualEndDate DATE COMMENT '实际结束日期' AFTER actualStartDate,
ADD COLUMN planPeriod INT COMMENT '计划工期（天数）' AFTER actualEndDate,
ADD COLUMN actualPeriod INT COMMENT '实际工期（天数）' AFTER planPeriod;

-- 重命名表
RENAME TABLE project_relations TO project_sstep_relations;

-- 添加外键约束
ALTER TABLE project_sstep_relations 
ADD CONSTRAINT fk_project_sstep_relations_director 
FOREIGN KEY (director) REFERENCES user(userId) 
ON DELETE SET NULL ON UPDATE CASCADE;

-- 创建新索引
CREATE INDEX idx_project_sstep_relations_director ON project_sstep_relations(director);
CREATE INDEX idx_project_sstep_relations_plan_start ON project_sstep_relations(planStartDate);
CREATE INDEX idx_project_sstep_relations_plan_end ON project_sstep_relations(planEndDate);