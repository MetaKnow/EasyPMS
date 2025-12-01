-- 增加 isComplete 字段到 afterService_event 表
-- 版本: 064
-- 描述: 增加 isComplete 字段，标识运维事件是否完成

ALTER TABLE afterService_event
ADD COLUMN isComplete BOOLEAN DEFAULT FALSE COMMENT '是否完成';
