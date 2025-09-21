-- 创建非标准步骤表
-- 版本: 017
-- 描述: 创建非标准步骤表，包含非标准步骤的计划和实际执行信息

CREATE TABLE nonstandard_construct_step (
    nstepId BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '非标准步骤ID，主键',
    nstepName VARCHAR(200) NOT NULL COMMENT '非标准步骤名，必填',
    director BIGINT NOT NULL COMMENT '负责人，user表外键，必填',
    planStartDate DATE NOT NULL COMMENT '计划开始日期，必填',
    planEndDate DATE NOT NULL COMMENT '计划结束日期，必填',
    actualStartDate DATE COMMENT '实际开始日期',
    actualEndDate DATE COMMENT '实际结束日期',
    planPeriod INT COMMENT '计划工期（天数）',
    actualPeriod INT COMMENT '实际工期（天数）',
    createTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updateTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='非标准步骤表';

-- 添加外键约束
ALTER TABLE nonstandard_construct_step 
ADD CONSTRAINT fk_nonstandard_step_director 
FOREIGN KEY (director) REFERENCES user(userId) 
ON DELETE RESTRICT ON UPDATE CASCADE;

-- 创建索引
CREATE INDEX idx_nonstandard_step_name ON nonstandard_construct_step(nstepName);
CREATE INDEX idx_nonstandard_step_director ON nonstandard_construct_step(director);
CREATE INDEX idx_nonstandard_step_plan_start ON nonstandard_construct_step(planStartDate);
CREATE INDEX idx_nonstandard_step_plan_end ON nonstandard_construct_step(planEndDate);