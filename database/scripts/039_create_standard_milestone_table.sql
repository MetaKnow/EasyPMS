-- 创建标准里程碑表
-- 文件: 039_create_standard_milestone_table.sql
-- 描述: 创建标准里程碑管理模块的数据表

CREATE TABLE IF NOT EXISTS standard_milestone (
    milestone_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '里程碑ID（主键）',
    milestone_name VARCHAR(100) NOT NULL COMMENT '里程碑名称',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标准里程碑信息表';

-- 创建索引
CREATE INDEX idx_milestone_name ON standard_milestone(milestone_name);