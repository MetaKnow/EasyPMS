USE pms_db;

ALTER TABLE `customer_follow_up`
  ADD COLUMN `afterServiceProjectId` BIGINT NOT NULL AFTER `description`,
  ADD COLUMN `createTime` DATETIME DEFAULT CURRENT_TIMESTAMP AFTER `afterServiceProjectId`,
  ADD COLUMN `updateTime` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER `createTime`;

ALTER TABLE `customer_follow_up`
  ADD CONSTRAINT `fk_customer_follow_up_project`
    FOREIGN KEY (`afterServiceProjectId`) REFERENCES `afterService_project`(`projectId`)
    ON DELETE CASCADE ON UPDATE CASCADE;

-- 索引
CREATE INDEX `idx_customer_follow_up_project` ON `customer_follow_up`(`afterServiceProjectId`);
CREATE INDEX `idx_customer_follow_up_way` ON `customer_follow_up`(`followUpWay`);

SELECT 'Migration 075 executed: added projectId and timestamps to customer_follow_up' AS message;
