-- 迁移版本: 047
-- 描述: 将constructing_project表中的arcSystem改为softId（archieveSoft外键），
--       将agentName改为channelId（channel_distributor外键）。
-- 创建时间: 2025-10-08

USE `pms_db`;

-- =============================================
-- 修改字段：新增softId与channelId，并建立外键与索引
-- =============================================

-- 新增档案软件外键字段（softId）
ALTER TABLE `constructing_project`
  ADD COLUMN `softId` BIGINT NULL COMMENT '档案软件ID（archieveSoft 外键）' AFTER `arcSystem`;

-- 为softId创建索引
ALTER TABLE `constructing_project`
  ADD INDEX `idx_soft_id` (`softId`);

-- 建立softId外键约束，引用archieveSoft(softId)
ALTER TABLE `constructing_project`
  ADD CONSTRAINT `fk_soft_id`
  FOREIGN KEY (`softId`) REFERENCES `archieveSoft`(`softId`)
  ON DELETE SET NULL
  ON UPDATE CASCADE;

-- 新增渠道外键字段（channelId）
ALTER TABLE `constructing_project`
  ADD COLUMN `channelId` BIGINT NULL COMMENT '渠道ID（channel_distributor 外键）' AFTER `agentName`;

-- 为channelId创建索引
ALTER TABLE `constructing_project`
  ADD INDEX `idx_channel_id` (`channelId`);

-- 建立channelId外键约束，引用channel_distributor(channel_id)
ALTER TABLE `constructing_project`
  ADD CONSTRAINT `fk_channel_id`
  FOREIGN KEY (`channelId`) REFERENCES `channel_distributor`(`channel_id`)
  ON DELETE SET NULL
  ON UPDATE CASCADE;

-- =============================================
-- 清理旧字段及索引
-- =============================================

-- 删除旧索引：idx_agent_name（存在于迁移010）
DROP INDEX `idx_agent_name` ON `constructing_project`;

-- 删除旧字段：arcSystem、agentName
ALTER TABLE `constructing_project`
  DROP COLUMN `arcSystem`,
  DROP COLUMN `agentName`;

SELECT 'constructing_project表字段已改为外键：softId与channelId' AS message;