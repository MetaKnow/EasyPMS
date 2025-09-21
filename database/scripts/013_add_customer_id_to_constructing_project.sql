-- 为constructing_project表添加customerId字段
-- 版本: 013
-- 描述: 为constructing_project表添加customerId外键字段，关联customer表

ALTER TABLE constructing_project 
ADD COLUMN customerId BIGINT COMMENT '客户ID，customer表外键';

-- 添加外键约束
ALTER TABLE constructing_project 
ADD CONSTRAINT fk_constructing_project_customer 
FOREIGN KEY (customerId) REFERENCES customer(customerId) 
ON DELETE SET NULL ON UPDATE CASCADE;

-- 创建索引
CREATE INDEX idx_constructing_project_customer ON constructing_project(customerId);