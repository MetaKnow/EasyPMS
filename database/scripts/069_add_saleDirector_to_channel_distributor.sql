-- 增加销售总监字段到渠道商表
-- 文件: 069_add_saleDirector_to_channel_distributor.sql
-- 描述: channel_distributor表增加saleDirector字段，user表外键

USE `pms_db`;

-- 添加字段
ALTER TABLE `channel_distributor`
ADD COLUMN `saleDirector` BIGINT COMMENT '销售总监（关联user表）';

-- 添加索引和外键
ALTER TABLE `channel_distributor`
ADD INDEX `idx_sale_director` (`saleDirector`);

ALTER TABLE `channel_distributor`
ADD CONSTRAINT `fk_channel_distributor_saleDirector`
FOREIGN KEY (`saleDirector`) REFERENCES `user`(`userId`) ON DELETE SET NULL;

SELECT 'Added saleDirector field to channel_distributor table successfully!' as message;
