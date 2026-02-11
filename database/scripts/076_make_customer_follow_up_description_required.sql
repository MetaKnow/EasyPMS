UPDATE `customer_follow_up` SET `description` = '' WHERE `description` IS NULL;
ALTER TABLE `customer_follow_up` MODIFY COLUMN `description` VARCHAR(1000) NOT NULL;
