-- 迁移版本: 010
-- 描述: 为constructing_project表添加新字段：isAgent、agentName、value、receivedMoney、unreceiveMoney、acceptanceDate
-- 创建时间: 2024-01-20

USE pms_db;

-- 添加新字段到constructing_project表
ALTER TABLE constructing_project 
ADD COLUMN isAgent TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否渠道项目（0-否，1-是）';

ALTER TABLE constructing_project 
ADD COLUMN agentName VARCHAR(100) NULL COMMENT '渠道名称';

ALTER TABLE constructing_project 
ADD COLUMN value DECIMAL(15,2) NULL COMMENT '项目金额';

ALTER TABLE constructing_project 
ADD COLUMN receivedMoney DECIMAL(15,2) NOT NULL DEFAULT 0.00 COMMENT '已回款金额';

ALTER TABLE constructing_project 
ADD COLUMN unreceiveMoney DECIMAL(15,2) NOT NULL DEFAULT 0.00 COMMENT '未回款金额';

ALTER TABLE constructing_project 
ADD COLUMN acceptanceDate DATE NULL COMMENT '项目验收日期';

-- 创建相关索引以提高查询性能
CREATE INDEX idx_is_agent ON constructing_project(isAgent);
CREATE INDEX idx_agent_name ON constructing_project(agentName);
CREATE INDEX idx_project_value ON constructing_project(value);
CREATE INDEX idx_acceptance_date ON constructing_project(acceptanceDate);

SELECT 'constructing_project表新字段添加成功' AS message;