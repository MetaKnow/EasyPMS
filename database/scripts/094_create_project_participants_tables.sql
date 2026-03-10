-- 迁移版本: 094
-- 描述: 创建项目参与人员表 constructing_project_participants 和 afterservice_project_participants
-- 创建时间: 2026-03-09

USE pms_db;

-- 创建在建项目参与人员表
CREATE TABLE IF NOT EXISTS constructing_project_participants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    projectId BIGINT NOT NULL COMMENT '在建项目ID（外键）',
    userId BIGINT NOT NULL COMMENT '用户ID（外键）',
    createTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updateTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT fk_cpp_project FOREIGN KEY (projectId) REFERENCES constructing_project(projectId) ON DELETE CASCADE,
    CONSTRAINT fk_cpp_user FOREIGN KEY (userId) REFERENCES user(userId) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='在建项目参与人员表';

-- 创建索引
CREATE INDEX idx_cpp_project_id ON constructing_project_participants(projectId);
CREATE INDEX idx_cpp_user_id ON constructing_project_participants(userId);

-- 创建运维项目参与人员表
CREATE TABLE IF NOT EXISTS afterservice_project_participants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    projectId BIGINT NOT NULL COMMENT '运维项目ID（外键）',
    userId BIGINT NOT NULL COMMENT '用户ID（外键）',
    createTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updateTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT fk_app_project FOREIGN KEY (projectId) REFERENCES afterService_project(projectId) ON DELETE CASCADE,
    CONSTRAINT fk_app_user FOREIGN KEY (userId) REFERENCES user(userId) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='运维项目参与人员表';

-- 创建索引
CREATE INDEX idx_app_project_id ON afterservice_project_participants(projectId);
CREATE INDEX idx_app_user_id ON afterservice_project_participants(userId);

SELECT '项目参与人员表创建成功' AS message;
