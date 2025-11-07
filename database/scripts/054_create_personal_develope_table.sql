-- 创建个性化开发表
-- 版本: 054
-- 描述: 创建personal_develope表，包含personalDevId（主键）、personalDevName、projectId（constructing_project外键）、milestoneId（construct_milestone外键）

CREATE TABLE personal_develope (
    personalDevId BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '个性化开发ID（主键）',
    personalDevName VARCHAR(200) NOT NULL COMMENT '个性化开发名称',
    projectId BIGINT COMMENT '项目ID（constructing_project外键）',
    milestoneId BIGINT COMMENT '里程碑ID（construct_milestone外键）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='个性化开发表';

-- 外键约束
ALTER TABLE personal_develope 
ADD CONSTRAINT fk_personal_develope_project 
FOREIGN KEY (projectId) REFERENCES constructing_project(projectId) 
ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE personal_develope 
ADD CONSTRAINT fk_personal_develope_milestone 
FOREIGN KEY (milestoneId) REFERENCES construct_milestone(milestoneId) 
ON DELETE SET NULL ON UPDATE CASCADE;

-- 索引
CREATE INDEX idx_personal_develope_project ON personal_develope(projectId);
CREATE INDEX idx_personal_develope_milestone ON personal_develope(milestoneId);
CREATE INDEX idx_personal_develope_name ON personal_develope(personalDevName);