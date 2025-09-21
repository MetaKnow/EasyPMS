-- 迁移版本: 008
-- 描述: 创建项目表 constructing_project
-- 创建时间: 2024-01-20

USE missoft_pms;

-- 创建项目表
CREATE TABLE IF NOT EXISTS constructing_project (
    projectId BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '项目ID（主键）',
    projectNum VARCHAR(50) NOT NULL UNIQUE COMMENT '项目编号',
    year INT NOT NULL COMMENT '年度',
    projectName VARCHAR(200) NOT NULL COMMENT '项目名称',
    projectType VARCHAR(50) NOT NULL COMMENT '项目类型',
    projectLeader BIGINT COMMENT '项目负责人ID（外键）',
    saleLeader BIGINT COMMENT '商务负责人ID（外键）',
    projectState VARCHAR(50) NOT NULL DEFAULT '待开始' COMMENT '项目状态',
    arcSystem VARCHAR(100) COMMENT '档案系统',
    startDate DATE COMMENT '开始日期',
    planEndDate DATE COMMENT '计划结束日期',
    actualEndDate DATE COMMENT '实际结束日期',
    planPeriod INT COMMENT '项目预计工期（天）',
    actualPeriod INT COMMENT '项目实际工期（天）',
    createTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updateTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目表';

-- 添加外键约束
ALTER TABLE constructing_project 
ADD CONSTRAINT fk_project_leader 
FOREIGN KEY (projectLeader) REFERENCES user(userId) ON DELETE SET NULL;

ALTER TABLE constructing_project 
ADD CONSTRAINT fk_sale_leader 
FOREIGN KEY (saleLeader) REFERENCES user(userId) ON DELETE SET NULL;

-- 创建索引
CREATE INDEX idx_project_num ON constructing_project(projectNum);
CREATE INDEX idx_year ON constructing_project(year);
CREATE INDEX idx_project_state ON constructing_project(projectState);
CREATE INDEX idx_start_date ON constructing_project(startDate);
CREATE INDEX idx_project_leader ON constructing_project(projectLeader);
CREATE INDEX idx_sale_leader ON constructing_project(saleLeader);

SELECT 'constructing_project表创建成功' AS message;