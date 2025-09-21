-- 创建运维项目表
-- 版本: 012
-- 描述: 创建运维项目表，包含运维项目基本信息和状态管理

CREATE TABLE afterService_project (
    projectId BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '项目ID，主键',
    projectName VARCHAR(200) NOT NULL COMMENT '项目名称，必填',
    arcSystem VARCHAR(100) NOT NULL COMMENT '档案系统，必填',
    director BIGINT NOT NULL COMMENT '负责人，user表外键，必填',
    ServiceYear INT COMMENT '运维年数',
    startDate DATE COMMENT '开始日期',
    endDate DATE COMMENT '结束日期',
    serviceState ENUM('免费运维期', '付费运维', '无付费运维', '暂停运维') NOT NULL COMMENT '运维状态，必填，值域为免费运维期、付费运维、无付费运维、暂停运维',
    totalHours DECIMAL(10,2) COMMENT '总工时',
    createTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updateTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='运维项目表';

-- 添加外键约束
ALTER TABLE afterService_project 
ADD CONSTRAINT fk_afterservice_director 
FOREIGN KEY (director) REFERENCES user(userId) 
ON DELETE RESTRICT ON UPDATE CASCADE;

-- 创建索引
CREATE INDEX idx_afterservice_project_name ON afterService_project(projectName);
CREATE INDEX idx_afterservice_director ON afterService_project(director);
CREATE INDEX idx_afterservice_state ON afterService_project(serviceState);
CREATE INDEX idx_afterservice_start_date ON afterService_project(startDate);
CREATE INDEX idx_afterservice_end_date ON afterService_project(endDate);