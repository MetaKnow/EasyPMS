-- 修改afterService_project表director字段为saleDirector
-- 版本: 061
-- 描述: 将运维项目表中的director字段重命名为saleDirector，并更新相应的外键和索引

-- 1. 删除旧的外键约束
ALTER TABLE afterService_project DROP FOREIGN KEY fk_afterservice_director;

-- 2. 删除旧的索引
DROP INDEX idx_afterservice_director ON afterService_project;

-- 3. 修改字段名称和注释
ALTER TABLE afterService_project CHANGE COLUMN director saleDirector BIGINT NOT NULL COMMENT '销售负责人，user表外键，必填';

-- 4. 添加新的外键约束
ALTER TABLE afterService_project 
ADD CONSTRAINT fk_afterservice_project_saleDirector 
FOREIGN KEY (saleDirector) REFERENCES user(userId) 
ON DELETE RESTRICT ON UPDATE CASCADE;

-- 5. 创建新的索引
CREATE INDEX idx_afterservice_project_saleDirector ON afterService_project(saleDirector);
