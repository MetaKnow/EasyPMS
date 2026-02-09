USE pms_db;

CREATE TABLE IF NOT EXISTS `extra_requirement_deliverableFiles` (
  `fileId` BIGINT NOT NULL AUTO_INCREMENT COMMENT '文件ID',
  `filePath` VARCHAR(500) DEFAULT NULL COMMENT '文件路径',
  `fileSize` BIGINT DEFAULT NULL COMMENT '文件大小(字节)',
  `uploadUser` VARCHAR(100) DEFAULT NULL COMMENT '上传人',
  `projectId` BIGINT DEFAULT NULL COMMENT '所属在建项目ID',
  `requirementId` BIGINT DEFAULT NULL COMMENT '所属合同外需求ID',
  `createTime` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`fileId`),
  KEY `idx_project_id` (`projectId`),
  KEY `idx_requirement_id` (`requirementId`),
  CONSTRAINT `fk_extra_req_files_project` FOREIGN KEY (`projectId`) REFERENCES `constructing_project` (`projectId`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_extra_req_files_requirement` FOREIGN KEY (`requirementId`) REFERENCES `extra_requirement` (`requirementId`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合同外需求交付物文件表';

SELECT 'Migration 073 executed: extra_requirement_deliverableFiles table created' AS message;
