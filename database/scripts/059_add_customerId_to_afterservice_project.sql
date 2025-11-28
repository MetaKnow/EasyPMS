-- ----------------------------
-- Add customerId to afterservice_project table
-- ----------------------------
ALTER TABLE `afterservice_project` ADD COLUMN `customerId` bigint(20) DEFAULT NULL COMMENT '客户ID';

-- Add foreign key constraint
ALTER TABLE `afterservice_project` ADD CONSTRAINT `fk_afterservice_project_customer` FOREIGN KEY (`customerId`) REFERENCES `customer` (`customerId`) ON DELETE SET NULL ON UPDATE CASCADE;
