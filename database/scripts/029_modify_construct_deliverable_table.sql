-- 修改交付物表
-- 版本: 029
-- 描述: 修改construct_deliverable表，重命名为construct_standard_deliverable，删除filePath、fileSize、uploadUser字段，增加isMustLoad、sstepId、milestoneId字段

-- 删除外键约束
ALTER TABLE construct_deliverable DROP FOREIGN KEY fk_construct_deliverable_upload_user;

-- 删除索引
DROP INDEX idx_construct_deliverable_upload_user ON construct_deliverable;
DROP INDEX idx_construct_deliverable_file_path ON construct_deliverable;

-- 删除字段
ALTER TABLE construct_deliverable 
DROP COLUMN filePath,
DROP COLUMN fileSize,
DROP COLUMN uploadUser;

-- 添加新字段
ALTER TABLE construct_deliverable 
ADD COLUMN isMustLoad TINYINT(1) DEFAULT 0 COMMENT '是否必须上传，0-非必须，1-必须' AFTER deliverableName,
ADD COLUMN sstepId BIGINT COMMENT '标准步骤ID，standard_construct_step表外键' AFTER isMustLoad,
ADD COLUMN milestoneId BIGINT COMMENT '里程碑ID，construct_milestone表外键' AFTER sstepId;

-- 重命名表
RENAME TABLE construct_deliverable TO construct_standard_deliverable;

-- 添加外键约束
ALTER TABLE construct_standard_deliverable 
ADD CONSTRAINT fk_construct_standard_deliverable_sstep 
FOREIGN KEY (sstepId) REFERENCES standard_construct_step(sstepId) 
ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE construct_standard_deliverable 
ADD CONSTRAINT fk_construct_standard_deliverable_milestone 
FOREIGN KEY (milestoneId) REFERENCES construct_milestone(milestoneId) 
ON DELETE SET NULL ON UPDATE CASCADE;

-- 创建新索引
CREATE INDEX idx_construct_standard_deliverable_must_load ON construct_standard_deliverable(isMustLoad);
CREATE INDEX idx_construct_standard_deliverable_sstep ON construct_standard_deliverable(sstepId);
CREATE INDEX idx_construct_standard_deliverable_milestone ON construct_standard_deliverable(milestoneId);