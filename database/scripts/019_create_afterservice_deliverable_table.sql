-- 创建运维交付物表
-- 版本: 019
-- 描述: 创建运维交付物表，包含运维交付物文件信息和关联关系

CREATE TABLE afterService_deliverable (
    deliverableId BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '交付物ID，主键',
    deliverableName VARCHAR(200) COMMENT '交付物名称',
    filePath VARCHAR(500) COMMENT '文件路径',
    fileSize BIGINT COMMENT '文件大小（字节）',
    projectId BIGINT NOT NULL COMMENT '运维项目ID，afterService_project表外键，必填',
    eventId BIGINT COMMENT '运维事件ID，afterService_event表外键',
    uploadUser BIGINT NOT NULL COMMENT '上传人，user表外键，必填',
    createTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updateTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='运维交付物表';

-- 添加外键约束
ALTER TABLE afterService_deliverable 
ADD CONSTRAINT fk_afterservice_deliverable_project 
FOREIGN KEY (projectId) REFERENCES afterService_project(projectId) 
ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE afterService_deliverable 
ADD CONSTRAINT fk_afterservice_deliverable_event 
FOREIGN KEY (eventId) REFERENCES afterService_event(eventId) 
ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE afterService_deliverable 
ADD CONSTRAINT fk_afterservice_deliverable_upload_user 
FOREIGN KEY (uploadUser) REFERENCES user(userId) 
ON DELETE RESTRICT ON UPDATE CASCADE;

-- 创建索引
CREATE INDEX idx_afterservice_deliverable_name ON afterService_deliverable(deliverableName);
CREATE INDEX idx_afterservice_deliverable_project ON afterService_deliverable(projectId);
CREATE INDEX idx_afterservice_deliverable_event ON afterService_deliverable(eventId);
CREATE INDEX idx_afterservice_deliverable_upload_user ON afterService_deliverable(uploadUser);
CREATE INDEX idx_afterservice_deliverable_file_path ON afterService_deliverable(filePath);