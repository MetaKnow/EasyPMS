-- 为项目标准步骤关系表增加接口与个性化开发关联
-- 版本: 055
-- 描述: project_sstep_relations表增加interfaceId（interface表外键）、personalDevId（personal_develope表外键）

-- 新增列
ALTER TABLE project_sstep_relations 
ADD COLUMN interfaceId BIGINT COMMENT '接口ID（interface表外键）' AFTER stepStatus,
ADD COLUMN personalDevId BIGINT COMMENT '个性化开发ID（personal_develope表外键）' AFTER interfaceId;

-- 外键约束
ALTER TABLE project_sstep_relations 
ADD CONSTRAINT fk_project_sstep_relations_interface 
FOREIGN KEY (interfaceId) REFERENCES interface(interfaceId) 
ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE project_sstep_relations 
ADD CONSTRAINT fk_project_sstep_relations_personal_dev 
FOREIGN KEY (personalDevId) REFERENCES personal_develope(personalDevId) 
ON DELETE SET NULL ON UPDATE CASCADE;

-- 索引
CREATE INDEX idx_project_sstep_relations_interface ON project_sstep_relations(interfaceId);
CREATE INDEX idx_project_sstep_relations_personal_dev ON project_sstep_relations(personalDevId);