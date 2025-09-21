-- 创建项目步骤关系表
-- 版本: 020
-- 描述: 创建项目步骤关系表，关联项目与各种步骤、里程碑和交付物

CREATE TABLE project_relations (
    relationId BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '关系ID，主键',
    projectId BIGINT COMMENT '项目ID，constructing_project表外键',
    sstepId BIGINT COMMENT '标准步骤ID，standard_construct_step表外键',
    milestoneId BIGINT COMMENT '里程碑ID，construct_milestone表外键',
    nstepId BIGINT COMMENT '非标步骤ID，nonstandard_construct_step表外键',
    deliverableId BIGINT COMMENT '交付物ID，construct_deliverable表外键',
    createTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updateTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目步骤关系表';

-- 添加外键约束
ALTER TABLE project_relations 
ADD CONSTRAINT fk_project_relations_project 
FOREIGN KEY (projectId) REFERENCES constructing_project(projectId) 
ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE project_relations 
ADD CONSTRAINT fk_project_relations_sstep 
FOREIGN KEY (sstepId) REFERENCES standard_construct_step(sstepId) 
ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE project_relations 
ADD CONSTRAINT fk_project_relations_milestone 
FOREIGN KEY (milestoneId) REFERENCES construct_milestone(milestoneId) 
ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE project_relations 
ADD CONSTRAINT fk_project_relations_nstep 
FOREIGN KEY (nstepId) REFERENCES nonstandard_construct_step(nstepId) 
ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE project_relations 
ADD CONSTRAINT fk_project_relations_deliverable 
FOREIGN KEY (deliverableId) REFERENCES construct_deliverable(deliverableId) 
ON DELETE SET NULL ON UPDATE CASCADE;

-- 创建索引
CREATE INDEX idx_project_relations_project ON project_relations(projectId);
CREATE INDEX idx_project_relations_sstep ON project_relations(sstepId);
CREATE INDEX idx_project_relations_milestone ON project_relations(milestoneId);
CREATE INDEX idx_project_relations_nstep ON project_relations(nstepId);
CREATE INDEX idx_project_relations_deliverable ON project_relations(deliverableId);