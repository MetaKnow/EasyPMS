-- 增加serviceType和serviceDirector字段
-- 版本: 019
-- 描述: 运维项目表增加运维类型和运维负责人字段

ALTER TABLE afterService_project ADD COLUMN serviceType VARCHAR(50) COMMENT '运维类型';
ALTER TABLE afterService_project ADD COLUMN serviceDirector BIGINT COMMENT '运维负责人，user表外键';

-- 添加外键约束
ALTER TABLE afterService_project 
ADD CONSTRAINT fk_afterservice_service_director 
FOREIGN KEY (serviceDirector) REFERENCES user(userId) 
ON DELETE RESTRICT ON UPDATE CASCADE;

-- 创建索引
CREATE INDEX idx_afterservice_service_director ON afterService_project(serviceDirector);
