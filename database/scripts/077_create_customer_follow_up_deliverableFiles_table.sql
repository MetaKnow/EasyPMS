USE pms_db;

CREATE TABLE IF NOT EXISTS `customer_follow_up_deliverableFiles` (
  `fileId` BIGINT NOT NULL AUTO_INCREMENT COMMENT '文件ID',
  `filePath` VARCHAR(500) DEFAULT NULL COMMENT '文件路径',
  `fileSize` BIGINT DEFAULT NULL COMMENT '文件大小(字节)',
  `uploadUser` VARCHAR(100) DEFAULT NULL COMMENT '上传人',
  `afterServiceProjectId` BIGINT DEFAULT NULL COMMENT '所属运维项目ID',
  `followUpRecordId` BIGINT DEFAULT NULL COMMENT '所属回访记录ID',
  PRIMARY KEY (`fileId`),
  KEY `idx_customer_follow_up_files_project` (`afterServiceProjectId`),
  KEY `idx_customer_follow_up_files_record` (`followUpRecordId`),
  CONSTRAINT `fk_customer_follow_up_files_project` FOREIGN KEY (`afterServiceProjectId`) REFERENCES `afterservice_project` (`projectId`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_customer_follow_up_files_record` FOREIGN KEY (`followUpRecordId`) REFERENCES `customer_follow_up` (`recordId`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='回访附件表';

SELECT 'Migration 077 executed: customer_follow_up_deliverableFiles table created' AS message;
