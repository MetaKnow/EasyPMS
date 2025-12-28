-- MySQL dump 10.13  Distrib 5.7.24, for osx11.1 (x86_64)
--
-- Host: localhost    Database: pms_db
-- ------------------------------------------------------
-- Server version	8.0.34

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `afterservice_deliverable`
--

DROP TABLE IF EXISTS `afterservice_deliverable`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `afterservice_deliverable` (
  `deliverableId` bigint NOT NULL AUTO_INCREMENT COMMENT '交付物ID，主键',
  `deliverableName` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '交付物名称',
  `filePath` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '文件路径',
  `fileSize` bigint DEFAULT NULL COMMENT '文件大小（字节）',
  `projectId` bigint NOT NULL COMMENT '运维项目ID，afterService_project表外键，必填',
  `eventId` bigint DEFAULT NULL COMMENT '运维事件ID，afterService_event表外键',
  `uploadUser` bigint NOT NULL COMMENT '上传人，user表外键，必填',
  `createTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`deliverableId`) USING BTREE,
  KEY `idx_afterservice_deliverable_name` (`deliverableName`) USING BTREE,
  KEY `idx_afterservice_deliverable_project` (`projectId`) USING BTREE,
  KEY `idx_afterservice_deliverable_event` (`eventId`) USING BTREE,
  KEY `idx_afterservice_deliverable_upload_user` (`uploadUser`) USING BTREE,
  KEY `idx_afterservice_deliverable_file_path` (`filePath`) USING BTREE,
  CONSTRAINT `fk_afterservice_deliverable_event` FOREIGN KEY (`eventId`) REFERENCES `afterservice_event` (`eventId`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_afterservice_deliverable_project` FOREIGN KEY (`projectId`) REFERENCES `afterservice_project` (`projectId`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_afterservice_deliverable_upload_user` FOREIGN KEY (`uploadUser`) REFERENCES `user` (`userId`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='运维交付物表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `afterservice_deliverable`
--

LOCK TABLES `afterservice_deliverable` WRITE;
/*!40000 ALTER TABLE `afterservice_deliverable` DISABLE KEYS */;
INSERT INTO `afterservice_deliverable` VALUES (1,'与泛微OA归档接口对接-20251215-095746-408.txt','afterServiceDeliverableFiles/MS-20251212183659-西安音乐学院档案管理系统建设项目/2025-12-08-与泛微OA归档接口对接/与泛微OA归档接口对接-20251215-095746-408.txt',104,1,1,4,'2025-12-15 01:57:46','2025-12-15 01:57:46');
/*!40000 ALTER TABLE `afterservice_deliverable` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `afterservice_event`
--

DROP TABLE IF EXISTS `afterservice_event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `afterservice_event` (
  `eventId` bigint NOT NULL AUTO_INCREMENT COMMENT '事件ID，主键',
  `eventName` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '事件名称，必填',
  `eventDate` date NOT NULL COMMENT '事件日期，必填',
  `eventType` enum('主动服务','响应服务') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '事件类型，必填，值域为主动服务、响应服务',
  `serviceMode` enum('远程服务','现场服务') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '服务方式，必填，值域为远程服务、现场服务',
  `director` bigint NOT NULL COMMENT '负责人，user表外键，必填',
  `eventhours` decimal(8,2) NOT NULL COMMENT '事件工时，必填',
  `eventDetails` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '事件描述（最长1000字符）',
  `afterServiceProjectId` bigint NOT NULL COMMENT '运维项目ID，afterService_project表外键，必填',
  `createTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `isComplete` tinyint(1) DEFAULT '0' COMMENT '是否完成',
  PRIMARY KEY (`eventId`) USING BTREE,
  KEY `idx_afterservice_event_name` (`eventName`) USING BTREE,
  KEY `idx_afterservice_event_date` (`eventDate`) USING BTREE,
  KEY `idx_afterservice_event_type` (`eventType`) USING BTREE,
  KEY `idx_afterservice_event_mode` (`serviceMode`) USING BTREE,
  KEY `idx_afterservice_event_director` (`director`) USING BTREE,
  KEY `idx_afterservice_event_project` (`afterServiceProjectId`) USING BTREE,
  CONSTRAINT `fk_afterservice_event_director` FOREIGN KEY (`director`) REFERENCES `user` (`userId`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_afterservice_event_project` FOREIGN KEY (`afterServiceProjectId`) REFERENCES `afterservice_project` (`projectId`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='运维事件表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `afterservice_event`
--

LOCK TABLES `afterservice_event` WRITE;
/*!40000 ALTER TABLE `afterservice_event` DISABLE KEYS */;
INSERT INTO `afterservice_event` VALUES (1,'与泛微OA归档接口对接，数据规范验证','2025-12-08','响应服务','远程服务',4,0.50,'提供FTP信息',1,'2025-12-15 01:54:06','2025-12-15 02:03:41',1),(2,'与OA归档接口开始对接','2025-12-10','响应服务','远程服务',4,2.00,'归档文件推送过来，验证数据是否符合规范',1,'2025-12-15 02:01:44','2025-12-15 02:05:03',1),(3,'与OA归档接口对接核查，文件缺失，OA方修改后再传','2025-12-15','响应服务','远程服务',4,1.00,'数据推送过来后，核查后数据未符合标准，OA方修改',1,'2025-12-15 02:08:31','2025-12-15 02:08:31',0);
/*!40000 ALTER TABLE `afterservice_event` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `afterservice_project`
--

DROP TABLE IF EXISTS `afterservice_project`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `afterservice_project` (
  `projectId` bigint NOT NULL AUTO_INCREMENT COMMENT '项目ID，主键',
  `projectName` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '项目名称，必填',
  `arcSystem` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '档案系统，必填',
  `saleDirector` bigint NOT NULL COMMENT '销售负责人，user表外键，必填',
  `ServiceYear` int DEFAULT NULL COMMENT '运维年数',
  `startDate` date DEFAULT NULL COMMENT '开始日期',
  `endDate` date DEFAULT NULL COMMENT '结束日期',
  `serviceState` enum('免费运维期','付费运维','无付费运维','暂停运维') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '运维状态，必填，值域为免费运维期、付费运维、无付费运维、暂停运维',
  `totalHours` decimal(10,2) DEFAULT NULL COMMENT '总工时',
  `createTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `customerId` bigint DEFAULT NULL COMMENT '客户ID',
  `projectNum` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '项目编号，自动生成，唯一',
  `serviceType` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '运维类型',
  `serviceDirector` bigint DEFAULT NULL COMMENT '运维负责人，user表外键',
  `constructingProjectId` bigint DEFAULT NULL COMMENT '关联的在建项目ID，外键',
  PRIMARY KEY (`projectId`) USING BTREE,
  UNIQUE KEY `idx_afterservice_projectNum` (`projectNum`) USING BTREE,
  KEY `idx_afterservice_project_name` (`projectName`) USING BTREE,
  KEY `idx_afterservice_state` (`serviceState`) USING BTREE,
  KEY `idx_afterservice_start_date` (`startDate`) USING BTREE,
  KEY `idx_afterservice_end_date` (`endDate`) USING BTREE,
  KEY `fk_afterservice_project_customer` (`customerId`) USING BTREE,
  KEY `idx_afterservice_project_saleDirector` (`saleDirector`) USING BTREE,
  KEY `idx_afterservice_service_director` (`serviceDirector`) USING BTREE,
  KEY `idx_afterservice_constructing_project` (`constructingProjectId`) USING BTREE,
  CONSTRAINT `fk_afterservice_constructing_project` FOREIGN KEY (`constructingProjectId`) REFERENCES `constructing_project` (`projectId`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_afterservice_project_customer` FOREIGN KEY (`customerId`) REFERENCES `customer` (`customerId`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_afterservice_project_saleDirector` FOREIGN KEY (`saleDirector`) REFERENCES `user` (`userId`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_afterservice_service_director` FOREIGN KEY (`serviceDirector`) REFERENCES `user` (`userId`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='运维项目表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `afterservice_project`
--

LOCK TABLES `afterservice_project` WRITE;
/*!40000 ALTER TABLE `afterservice_project` DISABLE KEYS */;
INSERT INTO `afterservice_project` VALUES (1,'西安音乐学院档案管理系统建设项目','DAIS数字档案馆（室）一体化系统 (V7.1)',2,3,'2023-09-01','2026-09-01','免费运维期',NULL,'2025-12-12 10:38:19','2025-12-24 16:03:11',2,'MS-20251212183659','我公司全权运维',4,NULL);
/*!40000 ALTER TABLE `afterservice_project` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `archievesoft`
--

DROP TABLE IF EXISTS `archievesoft`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `archievesoft` (
  `softId` bigint NOT NULL AUTO_INCREMENT COMMENT 'Software ID',
  `softName` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Software Name',
  `softVersion` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Software Version',
  `softType` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '产品类型',
  `createTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation Time',
  `updateTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
  PRIMARY KEY (`softId`) USING BTREE,
  KEY `idx_soft_name` (`softName`) USING BTREE,
  KEY `idx_soft_version` (`softVersion`) USING BTREE,
  KEY `idx_create_time` (`createTime`) USING BTREE,
  KEY `idx_soft_type` (`softType`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='Archive Software Table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `archievesoft`
--

LOCK TABLES `archievesoft` WRITE;
/*!40000 ALTER TABLE `archievesoft` DISABLE KEYS */;
INSERT INTO `archievesoft` VALUES (1,'DAIS数字档案馆（室）一体化系统','V7.1','标准产品','2025-12-11 12:01:59','2025-12-11 12:01:59'),(2,'DAIS数字档案馆（室）一体化系统','V7.2','标准产品','2025-12-11 12:02:13','2025-12-11 12:02:13'),(3,'CAMS综合档案管理系统专业版','V5.1','标准产品','2025-12-11 12:02:37','2025-12-11 12:02:37'),(4,'CAMS综合档案管理系统网络版','V3.5','标准产品','2025-12-11 12:02:53','2025-12-11 12:02:53'),(5,'档博通综合档案馆馆藏资源管理系统','V7.1','综合档案馆产品体系','2025-12-11 12:03:40','2025-12-11 12:03:40'),(6,'电子阅览室系统','V2','综合档案馆产品体系','2025-12-11 12:03:55','2025-12-11 12:03:55');
/*!40000 ALTER TABLE `archievesoft` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `channel_distributor`
--

DROP TABLE IF EXISTS `channel_distributor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `channel_distributor` (
  `channel_id` bigint NOT NULL AUTO_INCREMENT COMMENT '渠道ID（主键）',
  `channel_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '渠道名称',
  `contactor` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '联系人',
  `phone_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '联系方式',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `saleDirector` bigint DEFAULT NULL COMMENT '销售总监（关联user表）',
  `sale_director` bigint DEFAULT NULL COMMENT '销售总监（关联user表）',
  PRIMARY KEY (`channel_id`) USING BTREE,
  KEY `idx_channel_name` (`channel_name`) USING BTREE,
  KEY `idx_contactor` (`contactor`) USING BTREE,
  KEY `idx_sale_director` (`saleDirector`),
  CONSTRAINT `fk_channel_distributor_saleDirector` FOREIGN KEY (`saleDirector`) REFERENCES `user` (`userId`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='渠道商信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `channel_distributor`
--

LOCK TABLES `channel_distributor` WRITE;
/*!40000 ALTER TABLE `channel_distributor` DISABLE KEYS */;
INSERT INTO `channel_distributor` VALUES (1,'山西阳光兰台科技有限公司','孙文涛','13111067367','2025-12-18 14:06:07','2025-12-27 15:24:34',10,NULL),(2,'成都联运科技有限公司','李中全','13281132001','2025-12-28 07:58:27','2025-12-28 07:58:27',2,NULL),(3,'贵州秀城数字档案文化服务有限公司','解秀承','18935123492','2025-12-28 07:59:04','2025-12-28 07:59:04',11,NULL);
/*!40000 ALTER TABLE `channel_distributor` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `construct_deliverablefiles`
--

DROP TABLE IF EXISTS `construct_deliverablefiles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `construct_deliverablefiles` (
  `fileId` bigint NOT NULL AUTO_INCREMENT COMMENT '文件ID，主键',
  `filePath` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件路径，必填',
  `fileSize` bigint DEFAULT NULL COMMENT '文件大小（字节）',
  `uploadUser` bigint NOT NULL COMMENT '上传人，user表外键，必填',
  `deliverableId` bigint DEFAULT NULL COMMENT '交付物ID，construct_standard_deliverable表外键',
  `projectId` bigint DEFAULT NULL COMMENT '项目ID，constructing_project表外键',
  `projectStepId` bigint DEFAULT NULL COMMENT '项目步骤关系ID，project_sstep_relations表外键',
  `nstepId` bigint DEFAULT NULL COMMENT '非标步骤ID，nonstandard_construct_step表外键',
  `milestoneId` bigint DEFAULT NULL COMMENT '里程碑ID，construct_milestone表外键',
  `createTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`fileId`) USING BTREE,
  KEY `idx_construct_deliverableFiles_file_path` (`filePath`) USING BTREE,
  KEY `idx_construct_deliverableFiles_upload_user` (`uploadUser`) USING BTREE,
  KEY `idx_construct_deliverableFiles_deliverable` (`deliverableId`) USING BTREE,
  KEY `idx_construct_deliverableFiles_project` (`projectId`) USING BTREE,
  KEY `idx_construct_deliverableFiles_nstep` (`nstepId`) USING BTREE,
  KEY `idx_construct_deliverableFiles_milestone` (`milestoneId`) USING BTREE,
  KEY `idx_construct_deliverableFiles_projectStep` (`projectStepId`) USING BTREE,
  CONSTRAINT `fk_construct_deliverableFiles_deliverable` FOREIGN KEY (`deliverableId`) REFERENCES `standard_deliverable` (`deliverableId`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_construct_deliverableFiles_milestone` FOREIGN KEY (`milestoneId`) REFERENCES `construct_milestone` (`milestoneId`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_construct_deliverableFiles_nstep` FOREIGN KEY (`nstepId`) REFERENCES `nonstandard_construct_step` (`nstepId`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_construct_deliverableFiles_project` FOREIGN KEY (`projectId`) REFERENCES `constructing_project` (`projectId`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_construct_deliverableFiles_projectStep` FOREIGN KEY (`projectStepId`) REFERENCES `project_sstep_relations` (`relationId`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_construct_deliverableFiles_upload_user` FOREIGN KEY (`uploadUser`) REFERENCES `user` (`userId`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='交付物文件表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `construct_deliverablefiles`
--

LOCK TABLES `construct_deliverablefiles` WRITE;
/*!40000 ALTER TABLE `construct_deliverablefiles` DISABLE KEYS */;
INSERT INTO `construct_deliverablefiles` VALUES (1,'deliverableFiles/MS-20251211232738-北京市城建勘测设计院档案系统建设项目/02需求确定/系统应用规模及部署需求调研结果-02系统应用规模及部署需求调研-20251212-184325.docx',438562,1,2,1,2,NULL,2,'2025-12-12 10:43:26','2025-12-12 10:43:26');
/*!40000 ALTER TABLE `construct_deliverablefiles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `construct_milestone`
--

DROP TABLE IF EXISTS `construct_milestone`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `construct_milestone` (
  `milestoneId` bigint NOT NULL AUTO_INCREMENT COMMENT '里程碑ID，主键',
  `milestoneName` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '里程碑名称，必填',
  `projectId` bigint DEFAULT NULL COMMENT '项目ID，constructing_project表外键',
  `milestonePeriod` int DEFAULT NULL COMMENT '里程碑花费工期（天数）',
  `iscomplete` tinyint(1) DEFAULT '0' COMMENT '是否达成，0-未达成，1-已达成',
  `completeDate` date DEFAULT NULL COMMENT '达成日期',
  `createTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`milestoneId`) USING BTREE,
  KEY `idx_construct_milestone_complete` (`iscomplete`) USING BTREE,
  KEY `idx_construct_milestone_complete_date` (`completeDate`) USING BTREE,
  KEY `idx_construct_milestone_period` (`milestonePeriod`) USING BTREE,
  KEY `idx_construct_milestone_project` (`projectId`) USING BTREE,
  KEY `idx_construct_milestone_name` (`milestoneName`) USING BTREE,
  CONSTRAINT `fk_construct_milestone_project` FOREIGN KEY (`projectId`) REFERENCES `constructing_project` (`projectId`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='里程碑表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `construct_milestone`
--

LOCK TABLES `construct_milestone` WRITE;
/*!40000 ALTER TABLE `construct_milestone` DISABLE KEYS */;
INSERT INTO `construct_milestone` VALUES (1,'01项目正式启动',1,NULL,0,NULL,'2025-12-11 15:28:09','2025-12-11 15:28:09'),(2,'02需求确定',1,2,0,NULL,'2025-12-11 15:28:09','2025-12-12 10:46:18'),(3,'03完成标准产品功能配置',1,NULL,0,NULL,'2025-12-11 15:28:09','2025-12-11 15:28:09'),(4,'04完成数据迁移',1,NULL,0,NULL,'2025-12-11 15:28:09','2025-12-11 15:28:09'),(5,'05完成接口开发集成',1,NULL,0,NULL,'2025-12-11 15:28:09','2025-12-11 15:28:09'),(6,'06完成个性化功能开发',1,NULL,0,NULL,'2025-12-11 15:28:09','2025-12-11 15:28:09'),(7,'07完成系统上线前功能调整及其他准备工作',1,NULL,0,NULL,'2025-12-11 15:28:09','2025-12-11 15:28:09'),(8,'08完成系统上线及试运行',1,NULL,0,NULL,'2025-12-11 15:28:09','2025-12-11 15:28:09'),(9,'09完成项目验收',1,NULL,0,NULL,'2025-12-11 15:28:09','2025-12-11 15:28:09');
/*!40000 ALTER TABLE `construct_milestone` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `constructing_project`
--

DROP TABLE IF EXISTS `constructing_project`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `constructing_project` (
  `projectId` bigint NOT NULL AUTO_INCREMENT COMMENT '项目ID（主键）',
  `projectNum` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '项目编号',
  `year` int NOT NULL COMMENT '年度',
  `projectName` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '项目名称',
  `projectType` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '项目类型',
  `projectLeader` bigint DEFAULT NULL COMMENT '项目负责人ID（外键）',
  `saleLeader` bigint DEFAULT NULL COMMENT '商务负责人ID（外键）',
  `projectState` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '待开始' COMMENT '项目状态',
  `softId` bigint DEFAULT NULL COMMENT '档案软件ID（archieveSoft 外键）',
  `startDate` date DEFAULT NULL COMMENT '开始日期',
  `planEndDate` date DEFAULT NULL COMMENT '计划结束日期',
  `actualEndDate` date DEFAULT NULL COMMENT '实际结束日期',
  `planPeriod` int DEFAULT NULL COMMENT '项目预计工期（天）',
  `actualPeriod` int DEFAULT NULL COMMENT '项目实际工期（天）',
  `createTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `isAgent` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否渠道项目（0-否，1-是）',
  `channelId` bigint DEFAULT NULL COMMENT '渠道ID（channel_distributor 外键）',
  `value` decimal(15,2) DEFAULT NULL COMMENT '项目金额',
  `receivedMoney` decimal(15,2) NOT NULL COMMENT '已回款金额',
  `unreceiveMoney` decimal(15,2) NOT NULL COMMENT '未回款金额',
  `acceptanceDate` date DEFAULT NULL COMMENT '项目验收日期',
  `customerId` bigint DEFAULT NULL COMMENT '客户ID，customer表外键',
  `constructContent` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '项目建设内容（最多100字符）',
  `isCommitAfterSale` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否移交运维（0-未移交，1-已移交）',
  PRIMARY KEY (`projectId`) USING BTREE,
  UNIQUE KEY `projectNum` (`projectNum`) USING BTREE,
  KEY `idx_project_num` (`projectNum`) USING BTREE,
  KEY `idx_year` (`year`) USING BTREE,
  KEY `idx_project_state` (`projectState`) USING BTREE,
  KEY `idx_start_date` (`startDate`) USING BTREE,
  KEY `idx_project_leader` (`projectLeader`) USING BTREE,
  KEY `idx_sale_leader` (`saleLeader`) USING BTREE,
  KEY `idx_is_agent` (`isAgent`) USING BTREE,
  KEY `idx_project_value` (`value`) USING BTREE,
  KEY `idx_acceptance_date` (`acceptanceDate`) USING BTREE,
  KEY `idx_constructing_project_customer` (`customerId`) USING BTREE,
  KEY `idx_soft_id` (`softId`) USING BTREE,
  KEY `idx_channel_id` (`channelId`) USING BTREE,
  CONSTRAINT `fk_channel_id` FOREIGN KEY (`channelId`) REFERENCES `channel_distributor` (`channel_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_constructing_project_customer` FOREIGN KEY (`customerId`) REFERENCES `customer` (`customerId`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_project_leader` FOREIGN KEY (`projectLeader`) REFERENCES `user` (`userId`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_sale_leader` FOREIGN KEY (`saleLeader`) REFERENCES `user` (`userId`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_soft_id` FOREIGN KEY (`softId`) REFERENCES `archievesoft` (`softId`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='项目表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `constructing_project`
--

LOCK TABLES `constructing_project` WRITE;
/*!40000 ALTER TABLE `constructing_project` DISABLE KEYS */;
INSERT INTO `constructing_project` VALUES (1,'MS-20251211232738',2025,'北京市城建勘测设计院档案系统建设项目','软件开发',3,2,'进行中',1,'2025-10-13','2025-12-31',NULL,79,NULL,'2025-12-11 15:28:08','2025-12-24 15:44:52',0,NULL,NULL,0.00,0.00,NULL,1,'标准产品/接口开发/数据迁移/个性化功能开发/用户培训/系统上线试运行',0);
/*!40000 ALTER TABLE `constructing_project` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `customer`
--

DROP TABLE IF EXISTS `customer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `customer` (
  `customerId` bigint NOT NULL AUTO_INCREMENT COMMENT '客户ID，主键',
  `customerName` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '客户名称，必填',
  `contact` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '联系人，必填',
  `phoneNumber` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '联系方式，必填',
  `province` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '省份，必填',
  `customerRank` enum('战略客户','重要客户','一般客户') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '一般客户' COMMENT '客户等级，值域为战略客户、重要客户、一般客户',
  `createTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `saleLeader` bigint DEFAULT NULL COMMENT '销售负责人ID',
  `ifDeal` tinyint(1) DEFAULT '0' COMMENT '是否成交',
  `customerOwner` enum('自有客户','渠道客户') COLLATE utf8mb4_unicode_ci DEFAULT '自有客户' COMMENT '客户归属：自有客户、渠道客户',
  `channelId` bigint DEFAULT NULL COMMENT '渠道ID，关联channel_distributor表',
  PRIMARY KEY (`customerId`) USING BTREE,
  KEY `idx_customer_name` (`customerName`) USING BTREE,
  KEY `idx_customer_rank` (`customerRank`) USING BTREE,
  KEY `idx_customer_province` (`province`) USING BTREE,
  KEY `idx_customer_contact` (`contact`) USING BTREE,
  KEY `fk_customer_saleLeader` (`saleLeader`) USING BTREE,
  KEY `fk_customer_channel_distributor` (`channelId`),
  CONSTRAINT `fk_customer_channel_distributor` FOREIGN KEY (`channelId`) REFERENCES `channel_distributor` (`channel_id`),
  CONSTRAINT `fk_customer_saleLeader` FOREIGN KEY (`saleLeader`) REFERENCES `user` (`userId`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='客户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customer`
--

LOCK TABLES `customer` WRITE;
/*!40000 ALTER TABLE `customer` DISABLE KEYS */;
INSERT INTO `customer` VALUES (1,'北京城建勘测设计研究院有限责任公司','文菲菲','13488859794','北京','一般客户','2025-12-11 15:25:23','2025-12-13 14:35:32',2,0,'自有客户',NULL),(2,'西安音乐学院','李朝环','19909287338','陕西','一般客户','2025-12-12 03:48:48','2025-12-12 03:48:48',2,0,'自有客户',NULL),(3,'亚东县档案馆','尼珍馆长','13989928732','西藏','一般客户','2025-12-13 14:26:25','2025-12-13 14:26:25',2,0,'自有客户',NULL),(4,'乾县人民检察院','习攀娜','13700200757','陕西','一般客户','2025-12-13 14:28:22','2025-12-13 14:29:16',2,0,'自有客户',NULL),(5,'勉县人民检察院','李欣','19991357789','陕西','重要客户','2025-12-13 14:29:10','2025-12-13 14:30:44',11,0,'自有客户',NULL),(6,'北京市勘察设计研究院有限公司','王慧玲','13521808435','北京','一般客户','2025-12-13 14:35:12','2025-12-13 14:35:12',2,0,'自有客户',NULL),(7,'四川达陕高速公路有限责任公司','徐航','18228655128','四川','重要客户','2025-12-13 14:39:09','2025-12-13 14:39:09',2,0,'自有客户',NULL),(8,'四川省成南达铁路投资有限责任公司','覃诗益','18281530919','四川','一般客户','2025-12-13 14:41:55','2025-12-13 14:41:55',2,0,'自有客户',NULL),(9,'四川蜀道铁路运营管理集团有限责任公司','李丽华','15982828414','四川','一般客户','2025-12-13 14:44:48','2025-12-13 14:44:48',2,0,'自有客户',NULL),(10,'四川广安绕城高速公路有限责任公司','张楠欣','18382617801','四川','一般客户','2025-12-13 14:46:16','2025-12-13 14:46:16',2,0,'自有客户',NULL),(11,'四川川南高速公路有限责任公司','曾妍雯','18095069565','四川','一般客户','2025-12-13 14:47:33','2025-12-13 14:48:56',2,0,'自有客户',NULL),(12,'北控水务集团有限公司','杨会会','18201657370','北京','战略客户','2025-12-13 14:53:04','2025-12-13 14:53:04',10,0,'自有客户',NULL),(13,'晋城市城区档案馆','杨巍','13191161908','山西','重要客户','2025-12-13 15:14:06','2025-12-13 15:14:06',10,0,'自有客户',NULL),(14,'哈尔滨地铁集团','李震','17646632222','黑龙江','一般客户','2025-12-13 15:34:13','2025-12-13 15:34:13',10,0,'自有客户',NULL),(15,'岳阳市楼区档案馆','潘老师','18273028651','湖南','一般客户','2025-12-13 15:35:12','2025-12-13 15:35:12',10,0,'自有客户',NULL),(16,'陕西省社会科学院','温老师','17391811774','陕西','一般客户','2025-12-13 15:36:33','2025-12-13 15:36:33',2,0,'自有客户',NULL),(17,'‌山西省融资再担保集团有限公司','高部长','18103516262','山西','一般客户','2025-12-13 15:49:11','2025-12-13 15:49:11',10,0,'自有客户',NULL),(18,'北京市中关村集成电路设计园发展有限责任公司','张欣','15901029819','北京','一般客户','2025-12-13 15:52:23','2025-12-13 15:52:23',2,0,'自有客户',NULL),(19,'青海省委巡视办','荣老师','19909711236','青海','重要客户','2025-12-13 15:56:48','2025-12-13 15:56:48',10,0,'自有客户',NULL),(20,'华夏久盈资产管理有限责任公司','李跃丰','13691133511','北京','一般客户','2025-12-13 16:16:25','2025-12-16 01:57:29',10,0,'自有客户',NULL),(21,'陕西省信用再担保有限责任公司','田龙','13111111112','陕西','一般客户','2025-12-13 16:17:36','2025-12-13 16:17:36',11,0,'自有客户',NULL),(22,'勉县医用氧气厂','某某','13111111113','陕西','一般客户','2025-12-13 16:18:08','2025-12-13 16:18:08',11,0,'自有客户',NULL),(23,'四川汶马高速公路有限责任公司','庞老师','18982224692','四川','一般客户','2025-12-13 16:24:28','2025-12-13 16:24:28',2,0,'自有客户',NULL),(24,'古交市档案馆','高晓红','03515142750','山西','一般客户','2025-12-13 16:26:47','2025-12-13 16:26:47',2,0,'自有客户',NULL),(25,'成都市海关','不明','13111111114','四川','一般客户','2025-12-13 16:37:07','2025-12-18 14:26:50',2,1,'自有客户',NULL),(26,'四川广南高速公路有限责任公司','刘然','15692821001','四川','一般客户','2025-12-13 16:42:09','2025-12-13 16:45:20',2,0,'自有客户',NULL),(27,'四川攀西高速公路开发股份有限公司','杨老师','08342500693','四川','一般客户','2025-12-13 16:44:43','2025-12-13 16:44:43',2,0,'自有客户',NULL),(28,'四川泸渝高速公路开发有限责任公司','闫主任','0830-7771260','四川','一般客户','2025-12-13 16:58:51','2025-12-13 16:58:51',2,0,'自有客户',NULL),(29,'西安市空港新城管理委员会','吴楠','13636817674','陕西','一般客户','2025-12-16 01:35:23','2025-12-16 01:35:23',2,0,'自有客户',NULL),(30,'太原市城建档案馆','李科长','18935123492','山西','一般客户','2025-12-16 01:58:01','2025-12-18 14:06:22',2,1,'渠道客户',1);
/*!40000 ALTER TABLE `customer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `databaseBackup`
--

DROP TABLE IF EXISTS `databaseBackup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `databaseBackup` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `fileName` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '备份文件名',
  `backupDate` datetime NOT NULL COMMENT '备份时间',
  `backupState` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '备份状态',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_backup_date` (`backupDate`),
  KEY `idx_backup_state` (`backupState`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据库备份记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `databaseBackup`
--

LOCK TABLES `databaseBackup` WRITE;
/*!40000 ALTER TABLE `databaseBackup` DISABLE KEYS */;
/*!40000 ALTER TABLE `databaseBackup` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `interface`
--

DROP TABLE IF EXISTS `interface`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `interface` (
  `interfaceId` bigint NOT NULL AUTO_INCREMENT COMMENT '接口ID（主键）',
  `projectId` bigint DEFAULT NULL COMMENT '项目ID（外键）',
  `interfaceType` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接口类型（必填）',
  `integrationSysName` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '集成系统名称（必填）',
  `integrationSysManufacturer` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '集成系统厂商（必填）',
  `archieveDataCategory` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '归档数据类别',
  `archieveInterfaceImpl` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '归档接口实现方式',
  `milestoneId` bigint DEFAULT NULL COMMENT '里程碑ID（外键）',
  `createTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`interfaceId`) USING BTREE,
  KEY `idx_interface_project` (`projectId`) USING BTREE,
  KEY `idx_interface_type` (`interfaceType`) USING BTREE,
  KEY `idx_interface_milestone` (`milestoneId`) USING BTREE,
  KEY `idx_interface_integration_sys` (`integrationSysName`) USING BTREE,
  KEY `idx_interface_manufacturer` (`integrationSysManufacturer`) USING BTREE,
  CONSTRAINT `fk_interface_milestone` FOREIGN KEY (`milestoneId`) REFERENCES `construct_milestone` (`milestoneId`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_interface_project` FOREIGN KEY (`projectId`) REFERENCES `constructing_project` (`projectId`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='接口表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `interface`
--

LOCK TABLES `interface` WRITE;
/*!40000 ALTER TABLE `interface` DISABLE KEYS */;
INSERT INTO `interface` VALUES (1,1,'数据归档接口','OA','泛微',NULL,NULL,5,'2025-12-12 10:44:19','2025-12-12 10:44:19');
/*!40000 ALTER TABLE `interface` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `migration_history`
--

DROP TABLE IF EXISTS `migration_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `migration_history` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Migration ID',
  `version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Migration Version (e.g., 001, 002)',
  `script_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Script File Name',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT 'Migration Description',
  `checksum` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Script Content Checksum (MD5)',
  `executed_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Execution Time',
  `execution_time_ms` bigint DEFAULT NULL COMMENT 'Execution Time in Milliseconds',
  `success` tinyint(1) DEFAULT '1' COMMENT 'Execution Success Status',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT 'Error Message if Failed',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `version` (`version`) USING BTREE,
  KEY `idx_version` (`version`) USING BTREE,
  KEY `idx_executed_at` (`executed_at`) USING BTREE,
  KEY `idx_success` (`success`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=64 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='Database Migration History Table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `migration_history`
--

LOCK TABLES `migration_history` WRITE;
/*!40000 ALTER TABLE `migration_history` DISABLE KEYS */;
INSERT INTO `migration_history` VALUES (1,'001','001_create_migration_table.sql','Create migration history table for database version management',NULL,'2025-12-11 11:56:18',NULL,1,NULL),(2,'002','002_create_user_role_organ_tables.sql','Create user, role, organ tables with foreign key relationships','fd639560164f4f4b931cf13529e6b4f7','2025-12-11 11:56:19',194,1,NULL),(3,'008','008_create_constructing_project_table.sql','No description','96fafd2b65c4fe527ce604cb928000f4','2025-12-11 11:56:20',1022,1,NULL),(4,'010','010_add_constructing_project_fields.sql','No description','dbbd69f4e85b2410e0fa3a1c09168b8c','2025-12-11 11:56:21',804,1,NULL),(5,'011','011_create_customer_table.sql','No description','839b28df91608ceb31b51769c7d3e519','2025-12-11 11:56:21',172,1,NULL),(6,'012','012_create_afterservice_project_table.sql','No description','a0977e0b812e71707f807976f29a7422','2025-12-11 11:56:21',479,1,NULL),(7,'013','013_add_customer_id_to_constructing_project.sql','No description','f1640cb635622ef2bb14e8d6a1f5110b','2025-12-11 11:56:22',254,1,NULL),(8,'014','014_create_standard_construct_step_table.sql','No description','f9ff60c467ae68c73f2bfb7952735bdf','2025-12-11 11:56:22',352,1,NULL),(9,'015','015_create_construct_deliverable_table.sql','No description','dc5c9d9767f713a2cf02dadce6fa93da','2025-12-11 11:56:22',229,1,NULL),(10,'016','016_create_construct_milestone_table.sql','No description','f1ab3963a44fa2138f0c8c47965d0e7b','2025-12-11 11:56:22',109,1,NULL),(11,'017','017_create_nonstandard_construct_step_table.sql','No description','182cd93a412b81ab2c98b28703b2b941','2025-12-11 11:56:23',383,1,NULL),(12,'018','018_create_afterservice_event_table.sql','No description','6a28222d0663173bdb7d4fc293929260','2025-12-11 11:56:23',540,1,NULL),(13,'019','019_create_afterservice_deliverable_table.sql','No description','15231d144947a02abd0222b2869d4ac1','2025-12-11 11:56:24',517,1,NULL),(14,'020','020_create_project_relations_table.sql','No description','b7417b1e0d7f38c395d1bf8688dc9e42','2025-12-11 11:56:24',665,1,NULL),(15,'021','021_insert_admin_default_user.sql','Insert admin default user with initial password \'admin\'','d8d4bf1e72fd8aed0bbc528d88968fbe','2025-12-11 11:56:25',15,1,NULL),(16,'022','022_cleanup_user_table_extra_columns.sql','Remove extra columns that were added by Hibernate auto-ddl','e80b8ca9acc5e9445e9d1def9ff2faae','2025-12-11 11:56:25',41,1,NULL),(17,'023','023_update_admin_password.sql','Update admin user password to \'admin123\'','109267e1398b10a32c4f4e8d686b8538','2025-12-11 11:56:25',21,1,NULL),(18,'024','024_insert_default_organ.sql','Insert default top-level organization \"管软信息技术（北京）有限公司\"','49385f8aa2687abd7f39c18f637361d2','2025-12-11 11:56:25',22,1,NULL),(19,'025','025_add_role_description_column.sql','Align database schema with Role entity by adding optional description field','7208306eb26af8acecdf66937f15b37a','2025-12-11 11:56:25',55,1,NULL),(20,'026','026_insert_default_roles.sql','Insert default system roles for the application','078a3ccb21184c268e85e1f7804b64f4','2025-12-11 11:56:25',15,1,NULL),(21,'027','027_modify_standard_construct_step_table.sql','No description','7f2b96d7c741c8a4f5e4c602f69a8c1a','2025-12-11 11:56:25',392,1,NULL),(22,'028','028_modify_project_relations_table.sql','No description','690267df24a235abfcf0347f9965fcdf','2025-12-11 11:56:26',486,1,NULL),(23,'029','029_modify_construct_deliverable_table.sql','No description','bb3e46a7eb846d91ae3881e1d91bc9fb','2025-12-11 11:56:26',470,1,NULL),(24,'030','030_create_construct_deliverableFiles_table.sql','No description','efa8d295904c7194d2ad3d73a5d3e8e2','2025-12-11 11:56:27',568,1,NULL),(25,'031','031_modify_nonstandard_construct_step_table.sql','No description','3da18e9008ecb1ffd8d06286b52d298a','2025-12-11 11:56:27',204,1,NULL),(26,'032','032_modify_construct_milestone_table.sql','No description','35d0990447887328a8ce0c9831032c7e','2025-12-11 11:56:27',181,1,NULL),(27,'034','034_add_milestoneName_to_construct_milestone.sql','No description','420e76a907206036cf071f0f6500f7c0','2025-12-11 11:56:27',101,1,NULL),(28,'035','035_create_archieveSoft_table.sql','Create archieveSoft table for software archive management','1e9e24538d45ea9a9a30f0c871f33b1f','2025-12-11 11:56:27',37,1,NULL),(29,'036','036_add_timestamps_to_archieveSoft.sql','Add createTime and updateTime columns to archieveSoft table','2970d5fbb320de67e078ebb33b459fed','2025-12-11 11:56:27',70,1,NULL),(30,'037','037_create_channel_distributor_table.sql','No description','734ada521e0d7aef21bac9d254fc0ac1','2025-12-11 11:56:27',103,1,NULL),(31,'038','038_add_timestamps_to_missing_tables.sql','Add createTime and updateTime fields to tables that are missing them','51f9a45227fc94fbc7424dddf92ffd47','2025-12-11 11:56:28',465,1,NULL),(32,'039','039_create_standard_milestone_table.sql','No description','a228a89a6b4dec7ce3879b1eb7fd8606','2025-12-11 11:56:28',44,1,NULL),(33,'040','040_add_systemName_to_standard_construct_step.sql','No description','bc034b990ea77ee33668b860ac86f3c0','2025-12-11 11:56:28',113,1,NULL),(34,'041','041_add_smilestoneId_to_standard_construct_step.sql','No description','703eab8babf6eec0f5b3ec9b8339ac75','2025-12-11 11:56:28',228,1,NULL),(35,'042','042_add_smilestoneId_to_nonstandard_construct_step.sql','No description','9e8b407e1831251a5f41c98e10ae505b','2025-12-11 11:56:29',212,1,NULL),(36,'043','043_create_interface_table.sql','No description','ed4179a4127f67e753c27bfc10e2afad','2025-12-11 11:56:29',433,1,NULL),(37,'044','044_rename_construct_standard_deliverable_to_standard_deliverable.sql','No description','ca9e84d965c368c3f1b3d0b100aeaf56','2025-12-11 11:56:29',180,1,NULL),(38,'045','045_add_deliverableType_to_standard_deliverable.sql','No description','40cc44c9a8848caca052f3865c35e9d1','2025-12-11 11:56:29',111,1,NULL),(39,'046','046_fix_standard_deliverable_milestone_foreign_key.sql','No description','a6d12d8ff59a74bf1918e2ee284c9512','2025-12-11 11:56:30',260,1,NULL),(40,'047','047_modify_constructing_project_soft_channel_foreign_keys.sql','No description','abc6bba8b58e52c2150b826b3fe2ce47','2025-12-11 11:56:30',827,1,NULL),(41,'048','048_add_construct_content_to_constructing_project.sql','No description','71f39ad6e5b821d7f10a0816aa91adeb','2025-12-11 11:56:30',86,1,NULL),(42,'049','049_add_softType_to_archieveSoft.sql','Add 产品类型 (softType) field to archieveSoft (base product module)','d0e0687bd079f61341f96acfd5db7d54','2025-12-11 11:56:31',91,1,NULL),(43,'050','050_add_deliveryTempletePath_to_standard_deliverable.sql','No description','2059f946988480ff6ddb9e6e1b6f8dbd','2025-12-11 11:56:31',72,1,NULL),(44,'051','051_add_stepStatus_to_project_sstep_relations.sql','No description','2a7536fbf1bbd225daa47ff8f9bda944','2025-12-11 11:56:31',64,1,NULL),(45,'052','052_set_default_stepStatus.sql','No description','f599c90343032b55dac5db4536480367','2025-12-11 11:56:31',25,1,NULL),(46,'053','053_remove_sstepId_from_interface.sql','No description','77247a705ec94bc316247f8ccdffdbba','2025-12-11 11:56:31',120,1,NULL),(47,'054','054_create_personal_develope_table.sql','No description','f31d18f52c0fcea7b4f399dbb2338833','2025-12-11 11:56:31',212,1,NULL),(48,'055','055_add_interfaceId_personalDevId_to_project_sstep_relations.sql','No description','5df399b77da6d5ddc7fcb359d327eaf8','2025-12-11 11:56:31',295,1,NULL),(49,'056','056_add_milestoneId_to_project_sstep_relations.sql','No description','2f0b8aa227e56bccc8750ad8a7817255','2025-12-11 11:56:32',147,1,NULL),(50,'057','057_add_milestoneId_to_construct_deliverableFiles.sql','No description','9c2b6baa57224dc9aeae3dd82b970355','2025-12-11 11:56:32',234,1,NULL),(51,'058','058_modify_construct_deliverableFiles_sstepId_to_projectStepId.sql','No description','85aadee9d25fe4a560fab3e98d95372b','2025-12-11 11:56:32',233,1,NULL),(52,'059','059_add_customerId_to_afterservice_project.sql','No description','37c93fc628dc9625a9e5e335a5236518','2025-12-11 11:56:32',155,1,NULL),(53,'060','060_add_saleLeader_to_customer.sql','No description','4c315895e715f42e6095a4d609950c4a','2025-12-11 11:56:32',141,1,NULL),(54,'061','061_rename_director_to_saleDirector_in_afterservice_project.sql','No description','dd9d27b718f31872bf38b846c4949b6c','2025-12-11 11:56:33',257,1,NULL),(55,'062','062_add_eventDetails_to_afterservice_event.sql','No description','67e689f1e4da3655536caaf6a94992b3','2025-12-11 11:56:33',64,1,NULL),(56,'063','063_add_projectNum_to_afterservice_project.sql','No description','d2d4b88787634140d2b4c12e574e85ae','2025-12-11 11:56:33',181,1,NULL),(57,'064','064_add_isComplete_to_afterservice_event.sql','No description','141015611b32d30d66bf67998fb82dfa','2025-12-11 11:56:33',46,1,NULL),(58,'065','065_update_afterservice_project_table.sql','No description','1f720f8c5187c8b5158ae5a50cf10122','2025-12-11 11:56:33',276,1,NULL),(59,'066','066_add_constructingProjectId_to_afterservice_project.sql','No description','294e3a2b8b11e32d1915d9ce45580e87','2025-12-11 11:56:33',216,1,NULL),(60,'067','067_add_isCommitAfterSale_to_constructing_project.sql','No description','a558f68ee693820c89f0c153717e1958','2025-12-11 11:56:34',106,1,NULL),(61,'068','068_add_channel_fields_to_customer.sql','No description','b36f9a066b122be6669e62277913e251','2025-12-18 13:52:49',52,1,NULL),(62,'069','069_add_saleDirector_to_channel_distributor.sql','No description','418de880eeea1642cca17c127eae2ba5','2025-12-27 14:51:58',57,1,NULL),(63,'070','070_create_databaseBackup_table.sql','No description','801652701c9ff0e397efa1f71fafb497','2025-12-28 10:55:23',54,1,NULL);
/*!40000 ALTER TABLE `migration_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `nonstandard_construct_step`
--

DROP TABLE IF EXISTS `nonstandard_construct_step`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `nonstandard_construct_step` (
  `nstepId` bigint NOT NULL AUTO_INCREMENT COMMENT '非标准步骤ID，主键',
  `nstepName` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '非标准步骤名，必填',
  `smilestoneId` bigint DEFAULT NULL COMMENT '标准里程碑ID（外键）',
  `projectId` bigint DEFAULT NULL COMMENT '项目ID，constructing_project表外键',
  `director` bigint NOT NULL COMMENT '负责人，user表外键，必填',
  `planStartDate` date NOT NULL COMMENT '计划开始日期，必填',
  `planEndDate` date NOT NULL COMMENT '计划结束日期，必填',
  `actualStartDate` date DEFAULT NULL COMMENT '实际开始日期',
  `actualEndDate` date DEFAULT NULL COMMENT '实际结束日期',
  `planPeriod` int DEFAULT NULL COMMENT '计划工期（天数）',
  `actualPeriod` int DEFAULT NULL COMMENT '实际工期（天数）',
  `createTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`nstepId`) USING BTREE,
  KEY `idx_nonstandard_step_name` (`nstepName`) USING BTREE,
  KEY `idx_nonstandard_step_director` (`director`) USING BTREE,
  KEY `idx_nonstandard_step_plan_start` (`planStartDate`) USING BTREE,
  KEY `idx_nonstandard_step_plan_end` (`planEndDate`) USING BTREE,
  KEY `idx_nonstandard_step_project` (`projectId`) USING BTREE,
  KEY `idx_nonstandard_step_milestone` (`smilestoneId`) USING BTREE,
  CONSTRAINT `fk_nonstandard_step_director` FOREIGN KEY (`director`) REFERENCES `user` (`userId`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_nonstandard_step_milestone` FOREIGN KEY (`smilestoneId`) REFERENCES `standard_milestone` (`milestone_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_nonstandard_step_project` FOREIGN KEY (`projectId`) REFERENCES `constructing_project` (`projectId`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='非标准步骤表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `nonstandard_construct_step`
--

LOCK TABLES `nonstandard_construct_step` WRITE;
/*!40000 ALTER TABLE `nonstandard_construct_step` DISABLE KEYS */;
/*!40000 ALTER TABLE `nonstandard_construct_step` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `organ`
--

DROP TABLE IF EXISTS `organ`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `organ` (
  `organId` bigint NOT NULL AUTO_INCREMENT COMMENT 'Organization ID',
  `organName` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Organization Name',
  `parentOrganId` bigint DEFAULT NULL COMMENT 'Parent Organization ID',
  `path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Organization Path',
  `createTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`organId`) USING BTREE,
  KEY `idx_organ_name` (`organName`) USING BTREE,
  KEY `idx_parent_organ` (`parentOrganId`) USING BTREE,
  KEY `idx_organ_create_time` (`createTime`) USING BTREE,
  KEY `idx_organ_update_time` (`updateTime`) USING BTREE,
  CONSTRAINT `organ_ibfk_1` FOREIGN KEY (`parentOrganId`) REFERENCES `organ` (`organId`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='Organization Table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `organ`
--

LOCK TABLES `organ` WRITE;
/*!40000 ALTER TABLE `organ` DISABLE KEYS */;
INSERT INTO `organ` VALUES (1,'管软信息技术（北京）有限公司',NULL,'/1','2025-12-11 11:56:27','2025-12-11 11:56:27'),(2,'销售部',1,'/1/1','2025-12-11 15:25:51','2025-12-11 15:25:51'),(3,'项目部',1,'/1/1','2025-12-11 15:25:58','2025-12-11 15:25:58'),(4,'研发部',1,'/1/1','2025-12-11 15:26:03','2025-12-11 15:26:03'),(5,'产品部',1,'/1/1','2025-12-11 15:26:11','2025-12-11 15:26:11'),(6,'外部交付',1,'/1/1','2025-12-11 15:27:16','2025-12-11 15:27:16');
/*!40000 ALTER TABLE `organ` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `personal_develope`
--

DROP TABLE IF EXISTS `personal_develope`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `personal_develope` (
  `personalDevId` bigint NOT NULL AUTO_INCREMENT COMMENT '个性化开发ID（主键）',
  `personalDevName` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '个性化开发名称',
  `projectId` bigint DEFAULT NULL COMMENT '项目ID（constructing_project外键）',
  `milestoneId` bigint DEFAULT NULL COMMENT '里程碑ID（construct_milestone外键）',
  PRIMARY KEY (`personalDevId`) USING BTREE,
  KEY `idx_personal_develope_project` (`projectId`) USING BTREE,
  KEY `idx_personal_develope_milestone` (`milestoneId`) USING BTREE,
  KEY `idx_personal_develope_name` (`personalDevName`) USING BTREE,
  CONSTRAINT `fk_personal_develope_milestone` FOREIGN KEY (`milestoneId`) REFERENCES `construct_milestone` (`milestoneId`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_personal_develope_project` FOREIGN KEY (`projectId`) REFERENCES `constructing_project` (`projectId`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='个性化开发表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `personal_develope`
--

LOCK TABLES `personal_develope` WRITE;
/*!40000 ALTER TABLE `personal_develope` DISABLE KEYS */;
INSERT INTO `personal_develope` VALUES (1,'用印流程',1,6);
/*!40000 ALTER TABLE `personal_develope` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `project_sstep_relations`
--

DROP TABLE IF EXISTS `project_sstep_relations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `project_sstep_relations` (
  `relationId` bigint NOT NULL AUTO_INCREMENT COMMENT '关系ID，主键',
  `projectId` bigint DEFAULT NULL COMMENT '项目ID，constructing_project表外键',
  `sstepId` bigint DEFAULT NULL COMMENT '标准步骤ID，standard_construct_step表外键',
  `deliverableId` bigint DEFAULT NULL COMMENT '交付物ID，construct_deliverable表外键',
  `director` bigint DEFAULT NULL COMMENT '负责人，user表外键',
  `planStartDate` date DEFAULT NULL COMMENT '计划开始日期',
  `planEndDate` date DEFAULT NULL COMMENT '计划结束日期',
  `actualStartDate` date DEFAULT NULL COMMENT '实际开始日期',
  `actualEndDate` date DEFAULT NULL COMMENT '实际结束日期',
  `planPeriod` int DEFAULT NULL COMMENT '计划工期（天数）',
  `actualPeriod` int DEFAULT NULL COMMENT '实际工期（天数）',
  `stepStatus` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '未开始' COMMENT '步骤状态（未开始/进行中/已完成）',
  `interfaceId` bigint DEFAULT NULL COMMENT '接口ID（interface表外键）',
  `personalDevId` bigint DEFAULT NULL COMMENT '个性化开发ID（personal_develope表外键）',
  `createTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `milestoneId` bigint DEFAULT NULL COMMENT '项目里程碑ID，construct_milestone表外键',
  PRIMARY KEY (`relationId`) USING BTREE,
  KEY `idx_project_relations_project` (`projectId`) USING BTREE,
  KEY `idx_project_relations_sstep` (`sstepId`) USING BTREE,
  KEY `idx_project_relations_deliverable` (`deliverableId`) USING BTREE,
  KEY `idx_project_sstep_relations_director` (`director`) USING BTREE,
  KEY `idx_project_sstep_relations_plan_start` (`planStartDate`) USING BTREE,
  KEY `idx_project_sstep_relations_plan_end` (`planEndDate`) USING BTREE,
  KEY `idx_project_sstep_relations_interface` (`interfaceId`) USING BTREE,
  KEY `idx_project_sstep_relations_personal_dev` (`personalDevId`) USING BTREE,
  KEY `idx_psr_milestoneId` (`milestoneId`) USING BTREE,
  CONSTRAINT `fk_project_relations_deliverable` FOREIGN KEY (`deliverableId`) REFERENCES `standard_deliverable` (`deliverableId`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_project_relations_project` FOREIGN KEY (`projectId`) REFERENCES `constructing_project` (`projectId`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_project_relations_sstep` FOREIGN KEY (`sstepId`) REFERENCES `standard_construct_step` (`sstepId`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_project_sstep_relations_director` FOREIGN KEY (`director`) REFERENCES `user` (`userId`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_project_sstep_relations_interface` FOREIGN KEY (`interfaceId`) REFERENCES `interface` (`interfaceId`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_project_sstep_relations_personal_dev` FOREIGN KEY (`personalDevId`) REFERENCES `personal_develope` (`personalDevId`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_psr_milestone` FOREIGN KEY (`milestoneId`) REFERENCES `construct_milestone` (`milestoneId`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=88 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='项目步骤关系表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `project_sstep_relations`
--

LOCK TABLES `project_sstep_relations` WRITE;
/*!40000 ALTER TABLE `project_sstep_relations` DISABLE KEYS */;
INSERT INTO `project_sstep_relations` VALUES (1,1,1,NULL,3,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-11 15:28:09','2025-12-11 15:28:09',1),(2,1,2,NULL,3,NULL,NULL,'2025-12-12','2025-12-13',NULL,2,'已完成',NULL,NULL,'2025-12-11 15:28:09','2025-12-12 10:46:18',2),(3,1,3,NULL,3,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-11 15:28:09','2025-12-11 15:28:09',2),(4,1,7,NULL,3,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-11 15:28:09','2025-12-11 15:28:09',3),(5,1,8,NULL,3,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-11 15:28:09','2025-12-11 15:28:09',3),(6,1,9,NULL,3,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-11 15:28:09','2025-12-11 15:28:09',3),(7,1,10,NULL,3,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-11 15:28:09','2025-12-11 15:28:09',3),(8,1,11,NULL,3,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-11 15:28:09','2025-12-11 15:28:09',3),(9,1,12,NULL,3,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-11 15:28:09','2025-12-11 15:28:09',3),(10,1,13,NULL,3,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-11 15:28:09','2025-12-11 15:28:09',3),(11,1,14,NULL,3,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-11 15:28:09','2025-12-11 15:28:09',3),(12,1,15,NULL,3,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-11 15:28:09','2025-12-11 15:28:09',3),(13,1,16,NULL,3,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-11 15:28:09','2025-12-11 15:28:09',3),(14,1,17,NULL,3,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-11 15:28:09','2025-12-11 15:28:09',3),(15,1,18,NULL,3,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-11 15:28:09','2025-12-11 15:28:09',3),(16,1,19,NULL,3,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-11 15:28:09','2025-12-11 15:28:09',3),(17,1,20,NULL,3,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-11 15:28:09','2025-12-11 15:28:09',3),(18,1,21,NULL,3,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-11 15:28:09','2025-12-11 15:28:09',3),(19,1,22,NULL,3,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-11 15:28:09','2025-12-11 15:28:09',3),(20,1,23,NULL,3,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-11 15:28:09','2025-12-11 15:28:09',3),(21,1,24,NULL,3,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-11 15:28:09','2025-12-11 15:28:09',3),(22,1,25,NULL,3,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-11 15:28:09','2025-12-11 15:28:09',3),(23,1,26,NULL,3,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-11 15:28:09','2025-12-11 15:28:09',3),(24,1,27,NULL,3,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-11 15:28:09','2025-12-11 15:28:09',3),(25,1,28,NULL,3,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-11 15:28:09','2025-12-11 15:28:09',3),(26,1,29,NULL,3,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-11 15:28:09','2025-12-11 15:28:09',3),(27,1,4,NULL,3,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-11 15:28:09','2025-12-11 15:28:09',2),(28,1,30,NULL,3,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-11 15:28:09','2025-12-11 15:28:09',4),(29,1,31,NULL,3,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-11 15:28:09','2025-12-11 15:28:09',4),(30,1,32,NULL,3,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-11 15:28:09','2025-12-11 15:28:09',4),(31,1,33,NULL,3,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-11 15:28:09','2025-12-11 15:28:09',4),(32,1,44,NULL,3,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-11 15:28:09','2025-12-11 15:28:09',7),(33,1,43,NULL,3,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-11 15:28:09','2025-12-11 15:28:09',7),(34,1,45,NULL,3,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-11 15:28:09','2025-12-11 15:28:09',8),(35,1,46,NULL,3,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-11 15:28:09','2025-12-11 15:28:09',9),(36,1,5,NULL,3,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-11 15:28:09','2025-12-11 15:28:09',2),(37,1,6,NULL,3,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-11 15:28:09','2025-12-11 15:28:09',2),(38,1,34,NULL,3,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',1,NULL,'2025-12-12 10:44:20','2025-12-12 10:44:20',5),(39,1,35,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',1,NULL,'2025-12-12 10:44:20','2025-12-12 10:44:20',5),(40,1,36,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',1,NULL,'2025-12-12 10:44:20','2025-12-12 10:44:20',5),(41,1,37,NULL,3,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',1,NULL,'2025-12-12 10:44:20','2025-12-12 10:44:20',5),(42,1,38,NULL,3,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,1,'2025-12-12 10:44:38','2025-12-12 10:44:38',6),(43,1,39,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,1,'2025-12-12 10:44:38','2025-12-12 10:44:38',6),(44,1,40,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,1,'2025-12-12 10:44:38','2025-12-12 10:44:38',6),(45,1,41,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,1,'2025-12-12 10:44:38','2025-12-12 10:44:38',6),(46,1,42,NULL,3,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,1,'2025-12-12 10:44:38','2025-12-12 10:44:38',6);
/*!40000 ALTER TABLE `project_sstep_relations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `role`
--

DROP TABLE IF EXISTS `role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `role` (
  `roleId` bigint NOT NULL AUTO_INCREMENT COMMENT 'Role ID',
  `roleName` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Role Name',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT 'Role Description',
  `createTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`roleId`) USING BTREE,
  KEY `idx_role_name` (`roleName`) USING BTREE,
  KEY `idx_role_create_time` (`createTime`) USING BTREE,
  KEY `idx_role_update_time` (`updateTime`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='Role Table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `role`
--

LOCK TABLES `role` WRITE;
/*!40000 ALTER TABLE `role` DISABLE KEYS */;
INSERT INTO `role` VALUES (1,'管理员','管理员角色，拥有系统管理权限','2025-12-11 11:56:27','2025-12-11 11:56:27'),(2,'销售角色','销售人员角色，负责客户开发、项目销售等业务','2025-12-11 11:56:27','2025-12-11 11:56:27'),(3,'售后角色','售后服务人员角色，负责项目维护、客户服务等工作','2025-12-11 11:56:27','2025-12-11 11:56:27'),(4,'项目经理角色','项目经理角色，负责项目的规划、执行和管理','2025-12-11 11:56:27','2025-12-11 11:56:27'),(5,'销售总监角色','销售总监角色，负责销售团队管理和销售策略制定','2025-12-11 11:56:27','2025-12-11 11:56:27'),(6,'项目总监角色','项目总监角色，负责项目团队管理和项目战略规划','2025-12-11 11:56:27','2025-12-11 11:56:27'),(7,'公司领导角色','公司领导角色，拥有公司级别的决策和管理权限','2025-12-11 11:56:27','2025-12-11 11:56:27');
/*!40000 ALTER TABLE `role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `standard_construct_step`
--

DROP TABLE IF EXISTS `standard_construct_step`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `standard_construct_step` (
  `sstepId` bigint NOT NULL AUTO_INCREMENT COMMENT '标准步骤ID，主键',
  `sstepName` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标准步骤名，必填',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '步骤类型',
  `systemName` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '档案系统名称',
  `smilestoneId` bigint DEFAULT NULL COMMENT '标准里程碑ID（外键）',
  `createTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`sstepId`) USING BTREE,
  KEY `idx_standard_step_name` (`sstepName`) USING BTREE,
  KEY `idx_standard_step_type` (`type`) USING BTREE,
  KEY `idx_standard_step_system_name` (`systemName`) USING BTREE,
  KEY `idx_standard_step_milestone` (`smilestoneId`) USING BTREE,
  CONSTRAINT `fk_standard_step_milestone` FOREIGN KEY (`smilestoneId`) REFERENCES `standard_milestone` (`milestone_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=173 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='标准步骤表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `standard_construct_step`
--

LOCK TABLES `standard_construct_step` WRITE;
/*!40000 ALTER TABLE `standard_construct_step` DISABLE KEYS */;
INSERT INTO `standard_construct_step` VALUES (1,'01召开项目启动会','标准产品','DAIS数字档案馆（室）一体化系统',1,'2025-12-11 12:05:01','2025-12-11 12:05:01'),(2,'02系统应用规模及部署需求调研','标准产品','DAIS数字档案馆（室）一体化系统',2,'2025-12-11 12:05:01','2025-12-11 12:05:01'),(3,'03档案业务现状及需求调研','标准产品','DAIS数字档案馆（室）一体化系统',2,'2025-12-11 12:05:01','2025-12-11 12:05:01'),(4,'04历史数据迁移需求调研','数据迁移','DAIS数字档案馆（室）一体化系统',2,'2025-12-11 12:05:01','2025-12-11 12:05:01'),(5,'05业务系统接口需求调研','接口开发','DAIS数字档案馆（室）一体化系统',2,'2025-12-11 12:05:01','2025-12-11 12:05:01'),(6,'06个性化功能需求调研','个性化功能开发','DAIS数字档案馆（室）一体化系统',2,'2025-12-11 12:05:01','2025-12-11 12:05:01'),(7,'07系统安装部署','标准产品','DAIS数字档案馆（室）一体化系统',3,'2025-12-11 12:05:01','2025-12-11 12:05:01'),(8,'08系统参数配置','标准产品','DAIS数字档案馆（室）一体化系统',3,'2025-12-11 12:05:01','2025-12-11 12:05:01'),(9,'09存储配置','标准产品','DAIS数字档案馆（室）一体化系统',3,'2025-12-11 12:05:01','2025-12-11 12:05:01'),(10,'10导入通用初始化库','标准产品','DAIS数字档案馆（室）一体化系统',3,'2025-12-11 12:05:01','2025-12-11 12:05:01'),(11,'11设置全宗','标准产品','DAIS数字档案馆（室）一体化系统',3,'2025-12-11 12:05:01','2025-12-11 12:05:01'),(12,'12设置门类结构、规则及字典','标准产品','DAIS数字档案馆（室）一体化系统',3,'2025-12-11 12:05:01','2025-12-11 12:05:01'),(13,'13设置档案门类','标准产品','DAIS数字档案馆（室）一体化系统',3,'2025-12-11 12:05:01','2025-12-11 12:05:01'),(14,'14关联门类结构并检查门类规则','标准产品','DAIS数字档案馆（室）一体化系统',3,'2025-12-11 12:05:01','2025-12-11 12:05:01'),(15,'15设置（同步）组织机构','标准产品','DAIS数字档案馆（室）一体化系统',3,'2025-12-11 12:05:01','2025-12-11 12:05:01'),(16,'16设置（同步）用户','标准产品','DAIS数字档案馆（室）一体化系统',3,'2025-12-11 12:05:01','2025-12-11 12:05:01'),(17,'17设置角色权限及用户赋权','标准产品','DAIS数字档案馆（室）一体化系统',3,'2025-12-11 12:05:01','2025-12-11 12:05:01'),(18,'18设置档案利用表单及流程','标准产品','DAIS数字档案馆（室）一体化系统',3,'2025-12-11 12:05:01','2025-12-11 12:05:01'),(19,'19设置档案移交归档表单及流程','标准产品','DAIS数字档案馆（室）一体化系统',3,'2025-12-11 12:05:01','2025-12-11 12:05:01'),(20,'20设置其他表单及流程（如需要）','标准产品','DAIS数字档案馆（室）一体化系统',3,'2025-12-11 12:05:01','2025-12-11 12:05:01'),(21,'21设置数据报表','标准产品','DAIS数字档案馆（室）一体化系统',3,'2025-12-11 12:05:01','2025-12-11 12:05:01'),(22,'22设置档案资源统计、移交接收统计、档案利用统计','标准产品','DAIS数字档案馆（室）一体化系统',3,'2025-12-11 12:05:02','2025-12-11 12:05:02'),(23,'23配置手动登记借阅模板','标准产品','DAIS数字档案馆（室）一体化系统',3,'2025-12-11 12:05:02','2025-12-11 12:05:02'),(24,'24配置“三合一”表（如需要）','标准产品','DAIS数字档案馆（室）一体化系统',3,'2025-12-11 12:05:02','2025-12-11 12:05:02'),(25,'25配置水印','标准产品','DAIS数字档案馆（室）一体化系统',3,'2025-12-11 12:05:02','2025-12-11 12:05:02'),(26,'26配置备份策略','标准产品','DAIS数字档案馆（室）一体化系统',3,'2025-12-11 12:05:02','2025-12-11 12:05:02'),(27,'27配置四性检测（如需要）','标准产品','DAIS数字档案馆（室）一体化系统',3,'2025-12-11 12:05:02','2025-12-11 12:05:02'),(28,'28完成其他功能设置（如需要，例如发布管理、库房配置、菜单管理）','标准产品','DAIS数字档案馆（室）一体化系统',3,'2025-12-11 12:05:02','2025-12-11 12:05:02'),(29,'29标准产品整体功能跑通确认及调整','标准产品','DAIS数字档案馆（室）一体化系统',3,'2025-12-11 12:05:02','2025-12-11 12:05:02'),(30,'30数据迁移技术方式确定','数据迁移','DAIS数字档案馆（室）一体化系统',4,'2025-12-11 12:05:02','2025-12-11 12:05:02'),(31,'31执行数据迁移','数据迁移','DAIS数字档案馆（室）一体化系统',4,'2025-12-11 12:05:02','2025-12-11 12:05:02'),(32,'32数据迁移后核验','数据迁移','DAIS数字档案馆（室）一体化系统',4,'2025-12-11 12:05:02','2025-12-11 12:05:02'),(33,'33存量数据创建全文索引，并开启增量索引任务','数据迁移','DAIS数字档案馆（室）一体化系统',4,'2025-12-11 12:05:02','2025-12-11 12:05:02'),(34,'34接口需求及集成方式最终确定','接口开发','DAIS数字档案馆（室）一体化系统',5,'2025-12-11 12:05:02','2025-12-11 12:05:02'),(35,'35接口开发','接口开发','DAIS数字档案馆（室）一体化系统',5,'2025-12-11 12:05:02','2025-12-11 12:05:02'),(36,'36接口联调测试','接口开发','DAIS数字档案馆（室）一体化系统',5,'2025-12-11 12:05:02','2025-12-11 12:05:02'),(37,'37接口部署','接口开发','DAIS数字档案馆（室）一体化系统',5,'2025-12-11 12:05:02','2025-12-11 12:05:02'),(38,'38个性化需求整体最终确定','个性化功能开发','DAIS数字档案馆（室）一体化系统',6,'2025-12-11 12:05:02','2025-12-11 12:05:02'),(39,'39个性化需求整体设计','个性化功能开发','DAIS数字档案馆（室）一体化系统',6,'2025-12-11 12:05:02','2025-12-11 12:05:02'),(40,'40个性化需求开发','个性化功能开发','DAIS数字档案馆（室）一体化系统',6,'2025-12-11 12:05:02','2025-12-11 12:05:02'),(41,'41个性化需求测试验证','个性化功能开发','DAIS数字档案馆（室）一体化系统',6,'2025-12-11 12:05:02','2025-12-11 12:05:02'),(42,'42个性化需求部署及用户验证','个性化功能开发','DAIS数字档案馆（室）一体化系统',6,'2025-12-11 12:05:02','2025-12-11 12:05:02'),(43,'43系统上线前功能调整及其他准备工作','系统上线试运行','DAIS数字档案馆（室）一体化系统',7,'2025-12-11 12:05:02','2025-12-11 12:05:02'),(44,'44用户培训','用户培训','DAIS数字档案馆（室）一体化系统',7,'2025-12-11 12:05:02','2025-12-11 12:05:02'),(45,'45系统正式上线','系统上线试运行','DAIS数字档案馆（室）一体化系统',8,'2025-12-11 12:05:02','2025-12-11 12:05:02'),(46,'46项目正式验收','系统上线试运行','DAIS数字档案馆（室）一体化系统',9,'2025-12-11 12:05:02','2025-12-11 12:05:02'),(47,'01召开项目启动会','标准产品','CAMS综合档案管理系统专业版',1,'2025-12-11 12:05:09','2025-12-11 12:05:09'),(48,'02系统应用规模及部署需求调研','标准产品','CAMS综合档案管理系统专业版',2,'2025-12-11 12:05:09','2025-12-11 12:05:09'),(49,'03档案业务现状及需求调研','标准产品','CAMS综合档案管理系统专业版',2,'2025-12-11 12:05:09','2025-12-11 12:05:09'),(50,'04历史数据迁移需求调研','数据迁移','CAMS综合档案管理系统专业版',2,'2025-12-11 12:05:09','2025-12-11 12:05:09'),(51,'05业务系统接口需求调研','接口开发','CAMS综合档案管理系统专业版',2,'2025-12-11 12:05:09','2025-12-11 12:05:09'),(52,'06个性化功能需求调研','个性化功能开发','CAMS综合档案管理系统专业版',2,'2025-12-11 12:05:09','2025-12-11 12:05:09'),(53,'07系统安装部署','标准产品','CAMS综合档案管理系统专业版',3,'2025-12-11 12:05:09','2025-12-11 12:05:09'),(54,'08系统参数配置','标准产品','CAMS综合档案管理系统专业版',3,'2025-12-11 12:05:09','2025-12-11 12:05:09'),(55,'09存储配置','标准产品','CAMS综合档案管理系统专业版',3,'2025-12-11 12:05:09','2025-12-11 12:05:09'),(56,'10导入通用初始化库','标准产品','CAMS综合档案管理系统专业版',3,'2025-12-11 12:05:09','2025-12-11 12:05:09'),(57,'11设置全宗','标准产品','CAMS综合档案管理系统专业版',3,'2025-12-11 12:05:09','2025-12-11 12:05:09'),(58,'12设置门类结构、规则及字典','标准产品','CAMS综合档案管理系统专业版',3,'2025-12-11 12:05:09','2025-12-11 12:05:09'),(59,'13设置档案门类','标准产品','CAMS综合档案管理系统专业版',3,'2025-12-11 12:05:09','2025-12-11 12:05:09'),(60,'14关联门类结构并检查门类规则','标准产品','CAMS综合档案管理系统专业版',3,'2025-12-11 12:05:09','2025-12-11 12:05:09'),(61,'15设置（同步）组织机构','标准产品','CAMS综合档案管理系统专业版',3,'2025-12-11 12:05:09','2025-12-11 12:05:09'),(62,'16设置（同步）用户','标准产品','CAMS综合档案管理系统专业版',3,'2025-12-11 12:05:09','2025-12-11 12:05:09'),(63,'17设置角色权限及用户赋权','标准产品','CAMS综合档案管理系统专业版',3,'2025-12-11 12:05:10','2025-12-11 12:05:10'),(64,'18设置档案利用审核人','标准产品','CAMS综合档案管理系统专业版',3,'2025-12-11 12:05:10','2025-12-11 12:05:10'),(65,'19设置档案归档审核人','标准产品','CAMS综合档案管理系统专业版',3,'2025-12-11 12:05:10','2025-12-11 12:05:10'),(66,'20设置数据报表','标准产品','CAMS综合档案管理系统专业版',3,'2025-12-11 12:05:10','2025-12-11 12:05:10'),(67,'21设置档案资源统计、移交接收统计、档案利用统计','标准产品','CAMS综合档案管理系统专业版',3,'2025-12-11 12:05:10','2025-12-11 12:05:10'),(68,'22配置手动登记借阅模板','标准产品','CAMS综合档案管理系统专业版',3,'2025-12-11 12:05:10','2025-12-11 12:05:10'),(69,'23配置“三合一”表（如需要）','标准产品','CAMS综合档案管理系统专业版',3,'2025-12-11 12:05:10','2025-12-11 12:05:10'),(70,'24配置水印','标准产品','CAMS综合档案管理系统专业版',3,'2025-12-11 12:05:10','2025-12-11 12:05:10'),(71,'25配置备份策略','标准产品','CAMS综合档案管理系统专业版',3,'2025-12-11 12:05:10','2025-12-11 12:05:10'),(72,'26配置四性检测（如需要）','标准产品','CAMS综合档案管理系统专业版',3,'2025-12-11 12:05:10','2025-12-11 12:05:10'),(73,'27完成其他功能设置（如需要，例如库房配置、菜单管理、销毁业务等）','标准产品','CAMS综合档案管理系统专业版',3,'2025-12-11 12:05:10','2025-12-11 12:05:10'),(74,'28标准产品整体功能跑通确认及调整','标准产品','CAMS综合档案管理系统专业版',3,'2025-12-11 12:05:10','2025-12-11 12:05:10'),(75,'29数据迁移技术方式确定','数据迁移','CAMS综合档案管理系统专业版',4,'2025-12-11 12:05:10','2025-12-11 12:05:10'),(76,'30执行数据迁移','数据迁移','CAMS综合档案管理系统专业版',4,'2025-12-11 12:05:10','2025-12-11 12:05:10'),(77,'31数据迁移后核验','数据迁移','CAMS综合档案管理系统专业版',4,'2025-12-11 12:05:10','2025-12-11 12:05:10'),(78,'32存量数据创建全文索引，并开启增量索引任务','数据迁移','CAMS综合档案管理系统专业版',4,'2025-12-11 12:05:10','2025-12-11 12:05:10'),(79,'33接口需求及集成方式最终确定','接口开发','CAMS综合档案管理系统专业版',5,'2025-12-11 12:05:10','2025-12-11 12:05:10'),(80,'34接口开发','接口开发','CAMS综合档案管理系统专业版',5,'2025-12-11 12:05:10','2025-12-11 12:05:10'),(81,'35接口联调测试','接口开发','CAMS综合档案管理系统专业版',5,'2025-12-11 12:05:10','2025-12-11 12:05:10'),(82,'36接口部署','接口开发','CAMS综合档案管理系统专业版',5,'2025-12-11 12:05:10','2025-12-11 12:05:10'),(83,'37个性化需求整体最终确定','个性化功能开发','CAMS综合档案管理系统专业版',6,'2025-12-11 12:05:10','2025-12-11 12:05:10'),(84,'38个性化需求整体设计','个性化功能开发','CAMS综合档案管理系统专业版',6,'2025-12-11 12:05:10','2025-12-11 12:05:10'),(85,'39个性化需求开发','个性化功能开发','CAMS综合档案管理系统专业版',6,'2025-12-11 12:05:10','2025-12-11 12:05:10'),(86,'40个性化需求测试验证','个性化功能开发','CAMS综合档案管理系统专业版',6,'2025-12-11 12:05:10','2025-12-11 12:05:10'),(87,'41个性化需求部署及用户验证','个性化功能开发','CAMS综合档案管理系统专业版',6,'2025-12-11 12:05:10','2025-12-11 12:05:10'),(88,'42系统上线前功能调整及其他准备工作','系统上线试运行','CAMS综合档案管理系统专业版',7,'2025-12-11 12:05:10','2025-12-11 12:05:10'),(89,'43用户培训','用户培训','CAMS综合档案管理系统专业版',7,'2025-12-11 12:05:10','2025-12-11 12:05:10'),(90,'44系统正式上线','系统上线试运行','CAMS综合档案管理系统专业版',8,'2025-12-11 12:05:10','2025-12-11 12:05:10'),(91,'45项目正式验收','系统上线试运行','CAMS综合档案管理系统专业版',9,'2025-12-11 12:05:10','2025-12-11 12:05:10'),(92,'01召开项目启动会','标准产品','CAMS综合档案管理系统网络版',1,'2025-12-11 12:05:15','2025-12-11 12:05:15'),(93,'02系统应用规模及部署需求调研','标准产品','CAMS综合档案管理系统网络版',2,'2025-12-11 12:05:15','2025-12-11 12:05:15'),(94,'03档案业务现状及需求调研','标准产品','CAMS综合档案管理系统网络版',2,'2025-12-11 12:05:15','2025-12-11 12:05:15'),(95,'04历史数据迁移需求调研','数据迁移','CAMS综合档案管理系统网络版',2,'2025-12-11 12:05:15','2025-12-11 12:05:15'),(96,'05个性化功能需求调研','个性化功能开发','CAMS综合档案管理系统网络版',2,'2025-12-11 12:05:15','2025-12-11 12:05:15'),(97,'06系统安装部署','标准产品','CAMS综合档案管理系统网络版',3,'2025-12-11 12:05:15','2025-12-11 12:05:15'),(98,'07系统参数配置','标准产品','CAMS综合档案管理系统网络版',3,'2025-12-11 12:05:15','2025-12-11 12:05:15'),(99,'08存储配置','标准产品','CAMS综合档案管理系统网络版',3,'2025-12-11 12:05:15','2025-12-11 12:05:15'),(100,'09导入通用初始化库','标准产品','CAMS综合档案管理系统网络版',3,'2025-12-11 12:05:15','2025-12-11 12:05:15'),(101,'10设置全宗','标准产品','CAMS综合档案管理系统网络版',3,'2025-12-11 12:05:15','2025-12-11 12:05:15'),(102,'11设置门类结构、规则及字典','标准产品','CAMS综合档案管理系统网络版',3,'2025-12-11 12:05:15','2025-12-11 12:05:15'),(103,'12设置档案门类','标准产品','CAMS综合档案管理系统网络版',3,'2025-12-11 12:05:15','2025-12-11 12:05:15'),(104,'13关联门类结构并检查门类规则','标准产品','CAMS综合档案管理系统网络版',3,'2025-12-11 12:05:15','2025-12-11 12:05:15'),(105,'14设置组织机构','标准产品','CAMS综合档案管理系统网络版',3,'2025-12-11 12:05:15','2025-12-11 12:05:15'),(106,'15设置用户','标准产品','CAMS综合档案管理系统网络版',3,'2025-12-11 12:05:15','2025-12-11 12:05:15'),(107,'16设置角色权限及用户赋权','标准产品','CAMS综合档案管理系统网络版',3,'2025-12-11 12:05:15','2025-12-11 12:05:15'),(108,'17设置档案利用审核人','标准产品','CAMS综合档案管理系统网络版',3,'2025-12-11 12:05:15','2025-12-11 12:05:15'),(109,'18设置数据报表','标准产品','CAMS综合档案管理系统网络版',3,'2025-12-11 12:05:15','2025-12-11 12:05:15'),(110,'19设置档案统计','标准产品','CAMS综合档案管理系统网络版',3,'2025-12-11 12:05:15','2025-12-11 12:05:15'),(111,'20配置手动登记借阅模板','标准产品','CAMS综合档案管理系统网络版',3,'2025-12-11 12:05:15','2025-12-11 12:05:15'),(112,'21配置“三合一”表（如需要）','标准产品','CAMS综合档案管理系统网络版',3,'2025-12-11 12:05:15','2025-12-11 12:05:15'),(113,'22配置水印','标准产品','CAMS综合档案管理系统网络版',3,'2025-12-11 12:05:15','2025-12-11 12:05:15'),(114,'23配置备份策略','标准产品','CAMS综合档案管理系统网络版',3,'2025-12-11 12:05:15','2025-12-11 12:05:15'),(115,'24配置四性检测（如需要）','标准产品','CAMS综合档案管理系统网络版',3,'2025-12-11 12:05:15','2025-12-11 12:05:15'),(116,'25标准产品整体功能跑通确认及调整','标准产品','CAMS综合档案管理系统网络版',3,'2025-12-11 12:05:16','2025-12-11 12:05:16'),(117,'26数据迁移技术方式确定','数据迁移','CAMS综合档案管理系统网络版',4,'2025-12-11 12:05:16','2025-12-11 12:05:16'),(118,'27执行数据迁移','数据迁移','CAMS综合档案管理系统网络版',4,'2025-12-11 12:05:16','2025-12-11 12:05:16'),(119,'28数据迁移后核验','数据迁移','CAMS综合档案管理系统网络版',4,'2025-12-11 12:05:16','2025-12-11 12:05:16'),(120,'29存量数据创建全文索引，并开启增量索引任务','数据迁移','CAMS综合档案管理系统网络版',4,'2025-12-11 12:05:16','2025-12-11 12:05:16'),(121,'30个性化需求整体最终确定','个性化功能开发','CAMS综合档案管理系统网络版',6,'2025-12-11 12:05:16','2025-12-11 12:05:16'),(122,'31个性化需求整体设计','个性化功能开发','CAMS综合档案管理系统网络版',6,'2025-12-11 12:05:16','2025-12-11 12:05:16'),(123,'32个性化需求开发','个性化功能开发','CAMS综合档案管理系统网络版',6,'2025-12-11 12:05:16','2025-12-11 12:05:16'),(124,'33个性化需求测试验证','个性化功能开发','CAMS综合档案管理系统网络版',6,'2025-12-11 12:05:16','2025-12-11 12:05:16'),(125,'34个性化需求部署及用户验证','个性化功能开发','CAMS综合档案管理系统网络版',6,'2025-12-11 12:05:16','2025-12-11 12:05:16'),(126,'35系统上线前功能调整及其他准备工作','系统上线试运行','CAMS综合档案管理系统网络版',7,'2025-12-11 12:05:16','2025-12-11 12:05:16'),(127,'36用户培训','用户培训','CAMS综合档案管理系统网络版',7,'2025-12-11 12:05:16','2025-12-11 12:05:16'),(128,'37系统正式上线','系统上线试运行','CAMS综合档案管理系统网络版',8,'2025-12-11 12:05:16','2025-12-11 12:05:16'),(129,'38项目正式验收','系统上线试运行','CAMS综合档案管理系统网络版',9,'2025-12-11 12:05:16','2025-12-11 12:05:16'),(130,'01召开项目启动会','标准产品','档博通综合档案馆馆藏资源管理系统',1,'2025-12-11 12:05:23','2025-12-11 12:05:23'),(131,'02系统部署需求调研','标准产品','档博通综合档案馆馆藏资源管理系统',2,'2025-12-11 12:05:23','2025-12-11 12:05:23'),(132,'03档案业务现状及需求调研','标准产品','档博通综合档案馆馆藏资源管理系统',2,'2025-12-11 12:05:23','2025-12-11 12:05:23'),(133,'04历史数据迁移需求调研','数据迁移','档博通综合档案馆馆藏资源管理系统',2,'2025-12-11 12:05:23','2025-12-11 12:05:23'),(134,'05接口需求调研','接口开发','档博通综合档案馆馆藏资源管理系统',2,'2025-12-11 12:05:23','2025-12-11 12:05:23'),(135,'06个性化功能需求调研','个性化功能开发','档博通综合档案馆馆藏资源管理系统',2,'2025-12-11 12:05:23','2025-12-11 12:05:23'),(136,'07系统安装部署','标准产品','档博通综合档案馆馆藏资源管理系统',3,'2025-12-11 12:05:23','2025-12-11 12:05:23'),(137,'08系统参数配置','标准产品','档博通综合档案馆馆藏资源管理系统',3,'2025-12-11 12:05:23','2025-12-11 12:05:23'),(138,'09存储配置','标准产品','档博通综合档案馆馆藏资源管理系统',3,'2025-12-11 12:05:23','2025-12-11 12:05:23'),(139,'10导入通用初始化库（不需要导档案门类）','标准产品','档博通综合档案馆馆藏资源管理系统',3,'2025-12-11 12:05:23','2025-12-11 12:05:23'),(140,'11设置全宗','标准产品','档博通综合档案馆馆藏资源管理系统',3,'2025-12-11 12:05:23','2025-12-11 12:05:23'),(141,'12设置门类结构、规则及字典','标准产品','档博通综合档案馆馆藏资源管理系统',3,'2025-12-11 12:05:23','2025-12-11 12:05:23'),(142,'13设置档案门类','标准产品','档博通综合档案馆馆藏资源管理系统',3,'2025-12-11 12:05:23','2025-12-11 12:05:23'),(143,'14关联门类结构并检查门类规则','标准产品','档博通综合档案馆馆藏资源管理系统',3,'2025-12-11 12:05:23','2025-12-11 12:05:23'),(144,'15设置组织机构','标准产品','档博通综合档案馆馆藏资源管理系统',3,'2025-12-11 12:05:23','2025-12-11 12:05:23'),(145,'16设置用户','标准产品','档博通综合档案馆馆藏资源管理系统',3,'2025-12-11 12:05:23','2025-12-11 12:05:23'),(146,'17设置角色权限及用户赋权','标准产品','档博通综合档案馆馆藏资源管理系统',3,'2025-12-11 12:05:23','2025-12-11 12:05:23'),(147,'18设置鉴定、销毁表单及流程（如需要）','标准产品','档博通综合档案馆馆藏资源管理系统',3,'2025-12-11 12:05:23','2025-12-11 12:05:23'),(148,'19设置数据报表','标准产品','档博通综合档案馆馆藏资源管理系统',3,'2025-12-11 12:05:23','2025-12-11 12:05:23'),(149,'20设置档案资源统计、移交接收统计、档案利用统计','标准产品','档博通综合档案馆馆藏资源管理系统',3,'2025-12-11 12:05:23','2025-12-11 12:05:23'),(150,'21配置手动登记借阅模板','标准产品','档博通综合档案馆馆藏资源管理系统',3,'2025-12-11 12:05:23','2025-12-11 12:05:23'),(151,'22配置水印','标准产品','档博通综合档案馆馆藏资源管理系统',3,'2025-12-11 12:05:23','2025-12-11 12:05:23'),(152,'23配置备份策略','标准产品','档博通综合档案馆馆藏资源管理系统',3,'2025-12-11 12:05:23','2025-12-11 12:05:23'),(153,'24配置四性检测（如需要）','标准产品','档博通综合档案馆馆藏资源管理系统',3,'2025-12-11 12:05:23','2025-12-11 12:05:23'),(154,'25完成其他功能设置（如需要，例如发布管理、库房配置、菜单管理）','标准产品','档博通综合档案馆馆藏资源管理系统',3,'2025-12-11 12:05:23','2025-12-11 12:05:23'),(155,'26标准产品整体功能跑通确认及调整','标准产品','档博通综合档案馆馆藏资源管理系统',3,'2025-12-11 12:05:23','2025-12-11 12:05:23'),(156,'27数据迁移技术方式确定','数据迁移','档博通综合档案馆馆藏资源管理系统',4,'2025-12-11 12:05:23','2025-12-11 12:05:23'),(157,'28执行数据迁移','数据迁移','档博通综合档案馆馆藏资源管理系统',4,'2025-12-11 12:05:23','2025-12-11 12:05:23'),(158,'29数据迁移后核验','数据迁移','档博通综合档案馆馆藏资源管理系统',4,'2025-12-11 12:05:23','2025-12-11 12:05:23'),(159,'30存量数据创建全文索引，并开启增量索引任务','数据迁移','档博通综合档案馆馆藏资源管理系统',4,'2025-12-11 12:05:23','2025-12-11 12:05:23'),(160,'31接口需求及集成方式最终确定','接口开发','档博通综合档案馆馆藏资源管理系统',5,'2025-12-11 12:05:23','2025-12-11 12:05:23'),(161,'32接口开发','接口开发','档博通综合档案馆馆藏资源管理系统',5,'2025-12-11 12:05:23','2025-12-11 12:05:23'),(162,'33接口联调测试','接口开发','档博通综合档案馆馆藏资源管理系统',5,'2025-12-11 12:05:23','2025-12-11 12:05:23'),(163,'34接口部署','接口开发','档博通综合档案馆馆藏资源管理系统',5,'2025-12-11 12:05:23','2025-12-11 12:05:23'),(164,'35个性化需求整体最终确定','个性化功能开发','档博通综合档案馆馆藏资源管理系统',6,'2025-12-11 12:05:23','2025-12-11 12:05:23'),(165,'36个性化需求整体设计','个性化功能开发','档博通综合档案馆馆藏资源管理系统',6,'2025-12-11 12:05:23','2025-12-11 12:05:23'),(166,'37个性化需求开发','个性化功能开发','档博通综合档案馆馆藏资源管理系统',6,'2025-12-11 12:05:23','2025-12-11 12:05:23'),(167,'38个性化需求测试验证','个性化功能开发','档博通综合档案馆馆藏资源管理系统',6,'2025-12-11 12:05:23','2025-12-11 12:05:23'),(168,'39个性化需求部署及用户验证','个性化功能开发','档博通综合档案馆馆藏资源管理系统',6,'2025-12-11 12:05:23','2025-12-11 12:05:23'),(169,'40系统上线前功能调整及其他准备工作','系统上线试运行','档博通综合档案馆馆藏资源管理系统',7,'2025-12-11 12:05:23','2025-12-11 12:05:23'),(170,'41用户培训','用户培训','档博通综合档案馆馆藏资源管理系统',7,'2025-12-11 12:05:23','2025-12-11 12:05:23'),(171,'42系统正式上线','系统上线试运行','档博通综合档案馆馆藏资源管理系统',8,'2025-12-11 12:05:23','2025-12-11 12:05:23'),(172,'43项目正式验收','系统上线试运行','档博通综合档案馆馆藏资源管理系统',9,'2025-12-11 12:05:23','2025-12-11 12:05:23');
/*!40000 ALTER TABLE `standard_construct_step` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `standard_deliverable`
--

DROP TABLE IF EXISTS `standard_deliverable`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `standard_deliverable` (
  `deliverableId` bigint NOT NULL AUTO_INCREMENT COMMENT '交付物ID，主键',
  `deliverableName` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '交付物名称',
  `systemName` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '系统名称',
  `deliverableType` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '交付物类型',
  `deliveryTempletePath` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '模板文件相对路径',
  `isMustLoad` tinyint(1) DEFAULT '0' COMMENT '是否必须上传，0-非必须，1-必须',
  `sstepId` bigint DEFAULT NULL COMMENT '标准步骤ID，standard_construct_step表外键',
  `milestoneId` bigint DEFAULT NULL COMMENT '里程碑ID，construct_milestone表外键',
  `createTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`deliverableId`) USING BTREE,
  KEY `idx_construct_deliverable_name` (`deliverableName`) USING BTREE,
  KEY `idx_construct_standard_deliverable_must_load` (`isMustLoad`) USING BTREE,
  KEY `idx_construct_standard_deliverable_sstep` (`sstepId`) USING BTREE,
  KEY `idx_standard_deliverable_system_name` (`systemName`) USING BTREE,
  KEY `idx_standard_deliverable_type` (`deliverableType`) USING BTREE,
  KEY `idx_standard_deliverable_milestone` (`milestoneId`) USING BTREE,
  CONSTRAINT `fk_construct_standard_deliverable_sstep` FOREIGN KEY (`sstepId`) REFERENCES `standard_construct_step` (`sstepId`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_standard_deliverable_standard_milestone` FOREIGN KEY (`milestoneId`) REFERENCES `standard_milestone` (`milestone_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=177 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='交付物表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `standard_deliverable`
--

LOCK TABLES `standard_deliverable` WRITE;
/*!40000 ALTER TABLE `standard_deliverable` DISABLE KEYS */;
INSERT INTO `standard_deliverable` VALUES (1,'项目启动会汇报材料','DAIS数字档案馆（室）一体化系统','里程碑交付物',NULL,1,NULL,1,'2025-12-11 12:05:36','2025-12-11 12:05:36'),(2,'系统应用规模及部署需求调研结果','DAIS数字档案馆（室）一体化系统','步骤交付物','docs/deliveryTempletes/DAIS数字档案馆（室）一体化系统/',1,2,NULL,'2025-12-11 12:05:36','2025-12-12 10:42:20'),(3,'档案业务现状及需求调研报告','DAIS数字档案馆（室）一体化系统','步骤交付物',NULL,1,3,NULL,'2025-12-11 12:05:36','2025-12-11 12:05:36'),(4,'历史数据迁移需求调研报告','DAIS数字档案馆（室）一体化系统','步骤交付物',NULL,1,4,NULL,'2025-12-11 12:05:36','2025-12-11 12:05:36'),(5,'业务系统接口需求调研报告','DAIS数字档案馆（室）一体化系统','步骤交付物',NULL,1,5,NULL,'2025-12-11 12:05:36','2025-12-11 12:05:36'),(6,'个性化功能需求调研报告','DAIS数字档案馆（室）一体化系统','步骤交付物',NULL,1,6,NULL,'2025-12-11 12:05:36','2025-12-11 12:05:36'),(7,'项目整体需求调研报告','DAIS数字档案馆（室）一体化系统','里程碑交付物',NULL,1,NULL,2,'2025-12-11 12:05:36','2025-12-11 12:05:36'),(8,'系统安装部署文档','DAIS数字档案馆（室）一体化系统','步骤交付物',NULL,1,7,NULL,'2025-12-11 12:05:36','2025-12-11 12:05:36'),(9,'配置截图','DAIS数字档案馆（室）一体化系统','步骤交付物',NULL,0,8,NULL,'2025-12-11 12:05:36','2025-12-11 12:05:36'),(10,'配置截图','DAIS数字档案馆（室）一体化系统','步骤交付物',NULL,1,9,NULL,'2025-12-11 12:05:36','2025-12-11 12:05:36'),(11,'配置截图','DAIS数字档案馆（室）一体化系统','步骤交付物',NULL,0,10,NULL,'2025-12-11 12:05:36','2025-12-11 12:05:36'),(12,'配置截图','DAIS数字档案馆（室）一体化系统','步骤交付物',NULL,1,11,NULL,'2025-12-11 12:05:36','2025-12-11 12:05:36'),(13,'配置截图','DAIS数字档案馆（室）一体化系统','步骤交付物',NULL,1,12,NULL,'2025-12-11 12:05:36','2025-12-11 12:05:36'),(14,'配置截图','DAIS数字档案馆（室）一体化系统','步骤交付物',NULL,1,13,NULL,'2025-12-11 12:05:36','2025-12-11 12:05:36'),(15,'配置截图','DAIS数字档案馆（室）一体化系统','步骤交付物',NULL,0,14,NULL,'2025-12-11 12:05:36','2025-12-11 12:05:36'),(16,'配置截图','DAIS数字档案馆（室）一体化系统','步骤交付物',NULL,1,15,NULL,'2025-12-11 12:05:36','2025-12-11 12:05:36'),(17,'配置截图','DAIS数字档案馆（室）一体化系统','步骤交付物',NULL,0,16,NULL,'2025-12-11 12:05:36','2025-12-11 12:05:36'),(18,'配置截图','DAIS数字档案馆（室）一体化系统','步骤交付物',NULL,1,17,NULL,'2025-12-11 12:05:37','2025-12-11 12:05:37'),(19,'配置截图','DAIS数字档案馆（室）一体化系统','步骤交付物',NULL,1,18,NULL,'2025-12-11 12:05:37','2025-12-11 12:05:37'),(20,'配置截图','DAIS数字档案馆（室）一体化系统','步骤交付物',NULL,1,19,NULL,'2025-12-11 12:05:37','2025-12-11 12:05:37'),(21,'配置截图','DAIS数字档案馆（室）一体化系统','步骤交付物',NULL,0,20,NULL,'2025-12-11 12:05:37','2025-12-11 12:05:37'),(22,'配置截图','DAIS数字档案馆（室）一体化系统','步骤交付物',NULL,1,21,NULL,'2025-12-11 12:05:37','2025-12-11 12:05:37'),(23,'配置截图','DAIS数字档案馆（室）一体化系统','步骤交付物',NULL,1,22,NULL,'2025-12-11 12:05:37','2025-12-11 12:05:37'),(24,'配置截图','DAIS数字档案馆（室）一体化系统','步骤交付物',NULL,1,23,NULL,'2025-12-11 12:05:37','2025-12-11 12:05:37'),(25,'配置截图','DAIS数字档案馆（室）一体化系统','步骤交付物',NULL,0,24,NULL,'2025-12-11 12:05:37','2025-12-11 12:05:37'),(26,'配置截图','DAIS数字档案馆（室）一体化系统','步骤交付物',NULL,1,25,NULL,'2025-12-11 12:05:37','2025-12-11 12:05:37'),(27,'配置截图','DAIS数字档案馆（室）一体化系统','步骤交付物',NULL,1,26,NULL,'2025-12-11 12:05:37','2025-12-11 12:05:37'),(28,'配置截图','DAIS数字档案馆（室）一体化系统','步骤交付物',NULL,0,27,NULL,'2025-12-11 12:05:37','2025-12-11 12:05:37'),(29,'配置截图','DAIS数字档案馆（室）一体化系统','步骤交付物',NULL,0,28,NULL,'2025-12-11 12:05:37','2025-12-11 12:05:37'),(30,'标准产品功能配置报告','DAIS数字档案馆（室）一体化系统','里程碑交付物',NULL,1,NULL,3,'2025-12-11 12:05:37','2025-12-11 12:05:37'),(31,'数据迁移技术方案','DAIS数字档案馆（室）一体化系统','步骤交付物',NULL,1,30,NULL,'2025-12-11 12:05:37','2025-12-11 12:05:37'),(32,'数据迁移核验表','DAIS数字档案馆（室）一体化系统','步骤交付物',NULL,1,32,NULL,'2025-12-11 12:05:37','2025-12-11 12:05:37'),(33,'数据迁移报告','DAIS数字档案馆（室）一体化系统','里程碑交付物',NULL,1,NULL,4,'2025-12-11 12:05:37','2025-12-11 12:05:37'),(34,'接口需求文档（用户签字版）','DAIS数字档案馆（室）一体化系统','步骤交付物',NULL,1,34,NULL,'2025-12-11 12:05:37','2025-12-11 12:05:37'),(35,'接口文档','DAIS数字档案馆（室）一体化系统','步骤交付物',NULL,1,35,NULL,'2025-12-11 12:05:37','2025-12-11 12:05:37'),(36,'接口开发代码或配置文件','DAIS数字档案馆（室）一体化系统','步骤交付物',NULL,1,37,NULL,'2025-12-11 12:05:37','2025-12-11 12:05:37'),(37,'接口集成报告','DAIS数字档案馆（室）一体化系统','里程碑交付物',NULL,1,NULL,5,'2025-12-11 12:05:37','2025-12-11 12:05:37'),(38,'个性化需求文档（用户签字版）','DAIS数字档案馆（室）一体化系统','步骤交付物',NULL,1,38,NULL,'2025-12-11 12:05:37','2025-12-11 12:05:37'),(39,'个性化需求设计方案（用户签字版）','DAIS数字档案馆（室）一体化系统','步骤交付物',NULL,1,39,NULL,'2025-12-11 12:05:37','2025-12-11 12:05:37'),(40,'个性化需求开发代码','DAIS数字档案馆（室）一体化系统','步骤交付物',NULL,1,42,NULL,'2025-12-11 12:05:37','2025-12-11 12:05:37'),(41,'个性化需求开发报告','DAIS数字档案馆（室）一体化系统','里程碑交付物',NULL,1,NULL,6,'2025-12-11 12:05:37','2025-12-11 12:05:37'),(42,'产生变动的里程碑交付物','DAIS数字档案馆（室）一体化系统','步骤交付物',NULL,0,43,NULL,'2025-12-11 12:05:37','2025-12-11 12:05:37'),(43,'用户操作手册及培训材料','DAIS数字档案馆（室）一体化系统','步骤交付物',NULL,1,44,NULL,'2025-12-11 12:05:37','2025-12-11 12:05:37'),(44,'项目产生的其他材料','DAIS数字档案馆（室）一体化系统','里程碑交付物',NULL,0,NULL,7,'2025-12-11 12:05:37','2025-12-11 12:05:37'),(45,'上线试运行申请（用户签字版）','DAIS数字档案馆（室）一体化系统','里程碑交付物',NULL,1,NULL,8,'2025-12-11 12:05:37','2025-12-11 12:05:37'),(46,'试运行报告（如需）','DAIS数字档案馆（室）一体化系统','里程碑交付物',NULL,0,NULL,9,'2025-12-11 12:05:37','2025-12-11 12:05:37'),(47,'项目验收报告','DAIS数字档案馆（室）一体化系统','里程碑交付物',NULL,1,NULL,9,'2025-12-11 12:05:37','2025-12-11 12:05:37'),(48,'项目启动会汇报材料','CAMS综合档案管理系统专业版','里程碑交付物',NULL,1,NULL,1,'2025-12-11 12:05:47','2025-12-11 12:05:47'),(49,'系统应用规模及部署需求调研结果','CAMS综合档案管理系统专业版','步骤交付物',NULL,1,48,NULL,'2025-12-11 12:05:47','2025-12-11 12:05:47'),(50,'档案业务现状及需求调研报告','CAMS综合档案管理系统专业版','步骤交付物',NULL,1,49,NULL,'2025-12-11 12:05:47','2025-12-11 12:05:47'),(51,'历史数据迁移需求调研报告','CAMS综合档案管理系统专业版','步骤交付物',NULL,1,50,NULL,'2025-12-11 12:05:47','2025-12-11 12:05:47'),(52,'业务系统接口需求调研报告','CAMS综合档案管理系统专业版','步骤交付物',NULL,1,51,NULL,'2025-12-11 12:05:47','2025-12-11 12:05:47'),(53,'个性化功能需求调研报告','CAMS综合档案管理系统专业版','步骤交付物',NULL,1,52,NULL,'2025-12-11 12:05:47','2025-12-11 12:05:47'),(54,'项目整体需求调研报告','CAMS综合档案管理系统专业版','里程碑交付物',NULL,1,NULL,2,'2025-12-11 12:05:47','2025-12-11 12:05:47'),(55,'系统安装部署文档','CAMS综合档案管理系统专业版','步骤交付物',NULL,1,53,NULL,'2025-12-11 12:05:47','2025-12-11 12:05:47'),(56,'配置截图','CAMS综合档案管理系统专业版','步骤交付物',NULL,0,54,NULL,'2025-12-11 12:05:47','2025-12-11 12:05:47'),(57,'配置截图','CAMS综合档案管理系统专业版','步骤交付物',NULL,1,55,NULL,'2025-12-11 12:05:47','2025-12-11 12:05:47'),(58,'配置截图','CAMS综合档案管理系统专业版','步骤交付物',NULL,0,56,NULL,'2025-12-11 12:05:47','2025-12-11 12:05:47'),(59,'配置截图','CAMS综合档案管理系统专业版','步骤交付物',NULL,1,57,NULL,'2025-12-11 12:05:47','2025-12-11 12:05:47'),(60,'配置截图','CAMS综合档案管理系统专业版','步骤交付物',NULL,1,58,NULL,'2025-12-11 12:05:47','2025-12-11 12:05:47'),(61,'配置截图','CAMS综合档案管理系统专业版','步骤交付物',NULL,1,59,NULL,'2025-12-11 12:05:47','2025-12-11 12:05:47'),(62,'配置截图','CAMS综合档案管理系统专业版','步骤交付物',NULL,0,60,NULL,'2025-12-11 12:05:47','2025-12-11 12:05:47'),(63,'配置截图','CAMS综合档案管理系统专业版','步骤交付物',NULL,1,61,NULL,'2025-12-11 12:05:47','2025-12-11 12:05:47'),(64,'配置截图','CAMS综合档案管理系统专业版','步骤交付物',NULL,0,62,NULL,'2025-12-11 12:05:47','2025-12-11 12:05:47'),(65,'配置截图','CAMS综合档案管理系统专业版','步骤交付物',NULL,1,63,NULL,'2025-12-11 12:05:47','2025-12-11 12:05:47'),(66,'配置截图','CAMS综合档案管理系统专业版','步骤交付物',NULL,1,64,NULL,'2025-12-11 12:05:47','2025-12-11 12:05:47'),(67,'配置截图','CAMS综合档案管理系统专业版','步骤交付物',NULL,1,65,NULL,'2025-12-11 12:05:47','2025-12-11 12:05:47'),(68,'配置截图','CAMS综合档案管理系统专业版','步骤交付物',NULL,1,66,NULL,'2025-12-11 12:05:47','2025-12-11 12:05:47'),(69,'配置截图','CAMS综合档案管理系统专业版','步骤交付物',NULL,1,67,NULL,'2025-12-11 12:05:47','2025-12-11 12:05:47'),(70,'配置截图','CAMS综合档案管理系统专业版','步骤交付物',NULL,1,68,NULL,'2025-12-11 12:05:47','2025-12-11 12:05:47'),(71,'配置截图','CAMS综合档案管理系统专业版','步骤交付物',NULL,0,69,NULL,'2025-12-11 12:05:47','2025-12-11 12:05:47'),(72,'配置截图','CAMS综合档案管理系统专业版','步骤交付物',NULL,1,70,NULL,'2025-12-11 12:05:47','2025-12-11 12:05:47'),(73,'配置截图','CAMS综合档案管理系统专业版','步骤交付物',NULL,1,71,NULL,'2025-12-11 12:05:47','2025-12-11 12:05:47'),(74,'配置截图','CAMS综合档案管理系统专业版','步骤交付物',NULL,0,72,NULL,'2025-12-11 12:05:47','2025-12-11 12:05:47'),(75,'配置截图','CAMS综合档案管理系统专业版','步骤交付物',NULL,0,73,NULL,'2025-12-11 12:05:47','2025-12-11 12:05:47'),(76,'标准产品功能配置报告','CAMS综合档案管理系统专业版','里程碑交付物',NULL,1,NULL,3,'2025-12-11 12:05:47','2025-12-11 12:05:47'),(77,'数据迁移技术方案','CAMS综合档案管理系统专业版','步骤交付物',NULL,1,75,NULL,'2025-12-11 12:05:47','2025-12-11 12:05:47'),(78,'数据迁移核验表','CAMS综合档案管理系统专业版','步骤交付物',NULL,1,77,NULL,'2025-12-11 12:05:47','2025-12-11 12:05:47'),(79,'数据迁移报告','CAMS综合档案管理系统专业版','里程碑交付物',NULL,1,NULL,4,'2025-12-11 12:05:47','2025-12-11 12:05:47'),(80,'接口需求文档（用户签字版）','CAMS综合档案管理系统专业版','步骤交付物',NULL,1,79,NULL,'2025-12-11 12:05:47','2025-12-11 12:05:47'),(81,'接口文档','CAMS综合档案管理系统专业版','步骤交付物',NULL,1,80,NULL,'2025-12-11 12:05:47','2025-12-11 12:05:47'),(82,'接口开发代码或配置文件','CAMS综合档案管理系统专业版','步骤交付物',NULL,1,82,NULL,'2025-12-11 12:05:47','2025-12-11 12:05:47'),(83,'接口集成报告','CAMS综合档案管理系统专业版','里程碑交付物',NULL,1,NULL,5,'2025-12-11 12:05:47','2025-12-11 12:05:47'),(84,'个性化需求文档（用户签字版）','CAMS综合档案管理系统专业版','步骤交付物',NULL,1,83,NULL,'2025-12-11 12:05:48','2025-12-11 12:05:48'),(85,'个性化需求设计方案（用户签字版）','CAMS综合档案管理系统专业版','步骤交付物',NULL,1,84,NULL,'2025-12-11 12:05:48','2025-12-11 12:05:48'),(86,'个性化需求开发代码','CAMS综合档案管理系统专业版','步骤交付物',NULL,1,87,NULL,'2025-12-11 12:05:48','2025-12-11 12:05:48'),(87,'个性化需求开发报告','CAMS综合档案管理系统专业版','里程碑交付物',NULL,1,NULL,6,'2025-12-11 12:05:48','2025-12-11 12:05:48'),(88,'产生变动的里程碑交付物','CAMS综合档案管理系统专业版','步骤交付物',NULL,0,88,NULL,'2025-12-11 12:05:48','2025-12-11 12:05:48'),(89,'用户操作手册及培训材料','CAMS综合档案管理系统专业版','步骤交付物',NULL,1,89,NULL,'2025-12-11 12:05:48','2025-12-11 12:05:48'),(90,'项目产生的其他材料','CAMS综合档案管理系统专业版','里程碑交付物',NULL,0,NULL,7,'2025-12-11 12:05:48','2025-12-11 12:05:48'),(91,'上线试运行申请（用户签字版）','CAMS综合档案管理系统专业版','里程碑交付物',NULL,1,NULL,8,'2025-12-11 12:05:48','2025-12-11 12:05:48'),(92,'试运行报告（如需）','CAMS综合档案管理系统专业版','里程碑交付物',NULL,0,NULL,9,'2025-12-11 12:05:48','2025-12-11 12:05:48'),(93,'项目验收报告','CAMS综合档案管理系统专业版','里程碑交付物',NULL,1,NULL,9,'2025-12-11 12:05:48','2025-12-11 12:05:48'),(94,'项目启动会汇报材料','CAMS综合档案管理系统网络版','里程碑交付物',NULL,1,NULL,1,'2025-12-11 12:05:55','2025-12-11 12:05:55'),(95,'系统应用规模及部署需求调研结果','CAMS综合档案管理系统网络版','步骤交付物',NULL,1,93,NULL,'2025-12-11 12:05:55','2025-12-11 12:05:55'),(96,'档案业务现状及需求调研报告','CAMS综合档案管理系统网络版','步骤交付物',NULL,1,94,NULL,'2025-12-11 12:05:55','2025-12-11 12:05:55'),(97,'历史数据迁移需求调研报告','CAMS综合档案管理系统网络版','步骤交付物',NULL,1,95,NULL,'2025-12-11 12:05:55','2025-12-11 12:05:55'),(98,'个性化功能需求调研报告','CAMS综合档案管理系统网络版','步骤交付物',NULL,1,96,NULL,'2025-12-11 12:05:55','2025-12-11 12:05:55'),(99,'项目整体需求调研报告','CAMS综合档案管理系统网络版','里程碑交付物',NULL,1,NULL,2,'2025-12-11 12:05:55','2025-12-11 12:05:55'),(100,'系统安装部署文档','CAMS综合档案管理系统网络版','步骤交付物',NULL,1,97,NULL,'2025-12-11 12:05:55','2025-12-11 12:05:55'),(101,'配置截图','CAMS综合档案管理系统网络版','步骤交付物',NULL,0,98,NULL,'2025-12-11 12:05:55','2025-12-11 12:05:55'),(102,'配置截图','CAMS综合档案管理系统网络版','步骤交付物',NULL,1,99,NULL,'2025-12-11 12:05:55','2025-12-11 12:05:55'),(103,'配置截图','CAMS综合档案管理系统网络版','步骤交付物',NULL,0,100,NULL,'2025-12-11 12:05:55','2025-12-11 12:05:55'),(104,'配置截图','CAMS综合档案管理系统网络版','步骤交付物',NULL,1,101,NULL,'2025-12-11 12:05:55','2025-12-11 12:05:55'),(105,'配置截图','CAMS综合档案管理系统网络版','步骤交付物',NULL,1,102,NULL,'2025-12-11 12:05:55','2025-12-11 12:05:55'),(106,'配置截图','CAMS综合档案管理系统网络版','步骤交付物',NULL,1,103,NULL,'2025-12-11 12:05:55','2025-12-11 12:05:55'),(107,'配置截图','CAMS综合档案管理系统网络版','步骤交付物',NULL,0,104,NULL,'2025-12-11 12:05:55','2025-12-11 12:05:55'),(108,'配置截图','CAMS综合档案管理系统网络版','步骤交付物',NULL,1,105,NULL,'2025-12-11 12:05:55','2025-12-11 12:05:55'),(109,'配置截图','CAMS综合档案管理系统网络版','步骤交付物',NULL,0,106,NULL,'2025-12-11 12:05:55','2025-12-11 12:05:55'),(110,'配置截图','CAMS综合档案管理系统网络版','步骤交付物',NULL,1,107,NULL,'2025-12-11 12:05:55','2025-12-11 12:05:55'),(111,'配置截图','CAMS综合档案管理系统网络版','步骤交付物',NULL,1,108,NULL,'2025-12-11 12:05:56','2025-12-11 12:05:56'),(112,'配置截图','CAMS综合档案管理系统网络版','步骤交付物',NULL,1,109,NULL,'2025-12-11 12:05:56','2025-12-11 12:05:56'),(113,'配置截图','CAMS综合档案管理系统网络版','步骤交付物',NULL,1,110,NULL,'2025-12-11 12:05:56','2025-12-11 12:05:56'),(114,'配置截图','CAMS综合档案管理系统网络版','步骤交付物',NULL,1,111,NULL,'2025-12-11 12:05:56','2025-12-11 12:05:56'),(115,'配置截图','CAMS综合档案管理系统网络版','步骤交付物',NULL,0,112,NULL,'2025-12-11 12:05:56','2025-12-11 12:05:56'),(116,'配置截图','CAMS综合档案管理系统网络版','步骤交付物',NULL,1,113,NULL,'2025-12-11 12:05:56','2025-12-11 12:05:56'),(117,'配置截图','CAMS综合档案管理系统网络版','步骤交付物',NULL,1,114,NULL,'2025-12-11 12:05:56','2025-12-11 12:05:56'),(118,'配置截图','CAMS综合档案管理系统网络版','步骤交付物',NULL,0,115,NULL,'2025-12-11 12:05:56','2025-12-11 12:05:56'),(119,'标准产品功能配置报告','CAMS综合档案管理系统网络版','里程碑交付物',NULL,1,NULL,3,'2025-12-11 12:05:56','2025-12-11 12:05:56'),(120,'数据迁移技术方案','CAMS综合档案管理系统网络版','步骤交付物',NULL,1,117,NULL,'2025-12-11 12:05:56','2025-12-11 12:05:56'),(121,'数据迁移核验表','CAMS综合档案管理系统网络版','步骤交付物',NULL,1,119,NULL,'2025-12-11 12:05:56','2025-12-11 12:05:56'),(122,'数据迁移报告','CAMS综合档案管理系统网络版','里程碑交付物',NULL,1,NULL,4,'2025-12-11 12:05:56','2025-12-11 12:05:56'),(123,'个性化需求文档（用户签字版）','CAMS综合档案管理系统网络版','步骤交付物',NULL,1,121,NULL,'2025-12-11 12:05:56','2025-12-11 12:05:56'),(124,'个性化需求设计方案（用户签字版）','CAMS综合档案管理系统网络版','步骤交付物',NULL,1,122,NULL,'2025-12-11 12:05:56','2025-12-11 12:05:56'),(125,'个性化需求开发代码','CAMS综合档案管理系统网络版','步骤交付物',NULL,1,125,NULL,'2025-12-11 12:05:56','2025-12-11 12:05:56'),(126,'个性化需求开发报告','CAMS综合档案管理系统网络版','里程碑交付物',NULL,1,NULL,6,'2025-12-11 12:05:56','2025-12-11 12:05:56'),(127,'产生变动的里程碑交付物','CAMS综合档案管理系统网络版','步骤交付物',NULL,0,126,NULL,'2025-12-11 12:05:56','2025-12-11 12:05:56'),(128,'用户操作手册及培训材料','CAMS综合档案管理系统网络版','步骤交付物',NULL,1,127,NULL,'2025-12-11 12:05:56','2025-12-11 12:05:56'),(129,'项目产生的其他材料','CAMS综合档案管理系统网络版','里程碑交付物',NULL,0,NULL,7,'2025-12-11 12:05:56','2025-12-11 12:05:56'),(130,'上线试运行申请（用户签字版）','CAMS综合档案管理系统网络版','里程碑交付物',NULL,1,NULL,8,'2025-12-11 12:05:56','2025-12-11 12:05:56'),(131,'试运行报告（如需）','CAMS综合档案管理系统网络版','里程碑交付物',NULL,0,NULL,9,'2025-12-11 12:05:56','2025-12-11 12:05:56'),(132,'项目验收报告','CAMS综合档案管理系统网络版','里程碑交付物',NULL,1,NULL,9,'2025-12-11 12:05:56','2025-12-11 12:05:56'),(133,'项目启动会汇报材料','档博通综合档案馆馆藏资源管理系统','里程碑交付物',NULL,1,NULL,1,'2025-12-11 12:06:02','2025-12-11 12:06:02'),(134,'系统部署需求调研结果','档博通综合档案馆馆藏资源管理系统','步骤交付物',NULL,1,131,NULL,'2025-12-11 12:06:02','2025-12-11 12:06:02'),(135,'档案业务现状及需求调研报告','档博通综合档案馆馆藏资源管理系统','步骤交付物',NULL,1,132,NULL,'2025-12-11 12:06:02','2025-12-11 12:06:02'),(136,'历史数据迁移需求调研报告','档博通综合档案馆馆藏资源管理系统','步骤交付物',NULL,1,133,NULL,'2025-12-11 12:06:02','2025-12-11 12:06:02'),(137,'接口需求调研报告','档博通综合档案馆馆藏资源管理系统','步骤交付物',NULL,1,134,NULL,'2025-12-11 12:06:02','2025-12-11 12:06:02'),(138,'个性化功能需求调研报告','档博通综合档案馆馆藏资源管理系统','步骤交付物',NULL,1,135,NULL,'2025-12-11 12:06:02','2025-12-11 12:06:02'),(139,'项目整体需求调研报告','档博通综合档案馆馆藏资源管理系统','里程碑交付物',NULL,1,NULL,2,'2025-12-11 12:06:02','2025-12-11 12:06:02'),(140,'系统安装部署文档','档博通综合档案馆馆藏资源管理系统','步骤交付物',NULL,1,136,NULL,'2025-12-11 12:06:02','2025-12-11 12:06:02'),(141,'配置截图','档博通综合档案馆馆藏资源管理系统','步骤交付物',NULL,0,137,NULL,'2025-12-11 12:06:02','2025-12-11 12:06:02'),(142,'配置截图','档博通综合档案馆馆藏资源管理系统','步骤交付物',NULL,1,138,NULL,'2025-12-11 12:06:02','2025-12-11 12:06:02'),(143,'配置截图','档博通综合档案馆馆藏资源管理系统','步骤交付物',NULL,0,139,NULL,'2025-12-11 12:06:02','2025-12-11 12:06:02'),(144,'配置截图','档博通综合档案馆馆藏资源管理系统','步骤交付物',NULL,1,140,NULL,'2025-12-11 12:06:02','2025-12-11 12:06:02'),(145,'配置截图','档博通综合档案馆馆藏资源管理系统','步骤交付物',NULL,1,141,NULL,'2025-12-11 12:06:02','2025-12-11 12:06:02'),(146,'配置截图','档博通综合档案馆馆藏资源管理系统','步骤交付物',NULL,1,142,NULL,'2025-12-11 12:06:02','2025-12-11 12:06:02'),(147,'配置截图','档博通综合档案馆馆藏资源管理系统','步骤交付物',NULL,0,143,NULL,'2025-12-11 12:06:02','2025-12-11 12:06:02'),(148,'配置截图','档博通综合档案馆馆藏资源管理系统','步骤交付物',NULL,1,144,NULL,'2025-12-11 12:06:02','2025-12-11 12:06:02'),(149,'配置截图','档博通综合档案馆馆藏资源管理系统','步骤交付物',NULL,0,145,NULL,'2025-12-11 12:06:02','2025-12-11 12:06:02'),(150,'配置截图','档博通综合档案馆馆藏资源管理系统','步骤交付物',NULL,1,146,NULL,'2025-12-11 12:06:02','2025-12-11 12:06:02'),(151,'配置截图','档博通综合档案馆馆藏资源管理系统','步骤交付物',NULL,0,147,NULL,'2025-12-11 12:06:02','2025-12-11 12:06:02'),(152,'配置截图','档博通综合档案馆馆藏资源管理系统','步骤交付物',NULL,1,148,NULL,'2025-12-11 12:06:02','2025-12-11 12:06:02'),(153,'配置截图','档博通综合档案馆馆藏资源管理系统','步骤交付物',NULL,1,149,NULL,'2025-12-11 12:06:02','2025-12-11 12:06:02'),(154,'配置截图','档博通综合档案馆馆藏资源管理系统','步骤交付物',NULL,1,150,NULL,'2025-12-11 12:06:02','2025-12-11 12:06:02'),(155,'配置截图','档博通综合档案馆馆藏资源管理系统','步骤交付物',NULL,1,151,NULL,'2025-12-11 12:06:02','2025-12-11 12:06:02'),(156,'配置截图','档博通综合档案馆馆藏资源管理系统','步骤交付物',NULL,1,152,NULL,'2025-12-11 12:06:02','2025-12-11 12:06:02'),(157,'配置截图','档博通综合档案馆馆藏资源管理系统','步骤交付物',NULL,0,153,NULL,'2025-12-11 12:06:02','2025-12-11 12:06:02'),(158,'配置截图','档博通综合档案馆馆藏资源管理系统','步骤交付物',NULL,0,154,NULL,'2025-12-11 12:06:03','2025-12-11 12:06:03'),(159,'标准产品功能配置报告','档博通综合档案馆馆藏资源管理系统','里程碑交付物',NULL,1,NULL,3,'2025-12-11 12:06:03','2025-12-11 12:06:03'),(160,'数据迁移技术方案','档博通综合档案馆馆藏资源管理系统','步骤交付物',NULL,1,156,NULL,'2025-12-11 12:06:03','2025-12-11 12:06:03'),(161,'数据迁移核验表','档博通综合档案馆馆藏资源管理系统','步骤交付物',NULL,1,158,NULL,'2025-12-11 12:06:03','2025-12-11 12:06:03'),(162,'数据迁移报告','档博通综合档案馆馆藏资源管理系统','里程碑交付物',NULL,1,NULL,4,'2025-12-11 12:06:03','2025-12-11 12:06:03'),(163,'接口需求文档（用户签字版）','档博通综合档案馆馆藏资源管理系统','步骤交付物',NULL,1,160,NULL,'2025-12-11 12:06:03','2025-12-11 12:06:03'),(164,'接口文档','档博通综合档案馆馆藏资源管理系统','步骤交付物',NULL,1,161,NULL,'2025-12-11 12:06:03','2025-12-11 12:06:03'),(165,'接口开发代码或配置文件','档博通综合档案馆馆藏资源管理系统','步骤交付物',NULL,1,163,NULL,'2025-12-11 12:06:03','2025-12-11 12:06:03'),(166,'接口集成报告','档博通综合档案馆馆藏资源管理系统','里程碑交付物',NULL,1,NULL,5,'2025-12-11 12:06:03','2025-12-11 12:06:03'),(167,'个性化需求文档（用户签字版）','档博通综合档案馆馆藏资源管理系统','步骤交付物',NULL,1,164,NULL,'2025-12-11 12:06:03','2025-12-11 12:06:03'),(168,'个性化需求设计方案（用户签字版）','档博通综合档案馆馆藏资源管理系统','步骤交付物',NULL,1,165,NULL,'2025-12-11 12:06:03','2025-12-11 12:06:03'),(169,'个性化需求开发代码','档博通综合档案馆馆藏资源管理系统','步骤交付物',NULL,1,168,NULL,'2025-12-11 12:06:03','2025-12-11 12:06:03'),(170,'个性化需求开发报告','档博通综合档案馆馆藏资源管理系统','里程碑交付物',NULL,1,NULL,6,'2025-12-11 12:06:03','2025-12-11 12:06:03'),(171,'产生变动的里程碑交付物','档博通综合档案馆馆藏资源管理系统','步骤交付物',NULL,0,169,NULL,'2025-12-11 12:06:03','2025-12-11 12:06:03'),(172,'用户操作手册及培训材料','档博通综合档案馆馆藏资源管理系统','步骤交付物',NULL,1,170,NULL,'2025-12-11 12:06:03','2025-12-11 12:06:03'),(173,'项目产生的其他材料','档博通综合档案馆馆藏资源管理系统','里程碑交付物',NULL,0,NULL,7,'2025-12-11 12:06:03','2025-12-11 12:06:03'),(174,'上线试运行申请（用户签字版）','档博通综合档案馆馆藏资源管理系统','里程碑交付物',NULL,1,NULL,8,'2025-12-11 12:06:03','2025-12-11 12:06:03'),(175,'试运行报告（如需）','档博通综合档案馆馆藏资源管理系统','里程碑交付物',NULL,0,NULL,9,'2025-12-11 12:06:03','2025-12-11 12:06:03'),(176,'项目验收报告','档博通综合档案馆馆藏资源管理系统','里程碑交付物',NULL,1,NULL,9,'2025-12-11 12:06:03','2025-12-11 12:06:03');
/*!40000 ALTER TABLE `standard_deliverable` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `standard_milestone`
--

DROP TABLE IF EXISTS `standard_milestone`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `standard_milestone` (
  `milestone_id` bigint NOT NULL AUTO_INCREMENT COMMENT '里程碑ID（主键）',
  `milestone_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '里程碑名称',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`milestone_id`) USING BTREE,
  KEY `idx_milestone_name` (`milestone_name`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='标准里程碑信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `standard_milestone`
--

LOCK TABLES `standard_milestone` WRITE;
/*!40000 ALTER TABLE `standard_milestone` DISABLE KEYS */;
INSERT INTO `standard_milestone` VALUES (1,'01项目正式启动','2025-12-11 12:00:42','2025-12-11 12:00:42'),(2,'02需求确定','2025-12-11 12:00:42','2025-12-11 12:00:42'),(3,'03完成标准产品功能配置','2025-12-11 12:00:42','2025-12-11 12:00:42'),(4,'04完成数据迁移','2025-12-11 12:00:42','2025-12-11 12:00:42'),(5,'05完成接口开发集成','2025-12-11 12:00:42','2025-12-11 12:00:42'),(6,'06完成个性化功能开发','2025-12-11 12:00:42','2025-12-11 12:00:42'),(7,'07完成系统上线前功能调整及其他准备工作','2025-12-11 12:00:42','2025-12-11 12:00:42'),(8,'08完成系统上线及试运行','2025-12-11 12:00:42','2025-12-11 12:00:42'),(9,'09完成项目验收','2025-12-11 12:00:42','2025-12-11 12:00:42');
/*!40000 ALTER TABLE `standard_milestone` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user` (
  `userId` bigint NOT NULL AUTO_INCREMENT COMMENT 'User ID',
  `userName` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Username',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Password',
  `organId` bigint DEFAULT NULL COMMENT 'Organization ID',
  `roleId` bigint DEFAULT NULL COMMENT 'Role ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Real Name',
  `locked` tinyint DEFAULT '0' COMMENT 'Lock Status: 0-Normal, 1-Locked',
  `createTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`userId`) USING BTREE,
  UNIQUE KEY `userName` (`userName`) USING BTREE,
  KEY `idx_user_name` (`userName`) USING BTREE,
  KEY `idx_organ_id` (`organId`) USING BTREE,
  KEY `idx_role_id` (`roleId`) USING BTREE,
  KEY `idx_user_create_time` (`createTime`) USING BTREE,
  KEY `idx_user_update_time` (`updateTime`) USING BTREE,
  CONSTRAINT `user_ibfk_1` FOREIGN KEY (`organId`) REFERENCES `organ` (`organId`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `user_ibfk_2` FOREIGN KEY (`roleId`) REFERENCES `role` (`roleId`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='User Table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'admin','$2a$10$83xFBa.d70Dr2M0xUnUD0OAZNweDHCs.WoKqgcWytnjrUWQFiiv.i',1,1,'系统管理员',0,'2025-12-11 11:56:27','2025-12-11 11:56:27'),(2,'wenxiaojun','$2a$10$8645OmmSlkqWcy8s8w7epuyBXlpIm.d98uUBFSJafKlmned6KApUa',2,7,'温晓军',0,'2025-12-11 11:56:27','2025-12-18 13:27:08'),(3,'jiayazhou','$2a$10$oaVXeM2VwjFRYjlaXlYIy.pC4O82BPEJg9AffXsHFofLl4E4lyYnm',6,4,'贾亚洲',0,'2025-12-11 11:56:27','2025-12-18 13:27:09'),(4,'wangzhile','$2a$10$9..AUfIPioEa5Sgq8xQqsOxW.kwTtYTa7wovsUf0g7yKcSDwbRoei',3,4,'王治乐',0,'2025-12-11 11:56:27','2025-12-18 13:27:10'),(5,'liutingjun','$2a$10$zLNR1hPKPxx2V/KG2GW.p.Fhkdb196rermGne23wBlLvWK.8sVzYu',3,4,'刘庭俊',0,'2025-12-11 11:56:27','2025-12-18 13:27:11'),(6,'gongxuefang','$2a$10$N5gsk8xgVQP2xZ5QCCELt.ySP8sKM.Iso/RTEu642bBLJDMcmYMg2',5,3,'巩雪芳',0,'2025-12-11 11:56:27','2025-12-18 13:27:12'),(7,'yanghuiqiang','$2a$10$KeBW4jj8eokSFQkpC04rW.lGdwf4y91fNMuA5lVevlDuX.waPYe7S',4,7,'杨慧强',0,'2025-12-11 11:56:27','2025-12-18 13:27:13'),(8,'jinkaituo','$2a$10$Mc9cxFIxq1wyAAQ0xFOzsuutpKT0MT1ULofqlLN6hyTYwSFG0Ifyy',4,4,'金开拓',0,'2025-12-11 11:56:27','2025-12-18 13:27:14'),(9,'tianliang','$2a$10$vyOSwThEB52B/2t8RNYQbOkIblkfkmPOw3PHl7dRKK20Ddgu5XXIW',4,4,'田亮',0,'2025-12-11 11:56:27','2025-12-18 13:27:14'),(10,'yanghan','$2a$10$XB5p4YDV6ZpUXZNwUY0eNO4XS1iz4RogencEm0.UKC7SOPtai4lye',2,7,'杨寒',0,'2025-12-11 11:56:27','2025-12-18 13:27:15'),(11,'wujun','$2a$10$uvOq0APWBW003.GEVXTR3eH35Iu0V2Zl9WP.Lk6vBdW/k70hmFdbm',2,2,'吴俊',0,'2025-12-11 11:56:27','2025-12-18 13:27:16'),(12,'chenlaiqian','$2a$10$q9d0jqxxtMK7.naszxi3MOpxPwxU8ctpAzrLufJ1jW6cv4mzHva8i',2,2,'陈来倩',0,'2025-12-11 11:56:27','2025-12-18 13:27:17'),(15,'chengrui','$2a$10$rrHtSPTuWQKM9V8AkwiDEeE9E3t6ZOt43ZVFLyI9rOt7jsXCPSzCS',6,4,'程瑞',0,'2025-12-18 13:29:09','2025-12-18 13:29:09'),(17,'xiaoshouzongjian1','$2a$10$lvcjBp7Vf82z452tO8mFuerNTmlyp6CdC2zrwFiHJnpQim9eQcwNe',2,5,'销售总监1',0,'2025-12-28 08:51:37','2025-12-28 08:51:37');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-12-28 21:01:50
