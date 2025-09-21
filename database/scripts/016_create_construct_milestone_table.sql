-- 创建里程碑表
-- 版本: 016
-- 描述: 创建里程碑表，包含里程碑的工期和完成状态信息

CREATE TABLE construct_milestone (
    milestoneId BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '里程碑ID，主键',
    milestonePeriod INT COMMENT '里程碑花费工期（天数）',
    iscomplete TINYINT(1) DEFAULT 0 COMMENT '是否达成，0-未达成，1-已达成',
    completeDate DATE COMMENT '达成日期',
    createTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updateTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='里程碑表';

-- 创建索引
CREATE INDEX idx_construct_milestone_complete ON construct_milestone(iscomplete);
CREATE INDEX idx_construct_milestone_complete_date ON construct_milestone(completeDate);
CREATE INDEX idx_construct_milestone_period ON construct_milestone(milestonePeriod);