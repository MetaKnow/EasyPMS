-- 创建标准步骤表
-- 版本: 014
-- 描述: 创建标准步骤表，包含标准步骤的计划和实际执行信息

CREATE TABLE standard_construct_step (
    sstepId BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '标准步骤ID，主键',
    sstepName VARCHAR(200) NOT NULL COMMENT '标准步骤名，必填',
    director BIGINT NOT NULL COMMENT '负责人，user表外键，必填',
    planStartDate DATE NOT NULL COMMENT '计划开始日期，必填',
    planEndDate DATE NOT NULL COMMENT '计划结束日期，必填',
    actualStartDate DATE COMMENT '实际开始日期',
    actualEndDate DATE COMMENT '实际结束日期',
    planPeriod INT COMMENT '计划工期（天数）',
    actualPeriod INT COMMENT '实际工期（天数）',
    afterServiceProjectId BIGINT COMMENT '运维项目ID，afterService_project表外键',
    createTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updateTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标准步骤表';

-- 添加外键约束
ALTER TABLE standard_construct_step 
ADD CONSTRAINT fk_standard_step_director 
FOREIGN KEY (director) REFERENCES user(userId) 
ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE standard_construct_step 
ADD CONSTRAINT fk_standard_step_afterservice 
FOREIGN KEY (afterServiceProjectId) REFERENCES afterService_project(projectId) 
ON DELETE SET NULL ON UPDATE CASCADE;

-- 创建索引
CREATE INDEX idx_standard_step_name ON standard_construct_step(sstepName);
CREATE INDEX idx_standard_step_director ON standard_construct_step(director);
CREATE INDEX idx_standard_step_plan_start ON standard_construct_step(planStartDate);
CREATE INDEX idx_standard_step_plan_end ON standard_construct_step(planEndDate);
CREATE INDEX idx_standard_step_afterservice ON standard_construct_step(afterServiceProjectId);