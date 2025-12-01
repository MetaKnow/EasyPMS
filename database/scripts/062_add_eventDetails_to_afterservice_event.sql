-- 为运维事件表新增事件描述字段
-- 版本: 062
-- 描述: 在 afterService_event 表中新增 eventDetails 字段，长度 1000

ALTER TABLE afterService_event
  ADD COLUMN eventDetails VARCHAR(1000) NULL COMMENT '事件描述（最长1000字符）' AFTER eventhours;

