-- ----------------------------
-- Add saleLeader to customer table
-- ----------------------------
ALTER TABLE `customer` ADD COLUMN `saleLeader` bigint(20) DEFAULT NULL COMMENT '销售负责人ID';

-- Add foreign key constraint
ALTER TABLE `customer` ADD CONSTRAINT `fk_customer_saleLeader` FOREIGN KEY (`saleLeader`) REFERENCES `user` (`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
