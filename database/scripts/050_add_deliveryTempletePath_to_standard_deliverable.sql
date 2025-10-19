-- 为标准交付物表添加模板相对路径字段
-- 版本: 050
-- 描述: 为standard_deliverable表添加deliveryTempletePath字段，用于存放上传模板文件的相对路径

ALTER TABLE standard_deliverable 
ADD COLUMN deliveryTempletePath VARCHAR(255) COMMENT '模板文件相对路径' AFTER deliverableType;