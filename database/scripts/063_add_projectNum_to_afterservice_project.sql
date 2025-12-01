-- 为运维项目表添加项目编号字段，并为历史数据回填
-- 版本: 063
-- 描述: 新增 projectNum（不可编辑，唯一），格式 MS-YYYYMMDDHHMMSS；为现有数据按 createTime 回填；添加唯一索引并设置为非空。

-- 1) 添加列（先允许为 NULL，便于回填）
ALTER TABLE afterService_project 
ADD COLUMN projectNum VARCHAR(32) NULL COMMENT '项目编号，自动生成，唯一';

-- 2) 为历史数据回填项目编号（使用 createTime，如为空则用当前时间）
UPDATE afterService_project 
SET projectNum = CONCAT('MS-', DATE_FORMAT(COALESCE(createTime, NOW()), '%Y%m%d%H%i%s'))
WHERE projectNum IS NULL OR projectNum = '';

-- 3) 创建唯一索引
CREATE UNIQUE INDEX idx_afterservice_projectNum ON afterService_project(projectNum);

-- 4) 将列改为非空
ALTER TABLE afterService_project 
MODIFY COLUMN projectNum VARCHAR(32) NOT NULL COMMENT '项目编号，自动生成，唯一';

