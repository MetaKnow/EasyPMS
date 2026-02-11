USE pms_db;

CREATE TABLE IF NOT EXISTS `customer_follow_up` (
  `recordId` BIGINT NOT NULL AUTO_INCREMENT,
  `followUpPerson` BIGINT NOT NULL,
  `followUpDate` DATE NOT NULL,
  `followUpWay` ENUM('电话回访','现场回访') NOT NULL,
  `description` TEXT NULL,
  PRIMARY KEY (`recordId`),
  KEY `idx_customer_follow_up_person` (`followUpPerson`),
  KEY `idx_customer_follow_up_date` (`followUpDate`),
  CONSTRAINT `fk_customer_follow_up_person` FOREIGN KEY (`followUpPerson`) REFERENCES `user` (`userId`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SELECT 'Migration 074 executed: customer_follow_up table created' AS message;
