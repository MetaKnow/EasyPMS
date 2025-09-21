-- 创建运维事件表
-- 版本: 018
-- 描述: 创建运维事件表，包含运维事件的详细信息和工时记录

CREATE TABLE afterService_event (
    eventId BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '事件ID，主键',
    eventName VARCHAR(200) NOT NULL COMMENT '事件名称，必填',
    eventDate DATE NOT NULL COMMENT '事件日期，必填',
    eventType ENUM('主动服务', '响应服务') NOT NULL COMMENT '事件类型，必填，值域为主动服务、响应服务',
    serviceMode ENUM('远程服务', '现场服务') NOT NULL COMMENT '服务方式，必填，值域为远程服务、现场服务',
    director BIGINT NOT NULL COMMENT '负责人，user表外键，必填',
    eventhours DECIMAL(8,2) NOT NULL COMMENT '事件工时，必填',
    afterServiceProjectId BIGINT NOT NULL COMMENT '运维项目ID，afterService_project表外键，必填',
    createTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updateTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='运维事件表';

-- 添加外键约束
ALTER TABLE afterService_event 
ADD CONSTRAINT fk_afterservice_event_director 
FOREIGN KEY (director) REFERENCES user(userId) 
ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE afterService_event 
ADD CONSTRAINT fk_afterservice_event_project 
FOREIGN KEY (afterServiceProjectId) REFERENCES afterService_project(projectId) 
ON DELETE CASCADE ON UPDATE CASCADE;

-- 创建索引
CREATE INDEX idx_afterservice_event_name ON afterService_event(eventName);
CREATE INDEX idx_afterservice_event_date ON afterService_event(eventDate);
CREATE INDEX idx_afterservice_event_type ON afterService_event(eventType);
CREATE INDEX idx_afterservice_event_mode ON afterService_event(serviceMode);
CREATE INDEX idx_afterservice_event_director ON afterService_event(director);
CREATE INDEX idx_afterservice_event_project ON afterService_event(afterServiceProjectId);