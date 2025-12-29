-- MySQL dump 10.13  Distrib 8.0.21, for Win64 (x86_64)
--
-- Host: localhost    Database: pms_db
-- ------------------------------------------------------
-- Server version	8.0.21

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
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
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `afterservice_deliverable` (
  `deliverableId` bigint NOT NULL AUTO_INCREMENT COMMENT '交付物ID，主键',
  `deliverableName` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '交付物名称',
  `filePath` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '文件路径',
  `fileSize` bigint DEFAULT NULL COMMENT '文件大小（字节）',
  `projectId` bigint NOT NULL COMMENT '运维项目ID，afterService_project表外键，必填',
  `eventId` bigint DEFAULT NULL COMMENT '运维事件ID，afterService_event表外键',
  `uploadUser` bigint NOT NULL COMMENT '上传人，user表外键，必填',
  `createTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`deliverableId`),
  KEY `idx_afterservice_deliverable_name` (`deliverableName`),
  KEY `idx_afterservice_deliverable_project` (`projectId`),
  KEY `idx_afterservice_deliverable_event` (`eventId`),
  KEY `idx_afterservice_deliverable_upload_user` (`uploadUser`),
  KEY `idx_afterservice_deliverable_file_path` (`filePath`),
  CONSTRAINT `fk_afterservice_deliverable_event` FOREIGN KEY (`eventId`) REFERENCES `afterservice_event` (`eventId`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_afterservice_deliverable_project` FOREIGN KEY (`projectId`) REFERENCES `afterservice_project` (`projectId`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_afterservice_deliverable_upload_user` FOREIGN KEY (`uploadUser`) REFERENCES `user` (`userId`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='运维交付物表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `afterservice_deliverable`
--

LOCK TABLES `afterservice_deliverable` WRITE;
/*!40000 ALTER TABLE `afterservice_deliverable` DISABLE KEYS */;
INSERT INTO `afterservice_deliverable` VALUES (3,'接口巡检-20251203-121319-014.png','afterServiceDeliverableFiles/MS-20251203114031-测试项目01/2025-12-03-接口巡检/接口巡检-20251203-121319-014.png',260280,3,2,1,'2025-12-03 04:13:19','2025-12-03 04:13:19');
/*!40000 ALTER TABLE `afterservice_deliverable` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `afterservice_event`
--

DROP TABLE IF EXISTS `afterservice_event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `afterservice_event` (
  `eventId` bigint NOT NULL AUTO_INCREMENT COMMENT '事件ID，主键',
  `eventName` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '事件名称，必填',
  `eventDate` date NOT NULL COMMENT '事件日期，必填',
  `eventType` enum('主动服务','响应服务') COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '事件类型，必填，值域为主动服务、响应服务',
  `serviceMode` enum('远程服务','现场服务') COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '服务方式，必填，值域为远程服务、现场服务',
  `director` bigint NOT NULL COMMENT '负责人，user表外键，必填',
  `eventhours` decimal(8,2) NOT NULL COMMENT '事件工时，必填',
  `eventDetails` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '事件描述（最长1000字符）',
  `afterServiceProjectId` bigint NOT NULL COMMENT '运维项目ID，afterService_project表外键，必填',
  `createTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `isComplete` tinyint(1) DEFAULT '0' COMMENT '是否完成',
  PRIMARY KEY (`eventId`),
  KEY `idx_afterservice_event_name` (`eventName`),
  KEY `idx_afterservice_event_date` (`eventDate`),
  KEY `idx_afterservice_event_type` (`eventType`),
  KEY `idx_afterservice_event_mode` (`serviceMode`),
  KEY `idx_afterservice_event_director` (`director`),
  KEY `idx_afterservice_event_project` (`afterServiceProjectId`),
  CONSTRAINT `fk_afterservice_event_director` FOREIGN KEY (`director`) REFERENCES `user` (`userId`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_afterservice_event_project` FOREIGN KEY (`afterServiceProjectId`) REFERENCES `afterservice_project` (`projectId`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='运维事件表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `afterservice_event`
--

LOCK TABLES `afterservice_event` WRITE;
/*!40000 ALTER TABLE `afterservice_event` DISABLE KEYS */;
INSERT INTO `afterservice_event` VALUES (2,'接口巡检','2025-12-03','响应服务','远程服务',3,0.50,'',3,'2025-12-03 04:12:16','2025-12-03 04:12:16',1),(3,'权限维护','2025-12-03','响应服务','远程服务',3,1.00,'',3,'2025-12-03 04:13:14','2025-12-03 04:13:14',1);
/*!40000 ALTER TABLE `afterservice_event` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `afterservice_project`
--

DROP TABLE IF EXISTS `afterservice_project`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `afterservice_project` (
  `projectId` bigint NOT NULL AUTO_INCREMENT COMMENT '项目ID，主键',
  `projectName` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '项目名称，必填',
  `arcSystem` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '档案系统，必填',
  `saleDirector` bigint NOT NULL COMMENT '销售负责人，user表外键，必填',
  `ServiceYear` int DEFAULT NULL COMMENT '运维年数',
  `startDate` date DEFAULT NULL COMMENT '开始日期',
  `endDate` date DEFAULT NULL COMMENT '结束日期',
  `serviceState` enum('免费运维期','付费运维','无付费运维','暂停运维') COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '运维状态，必填，值域为免费运维期、付费运维、无付费运维、暂停运维',
  `totalHours` decimal(10,2) DEFAULT NULL COMMENT '总工时',
  `createTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `customerId` bigint DEFAULT NULL COMMENT '客户ID',
  `projectNum` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '项目编号，自动生成，唯一',
  `serviceType` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '运维类型',
  `serviceDirector` bigint DEFAULT NULL COMMENT '运维负责人，user表外键',
  `constructingProjectId` bigint DEFAULT NULL COMMENT '关联的在建项目ID，外键',
  PRIMARY KEY (`projectId`),
  UNIQUE KEY `idx_afterservice_projectNum` (`projectNum`),
  KEY `idx_afterservice_project_name` (`projectName`),
  KEY `idx_afterservice_state` (`serviceState`),
  KEY `idx_afterservice_start_date` (`startDate`),
  KEY `idx_afterservice_end_date` (`endDate`),
  KEY `fk_afterservice_project_customer` (`customerId`),
  KEY `idx_afterservice_project_saleDirector` (`saleDirector`),
  KEY `idx_afterservice_service_director` (`serviceDirector`),
  KEY `idx_afterservice_constructing_project` (`constructingProjectId`),
  CONSTRAINT `fk_afterservice_constructing_project` FOREIGN KEY (`constructingProjectId`) REFERENCES `constructing_project` (`projectId`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_afterservice_project_customer` FOREIGN KEY (`customerId`) REFERENCES `customer` (`customerId`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_afterservice_project_saleDirector` FOREIGN KEY (`saleDirector`) REFERENCES `user` (`userId`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_afterservice_service_director` FOREIGN KEY (`serviceDirector`) REFERENCES `user` (`userId`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='运维项目表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `afterservice_project`
--

LOCK TABLES `afterservice_project` WRITE;
/*!40000 ALTER TABLE `afterservice_project` DISABLE KEYS */;
INSERT INTO `afterservice_project` VALUES (3,'测试项目01','综合档案管理系统专业版',4,1,'2025-12-03','2026-12-03','免费运维期',NULL,'2025-12-03 03:40:31','2025-12-03 09:20:42',1,'MS-20251203114031','我公司全权运维',2,NULL);
/*!40000 ALTER TABLE `afterservice_project` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `archievesoft`
--

DROP TABLE IF EXISTS `archievesoft`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `archievesoft` (
  `softId` bigint NOT NULL AUTO_INCREMENT COMMENT 'Software ID',
  `softName` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Software Name',
  `softVersion` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Software Version',
  `softType` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '产品类型',
  `createTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation Time',
  `updateTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
  PRIMARY KEY (`softId`),
  KEY `idx_soft_name` (`softName`),
  KEY `idx_soft_version` (`softVersion`),
  KEY `idx_create_time` (`createTime`),
  KEY `idx_soft_type` (`softType`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Archive Software Table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `archievesoft`
--

LOCK TABLES `archievesoft` WRITE;
/*!40000 ALTER TABLE `archievesoft` DISABLE KEYS */;
INSERT INTO `archievesoft` VALUES (1,'数字档案馆（室）一体化系统','V7.1.7','标准产品','2025-10-08 11:53:03','2025-10-18 11:53:20'),(2,'数字档案馆（室）一体化系统','V7.1.6','标准产品','2025-10-08 11:53:14','2025-10-18 11:53:36'),(3,'综合档案管理系统专业版','V5.1.5','标准产品','2025-10-08 11:53:27','2025-10-18 11:53:32'),(4,'综合档案管理系统网络版','V3.5.5','标准产品','2025-10-08 11:53:50','2025-10-18 11:53:24'),(6,'馆藏资源系统','V7.1.7','综合档案馆产品体系','2025-10-18 10:38:30','2025-10-18 11:53:39'),(7,'电子阅览室系统','V1.0','综合档案馆产品体系','2025-10-18 11:54:32','2025-10-18 11:54:32');
/*!40000 ALTER TABLE `archievesoft` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `channel_distributor`
--

DROP TABLE IF EXISTS `channel_distributor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `channel_distributor` (
  `channel_id` bigint NOT NULL AUTO_INCREMENT COMMENT '渠道ID（主键）',
  `channel_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '渠道名称',
  `contactor` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '联系人',
  `phone_number` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '联系方式',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `saleDirector` bigint DEFAULT NULL COMMENT '销售总监（关联user表）',
  PRIMARY KEY (`channel_id`),
  KEY `idx_channel_name` (`channel_name`),
  KEY `idx_contactor` (`contactor`),
  KEY `idx_sale_director` (`saleDirector`),
  CONSTRAINT `fk_channel_distributor_saleDirector` FOREIGN KEY (`saleDirector`) REFERENCES `user` (`userId`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='渠道商信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `channel_distributor`
--

LOCK TABLES `channel_distributor` WRITE;
/*!40000 ALTER TABLE `channel_distributor` DISABLE KEYS */;
INSERT INTO `channel_distributor` VALUES (1,'成都联运科技有限公司','李中全','19945112477','2025-10-08 11:54:01','2025-10-08 11:54:13',NULL),(2,'贵州秀城数字档案文化服务有限公司','解秀承','13281132001','2025-10-08 11:54:19','2025-10-08 11:54:19',NULL);
/*!40000 ALTER TABLE `channel_distributor` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `construct_deliverablefiles`
--

DROP TABLE IF EXISTS `construct_deliverablefiles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `construct_deliverablefiles` (
  `fileId` bigint NOT NULL AUTO_INCREMENT COMMENT '文件ID，主键',
  `filePath` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件路径，必填',
  `fileSize` bigint DEFAULT NULL COMMENT '文件大小（字节）',
  `uploadUser` bigint NOT NULL COMMENT '上传人，user表外键，必填',
  `deliverableId` bigint DEFAULT NULL COMMENT '交付物ID，construct_standard_deliverable表外键',
  `projectId` bigint DEFAULT NULL COMMENT '项目ID，constructing_project表外键',
  `projectStepId` bigint DEFAULT NULL COMMENT '项目步骤关系ID，project_sstep_relations表外键',
  `nstepId` bigint DEFAULT NULL COMMENT '非标步骤ID，nonstandard_construct_step表外键',
  `milestoneId` bigint DEFAULT NULL COMMENT '里程碑ID，construct_milestone表外键',
  `createTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`fileId`),
  KEY `idx_construct_deliverableFiles_file_path` (`filePath`),
  KEY `idx_construct_deliverableFiles_upload_user` (`uploadUser`),
  KEY `idx_construct_deliverableFiles_deliverable` (`deliverableId`),
  KEY `idx_construct_deliverableFiles_project` (`projectId`),
  KEY `idx_construct_deliverableFiles_nstep` (`nstepId`),
  KEY `idx_construct_deliverableFiles_milestone` (`milestoneId`),
  KEY `idx_construct_deliverableFiles_projectStep` (`projectStepId`),
  CONSTRAINT `fk_construct_deliverableFiles_deliverable` FOREIGN KEY (`deliverableId`) REFERENCES `standard_deliverable` (`deliverableId`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_construct_deliverableFiles_milestone` FOREIGN KEY (`milestoneId`) REFERENCES `construct_milestone` (`milestoneId`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_construct_deliverableFiles_nstep` FOREIGN KEY (`nstepId`) REFERENCES `nonstandard_construct_step` (`nstepId`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_construct_deliverableFiles_project` FOREIGN KEY (`projectId`) REFERENCES `constructing_project` (`projectId`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_construct_deliverableFiles_projectStep` FOREIGN KEY (`projectStepId`) REFERENCES `project_sstep_relations` (`relationId`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_construct_deliverableFiles_upload_user` FOREIGN KEY (`uploadUser`) REFERENCES `user` (`userId`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=70 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='交付物文件表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `construct_deliverablefiles`
--

LOCK TABLES `construct_deliverablefiles` WRITE;
/*!40000 ALTER TABLE `construct_deliverablefiles` DISABLE KEYS */;
INSERT INTO `construct_deliverablefiles` VALUES (65,'deliverableFiles/MS-20251203103739-测试项目01/02需求确定/历史数据迁移需求调研报告-04历史数据迁移需求调研-20251203-113629.png',260280,1,274,41,1755,NULL,358,'2025-12-03 03:36:29','2025-12-03 03:36:29'),(67,'deliverableFiles/MS-20251203103739-测试项目01/04完成数据迁移/数据迁移报告-20251204-201914.png',260280,5,302,41,NULL,NULL,367,'2025-12-04 12:19:14','2025-12-04 12:19:14'),(69,'deliverableFiles/MS-20251203103739-测试项目01/02需求确定/历史数据迁移需求调研报告-04历史数据迁移需求调研-20251210-213134.png',260280,1,274,41,1755,NULL,358,'2025-12-10 13:31:35','2025-12-10 13:31:35');
/*!40000 ALTER TABLE `construct_deliverablefiles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `construct_milestone`
--

DROP TABLE IF EXISTS `construct_milestone`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `construct_milestone` (
  `milestoneId` bigint NOT NULL AUTO_INCREMENT COMMENT '里程碑ID，主键',
  `milestoneName` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '里程碑名称，必填',
  `projectId` bigint DEFAULT NULL COMMENT '项目ID，constructing_project表外键',
  `milestonePeriod` int DEFAULT NULL COMMENT '里程碑花费工期（天数）',
  `iscomplete` tinyint(1) DEFAULT '0' COMMENT '是否达成，0-未达成，1-已达成',
  `completeDate` date DEFAULT NULL COMMENT '达成日期',
  `createTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`milestoneId`),
  KEY `idx_construct_milestone_complete` (`iscomplete`),
  KEY `idx_construct_milestone_complete_date` (`completeDate`),
  KEY `idx_construct_milestone_period` (`milestonePeriod`),
  KEY `idx_construct_milestone_project` (`projectId`),
  KEY `idx_construct_milestone_name` (`milestoneName`),
  CONSTRAINT `fk_construct_milestone_project` FOREIGN KEY (`projectId`) REFERENCES `constructing_project` (`projectId`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=369 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='里程碑表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `construct_milestone`
--

LOCK TABLES `construct_milestone` WRITE;
/*!40000 ALTER TABLE `construct_milestone` DISABLE KEYS */;
INSERT INTO `construct_milestone` VALUES (348,'01项目正式启动',40,NULL,0,NULL,'2025-12-02 13:43:37','2025-12-02 13:43:37'),(349,'02需求确定',40,NULL,0,NULL,'2025-12-02 13:43:37','2025-12-02 13:43:37'),(350,'03完成标准产品功能配置',40,NULL,0,NULL,'2025-12-02 13:43:37','2025-12-02 13:43:37'),(351,'04完成数据迁移',40,NULL,0,NULL,'2025-12-02 13:43:37','2025-12-02 13:43:37'),(352,'05完成接口开发集成',40,9,1,'2025-12-03','2025-12-02 13:43:37','2025-12-03 03:03:59'),(353,'06完成个性化功能开发',40,NULL,0,NULL,'2025-12-02 13:43:37','2025-12-02 13:43:37'),(354,'07完成系统上线前功能调整及其他准备工作',40,NULL,0,NULL,'2025-12-02 13:43:37','2025-12-02 13:43:37'),(355,'08完成系统上线及试运行',40,NULL,0,NULL,'2025-12-02 13:43:37','2025-12-02 13:43:37'),(356,'09完成项目验收',40,NULL,0,NULL,'2025-12-02 13:43:37','2025-12-02 13:43:37'),(358,'02需求确定',41,8,1,'2025-12-07','2025-12-03 02:38:12','2025-12-03 03:09:06'),(361,'05完成接口开发集成',41,6,1,'2025-12-04','2025-12-03 02:38:12','2025-12-03 03:11:23'),(366,'06完成个性化功能开发',41,12,1,'2025-12-04','2025-12-03 03:08:24','2025-12-03 03:10:44'),(367,'04完成数据迁移',41,6,1,'2025-12-03','2025-12-03 03:08:24','2025-12-03 03:11:23'),(368,'07完成系统上线前功能调整及其他准备工作',41,3,1,'2025-12-12','2025-12-03 03:08:24','2025-12-03 03:10:44');
/*!40000 ALTER TABLE `construct_milestone` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `constructing_project`
--

DROP TABLE IF EXISTS `constructing_project`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `constructing_project` (
  `projectId` bigint NOT NULL AUTO_INCREMENT COMMENT '项目ID（主键）',
  `projectNum` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '项目编号',
  `year` int NOT NULL COMMENT '年度',
  `projectName` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '项目名称',
  `projectType` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '项目类型',
  `projectLeader` bigint DEFAULT NULL COMMENT '项目负责人ID（外键）',
  `saleLeader` bigint DEFAULT NULL COMMENT '商务负责人ID（外键）',
  `projectState` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '待开始' COMMENT '项目状态',
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
  `receivedMoney` decimal(15,2) NOT NULL DEFAULT '0.00' COMMENT '已回款金额',
  `unreceiveMoney` decimal(15,2) NOT NULL DEFAULT '0.00' COMMENT '未回款金额',
  `acceptanceDate` date DEFAULT NULL COMMENT '项目验收日期',
  `customerId` bigint DEFAULT NULL COMMENT '客户ID，customer表外键',
  `constructContent` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '项目建设内容（最多100字符）',
  `isCommitAfterSale` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否移交运维（0-未移交，1-已移交）',
  PRIMARY KEY (`projectId`),
  UNIQUE KEY `projectNum` (`projectNum`),
  KEY `idx_project_num` (`projectNum`),
  KEY `idx_year` (`year`),
  KEY `idx_project_state` (`projectState`),
  KEY `idx_start_date` (`startDate`),
  KEY `idx_project_leader` (`projectLeader`),
  KEY `idx_sale_leader` (`saleLeader`),
  KEY `idx_is_agent` (`isAgent`),
  KEY `idx_project_value` (`value`),
  KEY `idx_acceptance_date` (`acceptanceDate`),
  KEY `idx_constructing_project_customer` (`customerId`),
  KEY `idx_soft_id` (`softId`),
  KEY `idx_channel_id` (`channelId`),
  CONSTRAINT `fk_channel_id` FOREIGN KEY (`channelId`) REFERENCES `channel_distributor` (`channel_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_constructing_project_customer` FOREIGN KEY (`customerId`) REFERENCES `customer` (`customerId`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_project_leader` FOREIGN KEY (`projectLeader`) REFERENCES `user` (`userId`) ON DELETE SET NULL,
  CONSTRAINT `fk_sale_leader` FOREIGN KEY (`saleLeader`) REFERENCES `user` (`userId`) ON DELETE SET NULL,
  CONSTRAINT `fk_soft_id` FOREIGN KEY (`softId`) REFERENCES `archievesoft` (`softId`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=42 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `constructing_project`
--

LOCK TABLES `constructing_project` WRITE;
/*!40000 ALTER TABLE `constructing_project` DISABLE KEYS */;
INSERT INTO `constructing_project` VALUES (40,'MS-20251202214317',2025,'北京市城建勘测设计院档案系统建设项目','软件开发',3,4,'进行中',1,'2025-12-02','2025-12-31',NULL,29,NULL,'2025-12-02 13:43:37','2025-12-03 08:03:26',0,NULL,NULL,0.00,0.00,NULL,2,'标准产品/接口开发/数据迁移/个性化功能开发/用户培训/系统上线试运行',0),(41,'MS-20251203103739',2025,'测试项目01','软件开发',2,6,'已完成',3,'2025-12-02','2025-12-14',NULL,12,NULL,'2025-12-03 02:38:12','2025-12-03 09:23:35',0,NULL,NULL,0.00,0.00,NULL,1,'接口开发/数据迁移/个性化功能开发/用户培训',1);
/*!40000 ALTER TABLE `constructing_project` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `customer`
--

DROP TABLE IF EXISTS `customer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customer` (
  `customerId` bigint NOT NULL AUTO_INCREMENT COMMENT '客户ID，主键',
  `customerName` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '客户名称，必填',
  `contact` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '联系人，必填',
  `phoneNumber` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '联系方式，必填',
  `province` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '省份，必填',
  `customerRank` enum('战略客户','重要客户','一般客户') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '一般客户' COMMENT '客户等级，值域为战略客户、重要客户、一般客户',
  `createTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `saleLeader` bigint DEFAULT NULL COMMENT '销售负责人ID',
  `ifDeal` tinyint(1) DEFAULT '0' COMMENT '是否成交',
  `customerOwner` enum('自有客户','渠道客户') COLLATE utf8mb4_unicode_ci DEFAULT '自有客户' COMMENT '客户归属：自有客户、渠道客户',
  `channelId` bigint DEFAULT NULL COMMENT '渠道ID，关联channel_distributor表',
  PRIMARY KEY (`customerId`),
  KEY `idx_customer_name` (`customerName`),
  KEY `idx_customer_rank` (`customerRank`),
  KEY `idx_customer_province` (`province`),
  KEY `idx_customer_contact` (`contact`),
  KEY `fk_customer_saleLeader` (`saleLeader`),
  KEY `fk_customer_channel_distributor` (`channelId`),
  CONSTRAINT `fk_customer_channel_distributor` FOREIGN KEY (`channelId`) REFERENCES `channel_distributor` (`channel_id`),
  CONSTRAINT `fk_customer_saleLeader` FOREIGN KEY (`saleLeader`) REFERENCES `user` (`userId`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customer`
--

LOCK TABLES `customer` WRITE;
/*!40000 ALTER TABLE `customer` DISABLE KEYS */;
INSERT INTO `customer` VALUES (1,'北控水务','陈老师','19945112477','北京','战略客户','2025-10-08 11:57:05','2025-12-03 09:49:53',6,0,'自有客户',NULL),(2,'北京城建勘测院','文老师','18612922676','北京','一般客户','2025-10-08 11:57:18','2025-12-03 09:49:47',4,0,'自有客户',NULL);
/*!40000 ALTER TABLE `customer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `databasebackup`
--

DROP TABLE IF EXISTS `databasebackup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `databasebackup` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `fileName` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '备份文件名',
  `backupDate` datetime NOT NULL COMMENT '备份时间',
  `backupState` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '备份状态',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_backup_date` (`backupDate`),
  KEY `idx_backup_state` (`backupState`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据库备份记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `databasebackup`
--

LOCK TABLES `databasebackup` WRITE;
/*!40000 ALTER TABLE `databasebackup` DISABLE KEYS */;
/*!40000 ALTER TABLE `databasebackup` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `interface`
--

DROP TABLE IF EXISTS `interface`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `interface` (
  `interfaceId` bigint NOT NULL AUTO_INCREMENT COMMENT '接口ID（主键）',
  `projectId` bigint DEFAULT NULL COMMENT '项目ID（外键）',
  `interfaceType` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接口类型（必填）',
  `integrationSysName` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '集成系统名称（必填）',
  `integrationSysManufacturer` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '集成系统厂商（必填）',
  `archieveDataCategory` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '归档数据类别',
  `archieveInterfaceImpl` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '归档接口实现方式',
  `milestoneId` bigint DEFAULT NULL COMMENT '里程碑ID（外键）',
  `createTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`interfaceId`),
  KEY `idx_interface_project` (`projectId`),
  KEY `idx_interface_type` (`interfaceType`),
  KEY `idx_interface_milestone` (`milestoneId`),
  KEY `idx_interface_integration_sys` (`integrationSysName`),
  KEY `idx_interface_manufacturer` (`integrationSysManufacturer`),
  CONSTRAINT `fk_interface_milestone` FOREIGN KEY (`milestoneId`) REFERENCES `construct_milestone` (`milestoneId`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_interface_project` FOREIGN KEY (`projectId`) REFERENCES `constructing_project` (`projectId`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=44 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='接口表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `interface`
--

LOCK TABLES `interface` WRITE;
/*!40000 ALTER TABLE `interface` DISABLE KEYS */;
INSERT INTO `interface` VALUES (41,40,'数据归档接口','oa','支援',NULL,NULL,352,'2025-12-02 13:51:13','2025-12-02 13:51:13'),(42,40,'门类同步','库房系统','鑫泰格',NULL,NULL,352,'2025-12-02 13:51:34','2025-12-02 13:51:34'),(43,41,'数据归档接口','oa','泛微',NULL,NULL,361,'2025-12-03 02:38:27','2025-12-03 02:38:27');
/*!40000 ALTER TABLE `interface` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `migration_history`
--

DROP TABLE IF EXISTS `migration_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `migration_history` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Migration ID',
  `version` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Migration Version (e.g., 001, 002)',
  `script_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Script File Name',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT 'Migration Description',
  `checksum` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Script Content Checksum (MD5)',
  `executed_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Execution Time',
  `execution_time_ms` bigint DEFAULT NULL COMMENT 'Execution Time in Milliseconds',
  `success` tinyint(1) DEFAULT '1' COMMENT 'Execution Success Status',
  `error_message` text COLLATE utf8mb4_unicode_ci COMMENT 'Error Message if Failed',
  PRIMARY KEY (`id`),
  UNIQUE KEY `version` (`version`),
  KEY `idx_version` (`version`),
  KEY `idx_executed_at` (`executed_at`),
  KEY `idx_success` (`success`)
) ENGINE=InnoDB AUTO_INCREMENT=186 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Database Migration History Table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `migration_history`
--

LOCK TABLES `migration_history` WRITE;
/*!40000 ALTER TABLE `migration_history` DISABLE KEYS */;
INSERT INTO `migration_history` VALUES (1,'001','001_create_migration_table.sql','Create migration history table for database version management',NULL,'2025-10-08 11:44:31',NULL,1,NULL),(2,'002','002_create_user_role_organ_tables.sql','Create user, role, organ tables with foreign key relationships','fd639560164f4f4b931cf13529e6b4f7','2025-10-08 11:44:31',86,1,NULL),(3,'008','008_create_constructing_project_table.sql','No description','96fafd2b65c4fe527ce604cb928000f4','2025-10-08 11:44:31',165,1,NULL),(4,'010','010_add_constructing_project_fields.sql','No description','dbbd69f4e85b2410e0fa3a1c09168b8c','2025-10-08 11:44:31',123,1,NULL),(5,'011','011_create_customer_table.sql','No description','839b28df91608ceb31b51769c7d3e519','2025-10-08 11:44:31',59,1,NULL),(6,'012','012_create_afterservice_project_table.sql','No description','a0977e0b812e71707f807976f29a7422','2025-10-08 11:44:31',122,1,NULL),(7,'013','013_add_customer_id_to_constructing_project.sql','No description','f1640cb635622ef2bb14e8d6a1f5110b','2025-10-08 11:44:31',101,1,NULL),(8,'014','014_create_standard_construct_step_table.sql','No description','f9ff60c467ae68c73f2bfb7952735bdf','2025-10-08 11:44:31',165,1,NULL),(9,'015','015_create_construct_deliverable_table.sql','No description','dc5c9d9767f713a2cf02dadce6fa93da','2025-10-08 11:44:32',78,1,NULL),(10,'016','016_create_construct_milestone_table.sql','No description','f1ab3963a44fa2138f0c8c47965d0e7b','2025-10-08 11:44:32',43,1,NULL),(11,'017','017_create_nonstandard_construct_step_table.sql','No description','182cd93a412b81ab2c98b28703b2b941','2025-10-08 11:44:32',102,1,NULL),(12,'018','018_create_afterservice_event_table.sql','No description','6a28222d0663173bdb7d4fc293929260','2025-10-08 11:44:32',196,1,NULL),(13,'019','019_create_afterservice_deliverable_table.sql','No description','15231d144947a02abd0222b2869d4ac1','2025-10-08 11:44:32',200,1,NULL),(14,'020','020_create_project_relations_table.sql','No description','b7417b1e0d7f38c395d1bf8688dc9e42','2025-10-08 11:44:32',342,1,NULL),(15,'021','021_insert_admin_default_user.sql','Insert admin default user with initial password \'admin\'','d8d4bf1e72fd8aed0bbc528d88968fbe','2025-10-08 11:44:32',7,1,NULL),(16,'022','022_cleanup_user_table_extra_columns.sql','Remove extra columns that were added by Hibernate auto-ddl','e80b8ca9acc5e9445e9d1def9ff2faae','2025-10-08 11:44:32',7,1,NULL),(17,'023','023_update_admin_password.sql','Update admin user password to \'admin123\'','109267e1398b10a32c4f4e8d686b8538','2025-10-08 11:44:32',6,1,NULL),(18,'024','024_insert_default_organ.sql','Insert default top-level organization \"管软信息技术（北京）有限公司\"','49385f8aa2687abd7f39c18f637361d2','2025-10-08 11:44:32',6,1,NULL),(19,'025','025_add_role_description_column.sql','Align database schema with Role entity by adding optional description field','7208306eb26af8acecdf66937f15b37a','2025-10-08 11:44:32',13,1,NULL),(20,'026','026_insert_default_roles.sql','Insert default system roles for the application','078a3ccb21184c268e85e1f7804b64f4','2025-10-08 11:44:32',3,1,NULL),(21,'027','027_modify_standard_construct_step_table.sql','No description','7f2b96d7c741c8a4f5e4c602f69a8c1a','2025-10-08 11:44:33',122,1,NULL),(22,'028','028_modify_project_relations_table.sql','No description','690267df24a235abfcf0347f9965fcdf','2025-10-08 11:44:33',231,1,NULL),(23,'029','029_modify_construct_deliverable_table.sql','No description','bb3e46a7eb846d91ae3881e1d91bc9fb','2025-10-08 11:44:33',244,1,NULL),(24,'030','030_create_construct_deliverableFiles_table.sql','No description','efa8d295904c7194d2ad3d73a5d3e8e2','2025-10-08 11:44:33',346,1,NULL),(25,'031','031_modify_nonstandard_construct_step_table.sql','No description','3da18e9008ecb1ffd8d06286b52d298a','2025-10-08 11:44:34',89,1,NULL),(26,'032','032_modify_construct_milestone_table.sql','No description','35d0990447887328a8ce0c9831032c7e','2025-10-08 11:44:34',88,1,NULL),(27,'034','034_add_milestoneName_to_construct_milestone.sql','No description','420e76a907206036cf071f0f6500f7c0','2025-10-08 11:44:34',45,1,NULL),(28,'035','035_create_archieveSoft_table.sql','Create archieveSoft table for software archive management','1e9e24538d45ea9a9a30f0c871f33b1f','2025-10-08 11:44:34',17,1,NULL),(29,'036','036_add_timestamps_to_archieveSoft.sql','Add createTime and updateTime columns to archieveSoft table','2970d5fbb320de67e078ebb33b459fed','2025-10-08 11:44:34',39,1,NULL),(30,'037','037_create_channel_distributor_table.sql','No description','734ada521e0d7aef21bac9d254fc0ac1','2025-10-08 11:44:34',36,1,NULL),(31,'038','038_add_timestamps_to_missing_tables.sql','Add createTime and updateTime fields to tables that are missing them','51f9a45227fc94fbc7424dddf92ffd47','2025-10-08 11:44:34',267,1,NULL),(32,'039','039_create_standard_milestone_table.sql','No description','a228a89a6b4dec7ce3879b1eb7fd8606','2025-10-08 11:44:34',47,1,NULL),(33,'040','040_add_systemName_to_standard_construct_step.sql','No description','bc034b990ea77ee33668b860ac86f3c0','2025-10-08 11:44:34',43,1,NULL),(34,'041','041_add_smilestoneId_to_standard_construct_step.sql','No description','703eab8babf6eec0f5b3ec9b8339ac75','2025-10-08 11:44:34',87,1,NULL),(35,'042','042_add_smilestoneId_to_nonstandard_construct_step.sql','No description','9e8b407e1831251a5f41c98e10ae505b','2025-10-08 11:44:34',145,1,NULL),(36,'043','043_create_interface_table.sql','No description','ed4179a4127f67e753c27bfc10e2afad','2025-10-08 11:44:35',205,1,NULL),(37,'044','044_rename_construct_standard_deliverable_to_standard_deliverable.sql','No description','ca9e84d965c368c3f1b3d0b100aeaf56','2025-10-08 11:44:35',83,1,NULL),(38,'045','045_add_deliverableType_to_standard_deliverable.sql','No description','40cc44c9a8848caca052f3865c35e9d1','2025-10-08 11:44:35',53,1,NULL),(39,'046','046_fix_standard_deliverable_milestone_foreign_key.sql','No description','a6d12d8ff59a74bf1918e2ee284c9512','2025-10-08 11:44:35',91,1,NULL),(40,'047','047_modify_constructing_project_soft_channel_foreign_keys.sql','No description','abc6bba8b58e52c2150b826b3fe2ce47','2025-10-08 11:50:34',426,1,NULL),(41,'048','048_add_construct_content_to_constructing_project.sql','No description','71f39ad6e5b821d7f10a0816aa91adeb','2025-10-08 12:35:16',47,1,NULL),(42,'049','049_add_softType_to_archieveSoft.sql','Add 产品类型 (softType) field to archieveSoft (base product module)','d0e0687bd079f61341f96acfd5db7d54','2025-10-18 11:22:47',71,1,NULL),(43,'050','050_add_deliveryTempletePath_to_standard_deliverable.sql','No description','2059f946988480ff6ddb9e6e1b6f8dbd','2025-10-19 08:20:32',66,1,NULL),(44,'051','051_add_stepStatus_to_project_sstep_relations.sql','No description','2a7536fbf1bbd225daa47ff8f9bda944','2025-11-08 03:31:53',48,1,NULL),(45,'052','052_set_default_stepStatus.sql','No description','f599c90343032b55dac5db4536480367','2025-11-08 03:31:53',13,1,NULL),(46,'053','053_remove_sstepId_from_interface.sql','No description','77247a705ec94bc316247f8ccdffdbba','2025-11-08 03:31:53',47,1,NULL),(47,'054','054_create_personal_develope_table.sql','No description','f31d18f52c0fcea7b4f399dbb2338833','2025-11-08 03:31:53',127,1,NULL),(48,'055','055_add_interfaceId_personalDevId_to_project_sstep_relations.sql','No description','5df399b77da6d5ddc7fcb359d327eaf8','2025-11-08 03:31:53',199,1,NULL),(49,'056','056_add_milestoneId_to_project_sstep_relations.sql','No description','2f0b8aa227e56bccc8750ad8a7817255','2025-11-08 12:23:20',122,1,NULL),(65,'058','058_modify_construct_deliverableFiles_sstepId_to_projectStepId.sql','No description','85aadee9d25fe4a560fab3e98d95372b','2025-11-12 14:37:32',129,1,NULL),(173,'057','057_add_milestoneId_to_construct_deliverableFiles.sql','No description','9c2b6baa57224dc9aeae3dd82b970355','2025-11-17 05:37:40',8,1,NULL),(174,'059','059_add_customerId_to_afterservice_project.sql','No description','37c93fc628dc9625a9e5e335a5236518','2025-12-01 14:11:44',102,1,NULL),(175,'060','060_add_saleLeader_to_customer.sql','No description','4c315895e715f42e6095a4d609950c4a','2025-12-01 14:11:44',124,1,NULL),(176,'061','061_rename_director_to_saleDirector_in_afterservice_project.sql','No description','dd9d27b718f31872bf38b846c4949b6c','2025-12-01 14:11:45',108,1,NULL),(177,'062','062_add_eventDetails_to_afterservice_event.sql','No description','67e689f1e4da3655536caaf6a94992b3','2025-12-01 14:11:45',51,1,NULL),(178,'063','063_add_projectNum_to_afterservice_project.sql','No description','d2d4b88787634140d2b4c12e574e85ae','2025-12-01 14:11:45',77,1,NULL),(179,'064','064_add_isComplete_to_afterservice_event.sql','No description','141015611b32d30d66bf67998fb82dfa','2025-12-01 14:11:45',16,1,NULL),(180,'065','065_update_afterservice_project_table.sql','No description','1f720f8c5187c8b5158ae5a50cf10122','2025-12-01 14:11:45',112,1,NULL),(181,'066','066_add_constructingProjectId_to_afterservice_project.sql','No description','294e3a2b8b11e32d1915d9ce45580e87','2025-12-01 14:11:45',97,1,NULL),(182,'067','067_add_isCommitAfterSale_to_constructing_project.sql','No description','a558f68ee693820c89f0c153717e1958','2025-12-01 14:11:45',35,1,NULL),(183,'068','068_add_channel_fields_to_customer.sql','No description','6665b1df333cd137a41ab1d7bebd920f','2025-12-29 01:23:23',171,1,NULL),(184,'069','069_add_saleDirector_to_channel_distributor.sql','No description','12025b5a9624f628979a71428324d381','2025-12-29 01:23:23',101,1,NULL),(185,'070','070_create_databaseBackup_table.sql','No description','9b81780686ab0afdf5194fbb1561a54b','2025-12-29 01:23:23',56,1,NULL);
/*!40000 ALTER TABLE `migration_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `nonstandard_construct_step`
--

DROP TABLE IF EXISTS `nonstandard_construct_step`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `nonstandard_construct_step` (
  `nstepId` bigint NOT NULL AUTO_INCREMENT COMMENT '非标准步骤ID，主键',
  `nstepName` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '非标准步骤名，必填',
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
  PRIMARY KEY (`nstepId`),
  KEY `idx_nonstandard_step_name` (`nstepName`),
  KEY `idx_nonstandard_step_director` (`director`),
  KEY `idx_nonstandard_step_plan_start` (`planStartDate`),
  KEY `idx_nonstandard_step_plan_end` (`planEndDate`),
  KEY `idx_nonstandard_step_project` (`projectId`),
  KEY `idx_nonstandard_step_milestone` (`smilestoneId`),
  CONSTRAINT `fk_nonstandard_step_director` FOREIGN KEY (`director`) REFERENCES `user` (`userId`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_nonstandard_step_milestone` FOREIGN KEY (`smilestoneId`) REFERENCES `standard_milestone` (`milestone_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_nonstandard_step_project` FOREIGN KEY (`projectId`) REFERENCES `constructing_project` (`projectId`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='非标准步骤表';
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
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `organ` (
  `organId` bigint NOT NULL AUTO_INCREMENT COMMENT 'Organization ID',
  `organName` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Organization Name',
  `parentOrganId` bigint DEFAULT NULL COMMENT 'Parent Organization ID',
  `path` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Organization Path',
  `createTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`organId`),
  KEY `idx_organ_name` (`organName`),
  KEY `idx_parent_organ` (`parentOrganId`),
  KEY `idx_organ_create_time` (`createTime`),
  KEY `idx_organ_update_time` (`updateTime`),
  CONSTRAINT `organ_ibfk_1` FOREIGN KEY (`parentOrganId`) REFERENCES `organ` (`organId`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Organization Table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `organ`
--

LOCK TABLES `organ` WRITE;
/*!40000 ALTER TABLE `organ` DISABLE KEYS */;
INSERT INTO `organ` VALUES (1,'管软信息技术（北京）有限公司',NULL,'/1','2025-10-08 11:44:34','2025-10-08 11:44:34'),(2,'项目部',1,'/1/1','2025-10-08 11:54:34','2025-10-08 11:54:34'),(3,'研发部',1,'/1/1','2025-10-08 11:54:39','2025-10-08 11:54:39'),(4,'测试部',1,'/1/1','2025-10-08 11:54:45','2025-10-08 11:54:45'),(5,'销售部',1,'/1/1','2025-10-08 11:54:50','2025-10-08 11:54:50');
/*!40000 ALTER TABLE `organ` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `personal_develope`
--

DROP TABLE IF EXISTS `personal_develope`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `personal_develope` (
  `personalDevId` bigint NOT NULL AUTO_INCREMENT COMMENT '个性化开发ID（主键）',
  `personalDevName` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '个性化开发名称',
  `projectId` bigint DEFAULT NULL COMMENT '项目ID（constructing_project外键）',
  `milestoneId` bigint DEFAULT NULL COMMENT '里程碑ID（construct_milestone外键）',
  PRIMARY KEY (`personalDevId`),
  KEY `idx_personal_develope_project` (`projectId`),
  KEY `idx_personal_develope_milestone` (`milestoneId`),
  KEY `idx_personal_develope_name` (`personalDevName`),
  CONSTRAINT `fk_personal_develope_milestone` FOREIGN KEY (`milestoneId`) REFERENCES `construct_milestone` (`milestoneId`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_personal_develope_project` FOREIGN KEY (`projectId`) REFERENCES `constructing_project` (`projectId`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='个性化开发表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `personal_develope`
--

LOCK TABLES `personal_develope` WRITE;
/*!40000 ALTER TABLE `personal_develope` DISABLE KEYS */;
INSERT INTO `personal_develope` VALUES (16,'权限',41,366),(17,'流程',41,366),(18,'用印流程',40,353);
/*!40000 ALTER TABLE `personal_develope` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `project_sstep_relations`
--

DROP TABLE IF EXISTS `project_sstep_relations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
  `stepStatus` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT '未开始' COMMENT '步骤状态（未开始/进行中/已完成）',
  `interfaceId` bigint DEFAULT NULL COMMENT '接口ID（interface表外键）',
  `personalDevId` bigint DEFAULT NULL COMMENT '个性化开发ID（personal_develope表外键）',
  `createTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `milestoneId` bigint DEFAULT NULL COMMENT '项目里程碑ID，construct_milestone表外键',
  PRIMARY KEY (`relationId`),
  KEY `idx_project_relations_project` (`projectId`),
  KEY `idx_project_relations_sstep` (`sstepId`),
  KEY `idx_project_relations_deliverable` (`deliverableId`),
  KEY `idx_project_sstep_relations_director` (`director`),
  KEY `idx_project_sstep_relations_plan_start` (`planStartDate`),
  KEY `idx_project_sstep_relations_plan_end` (`planEndDate`),
  KEY `idx_project_sstep_relations_interface` (`interfaceId`),
  KEY `idx_project_sstep_relations_personal_dev` (`personalDevId`),
  KEY `idx_psr_milestoneId` (`milestoneId`),
  CONSTRAINT `fk_project_relations_deliverable` FOREIGN KEY (`deliverableId`) REFERENCES `standard_deliverable` (`deliverableId`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_project_relations_project` FOREIGN KEY (`projectId`) REFERENCES `constructing_project` (`projectId`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_project_relations_sstep` FOREIGN KEY (`sstepId`) REFERENCES `standard_construct_step` (`sstepId`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_project_sstep_relations_director` FOREIGN KEY (`director`) REFERENCES `user` (`userId`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_project_sstep_relations_interface` FOREIGN KEY (`interfaceId`) REFERENCES `interface` (`interfaceId`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_project_sstep_relations_personal_dev` FOREIGN KEY (`personalDevId`) REFERENCES `personal_develope` (`personalDevId`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_psr_milestone` FOREIGN KEY (`milestoneId`) REFERENCES `construct_milestone` (`milestoneId`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1777 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目步骤关系表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `project_sstep_relations`
--

LOCK TABLES `project_sstep_relations` WRITE;
/*!40000 ALTER TABLE `project_sstep_relations` DISABLE KEYS */;
INSERT INTO `project_sstep_relations` VALUES (1705,40,402,NULL,2,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-02 13:43:37','2025-12-02 13:43:37',348),(1706,40,403,NULL,2,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-02 13:43:37','2025-12-02 13:43:37',349),(1707,40,404,NULL,2,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-02 13:43:37','2025-12-02 13:43:37',349),(1708,40,408,NULL,2,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-02 13:43:37','2025-12-02 13:43:37',350),(1709,40,409,NULL,2,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-02 13:43:37','2025-12-02 13:43:37',350),(1710,40,410,NULL,2,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-02 13:43:37','2025-12-02 13:43:37',350),(1711,40,411,NULL,2,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-02 13:43:37','2025-12-02 13:43:37',350),(1712,40,412,NULL,2,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-02 13:43:37','2025-12-02 13:43:37',350),(1713,40,413,NULL,2,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-02 13:43:37','2025-12-02 13:43:37',350),(1714,40,414,NULL,2,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-02 13:43:37','2025-12-02 13:43:37',350),(1715,40,415,NULL,2,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-02 13:43:37','2025-12-02 13:43:37',350),(1716,40,416,NULL,2,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-02 13:43:37','2025-12-02 13:43:37',350),(1717,40,417,NULL,2,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-02 13:43:37','2025-12-02 13:43:37',350),(1718,40,418,NULL,2,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-02 13:43:37','2025-12-02 13:43:37',350),(1719,40,419,NULL,2,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-02 13:43:37','2025-12-02 13:43:37',350),(1720,40,420,NULL,2,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-02 13:43:37','2025-12-02 13:43:37',350),(1721,40,421,NULL,2,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-02 13:43:37','2025-12-02 13:43:37',350),(1722,40,422,NULL,2,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-02 13:43:37','2025-12-02 13:43:37',350),(1723,40,423,NULL,2,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-02 13:43:37','2025-12-02 13:43:37',350),(1724,40,424,NULL,2,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-02 13:43:37','2025-12-02 13:43:37',350),(1725,40,425,NULL,2,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-02 13:43:37','2025-12-02 13:43:37',350),(1726,40,426,NULL,2,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-02 13:43:37','2025-12-02 13:43:37',350),(1727,40,427,NULL,2,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-02 13:43:37','2025-12-02 13:43:37',350),(1728,40,428,NULL,2,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-02 13:43:37','2025-12-02 13:43:37',350),(1729,40,429,NULL,2,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-02 13:43:37','2025-12-02 13:43:37',350),(1730,40,430,NULL,2,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-02 13:43:37','2025-12-02 13:43:37',350),(1731,40,405,NULL,2,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-02 13:43:37','2025-12-02 13:43:37',349),(1732,40,431,NULL,2,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-02 13:43:37','2025-12-02 13:43:37',351),(1733,40,432,NULL,2,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-02 13:43:37','2025-12-02 13:43:37',351),(1734,40,433,NULL,2,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-02 13:43:37','2025-12-02 13:43:37',351),(1735,40,434,NULL,2,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-02 13:43:37','2025-12-02 13:43:37',351),(1736,40,445,NULL,2,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-02 13:43:37','2025-12-02 13:43:37',354),(1737,40,444,NULL,2,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-02 13:43:37','2025-12-02 13:43:37',354),(1738,40,446,NULL,2,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-02 13:43:37','2025-12-02 13:43:37',355),(1739,40,447,NULL,2,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-02 13:43:37','2025-12-02 13:43:37',356),(1740,40,406,NULL,2,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-02 13:43:37','2025-12-02 13:43:37',349),(1741,40,407,NULL,2,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,NULL,'2025-12-02 13:43:37','2025-12-02 13:43:37',349),(1742,40,435,NULL,2,NULL,NULL,'2025-12-02','2025-12-03',NULL,2,'已完成',41,NULL,'2025-12-02 13:51:13','2025-12-02 13:51:46',352),(1743,40,436,NULL,NULL,NULL,NULL,'2025-12-03','2025-12-03',NULL,1,'已完成',41,NULL,'2025-12-02 13:51:13','2025-12-02 13:51:51',352),(1744,40,437,NULL,NULL,NULL,NULL,'2025-12-03','2025-12-03',NULL,1,'已完成',41,NULL,'2025-12-02 13:51:13','2025-12-02 13:51:55',352),(1745,40,438,NULL,2,NULL,NULL,'2025-12-03','2025-12-03',NULL,1,'已完成',41,NULL,'2025-12-02 13:51:13','2025-12-02 13:51:59',352),(1746,40,435,NULL,2,NULL,NULL,'2025-12-02','2025-12-02',NULL,1,'已完成',42,NULL,'2025-12-02 13:51:34','2025-12-02 13:52:07',352),(1747,40,436,NULL,NULL,NULL,NULL,'2025-12-03','2025-12-03',NULL,1,'已完成',42,NULL,'2025-12-02 13:51:34','2025-12-02 13:52:11',352),(1748,40,437,NULL,NULL,NULL,NULL,'2025-12-03','2025-12-03',NULL,1,'已完成',42,NULL,'2025-12-02 13:51:34','2025-12-02 13:52:16',352),(1749,40,438,NULL,2,NULL,NULL,'2025-12-03','2025-12-03',NULL,1,'已完成',42,NULL,'2025-12-02 13:51:34','2025-12-02 13:52:21',352),(1750,41,452,NULL,2,NULL,NULL,'2025-12-03','2025-12-04',NULL,2,'已完成',NULL,NULL,'2025-12-03 02:38:12','2025-12-03 02:38:38',358),(1751,41,480,NULL,2,NULL,NULL,'2025-12-02','2025-12-03',NULL,2,'已完成',43,NULL,'2025-12-03 02:38:27','2025-12-03 02:38:47',361),(1752,41,481,NULL,NULL,NULL,NULL,'2025-12-03','2025-12-03',NULL,1,'已完成',43,NULL,'2025-12-03 02:38:27','2025-12-03 03:11:15',361),(1753,41,482,NULL,NULL,NULL,NULL,'2025-12-03','2025-12-03',NULL,1,'已完成',43,NULL,'2025-12-03 02:38:27','2025-12-03 02:38:56',361),(1754,41,483,NULL,2,NULL,NULL,'2025-12-03','2025-12-04',NULL,2,'已完成',43,NULL,'2025-12-03 02:38:27','2025-12-03 03:06:25',361),(1755,41,451,NULL,2,NULL,NULL,'2025-12-03','2025-12-07',NULL,5,'已完成',NULL,NULL,'2025-12-03 03:08:24','2025-12-03 03:08:46',358),(1756,41,476,NULL,2,NULL,NULL,'2025-12-01','2025-12-01',NULL,1,'已完成',NULL,NULL,'2025-12-03 03:08:24','2025-12-03 03:10:54',367),(1757,41,477,NULL,2,NULL,NULL,'2025-12-01','2025-12-02',NULL,2,'已完成',NULL,NULL,'2025-12-03 03:08:24','2025-12-03 03:11:00',367),(1758,41,478,NULL,2,NULL,NULL,'2025-12-02','2025-12-03',NULL,2,'已完成',NULL,NULL,'2025-12-03 03:08:24','2025-12-03 03:11:05',367),(1759,41,479,NULL,2,NULL,NULL,'2025-12-03','2025-12-03',NULL,1,'已完成',NULL,NULL,'2025-12-03 03:08:24','2025-12-03 03:11:09',367),(1760,41,490,NULL,2,NULL,NULL,'2025-12-10','2025-12-12',NULL,3,'已完成',NULL,NULL,'2025-12-03 03:08:24','2025-12-03 03:10:32',368),(1761,41,453,NULL,2,NULL,NULL,'2025-12-03','2025-12-03',NULL,1,'已完成',NULL,NULL,'2025-12-03 03:08:24','2025-12-03 03:08:53',358),(1762,41,484,NULL,2,NULL,NULL,'2025-12-01','2025-12-02',NULL,2,'已完成',NULL,16,'2025-12-03 03:08:34','2025-12-03 03:09:13',366),(1763,41,485,NULL,NULL,NULL,NULL,'2025-12-02','2025-12-02',NULL,1,'已完成',NULL,16,'2025-12-03 03:08:34','2025-12-03 03:09:17',366),(1764,41,486,NULL,NULL,NULL,NULL,'2025-12-02','2025-12-03',NULL,2,'已完成',NULL,16,'2025-12-03 03:08:34','2025-12-03 03:09:22',366),(1765,41,487,NULL,NULL,NULL,NULL,'2025-12-03','2025-12-03',NULL,1,'已完成',NULL,16,'2025-12-03 03:08:34','2025-12-03 03:09:26',366),(1766,41,488,NULL,2,NULL,NULL,'2025-12-04','2025-12-04',NULL,1,'已完成',NULL,16,'2025-12-03 03:08:34','2025-12-03 03:09:30',366),(1767,41,484,NULL,2,NULL,NULL,'2025-12-03','2025-12-03',NULL,1,'已完成',NULL,17,'2025-12-03 03:09:06','2025-12-03 03:09:36',366),(1768,41,485,NULL,NULL,NULL,NULL,'2025-12-04','2025-12-04',NULL,1,'已完成',NULL,17,'2025-12-03 03:09:06','2025-12-03 03:09:40',366),(1769,41,486,NULL,NULL,NULL,NULL,'2025-12-04','2025-12-04',NULL,1,'已完成',NULL,17,'2025-12-03 03:09:06','2025-12-03 03:09:46',366),(1770,41,487,NULL,NULL,NULL,NULL,'2025-12-04','2025-12-04',NULL,1,'已完成',NULL,17,'2025-12-03 03:09:06','2025-12-03 03:09:51',366),(1771,41,488,NULL,2,NULL,NULL,'2025-12-04','2025-12-04',NULL,1,'已完成',NULL,17,'2025-12-03 03:09:06','2025-12-03 03:09:59',366),(1772,40,439,NULL,2,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,18,'2025-12-03 05:29:23','2025-12-03 05:29:23',353),(1773,40,440,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,18,'2025-12-03 05:29:23','2025-12-03 05:29:23',353),(1774,40,441,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,18,'2025-12-03 05:29:23','2025-12-03 05:29:23',353),(1775,40,442,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,18,'2025-12-03 05:29:23','2025-12-03 05:29:23',353),(1776,40,443,NULL,2,NULL,NULL,NULL,NULL,NULL,NULL,'未开始',NULL,18,'2025-12-03 05:29:23','2025-12-03 05:29:23',353);
/*!40000 ALTER TABLE `project_sstep_relations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `role`
--

DROP TABLE IF EXISTS `role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `role` (
  `roleId` bigint NOT NULL AUTO_INCREMENT COMMENT 'Role ID',
  `roleName` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Role Name',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT 'Role Description',
  `createTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`roleId`),
  KEY `idx_role_name` (`roleName`),
  KEY `idx_role_create_time` (`createTime`),
  KEY `idx_role_update_time` (`updateTime`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Role Table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `role`
--

LOCK TABLES `role` WRITE;
/*!40000 ALTER TABLE `role` DISABLE KEYS */;
INSERT INTO `role` VALUES (1,'管理员','管理员角色，拥有系统管理权限','2025-10-08 11:44:34','2025-10-08 11:44:34'),(2,'销售角色','销售人员角色，负责客户开发、项目销售等业务','2025-10-08 11:44:34','2025-10-08 11:44:34'),(3,'售后角色','售后服务人员角色，负责项目维护、客户服务等工作','2025-10-08 11:44:34','2025-10-08 11:44:34'),(4,'项目经理角色','项目经理角色，负责项目的规划、执行和管理','2025-10-08 11:44:34','2025-10-08 11:44:34'),(5,'销售总监角色','销售总监角色，负责销售团队管理和销售策略制定','2025-10-08 11:44:34','2025-10-08 11:44:34'),(6,'项目总监角色','项目总监角色，负责项目团队管理和项目战略规划','2025-10-08 11:44:34','2025-10-08 11:44:34'),(7,'公司领导角色','公司领导角色，拥有公司级别的决策和管理权限','2025-10-08 11:44:34','2025-10-08 11:44:34');
/*!40000 ALTER TABLE `role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `standard_construct_step`
--

DROP TABLE IF EXISTS `standard_construct_step`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `standard_construct_step` (
  `sstepId` bigint NOT NULL AUTO_INCREMENT COMMENT '标准步骤ID，主键',
  `sstepName` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标准步骤名，必填',
  `type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '步骤类型',
  `systemName` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '档案系统名称',
  `smilestoneId` bigint DEFAULT NULL COMMENT '标准里程碑ID（外键）',
  `createTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`sstepId`),
  KEY `idx_standard_step_name` (`sstepName`),
  KEY `idx_standard_step_type` (`type`),
  KEY `idx_standard_step_system_name` (`systemName`),
  KEY `idx_standard_step_milestone` (`smilestoneId`),
  CONSTRAINT `fk_standard_step_milestone` FOREIGN KEY (`smilestoneId`) REFERENCES `standard_milestone` (`milestone_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=574 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标准步骤表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `standard_construct_step`
--

LOCK TABLES `standard_construct_step` WRITE;
/*!40000 ALTER TABLE `standard_construct_step` DISABLE KEYS */;
INSERT INTO `standard_construct_step` VALUES (402,'01召开项目启动会','标准产品','数字档案馆（室）一体化系统',11,'2025-11-08 03:36:07','2025-11-08 03:36:07'),(403,'02系统应用规模及部署需求调研','标准产品','数字档案馆（室）一体化系统',12,'2025-11-08 03:36:07','2025-11-08 03:36:07'),(404,'03档案业务现状及需求调研','标准产品','数字档案馆（室）一体化系统',12,'2025-11-08 03:36:07','2025-11-08 03:36:07'),(405,'04历史数据迁移需求调研','数据迁移','数字档案馆（室）一体化系统',12,'2025-11-08 03:36:07','2025-11-08 03:36:07'),(406,'05业务系统接口需求调研','接口开发','数字档案馆（室）一体化系统',12,'2025-11-08 03:36:07','2025-11-08 03:36:07'),(407,'06个性化功能需求调研','个性化功能开发','数字档案馆（室）一体化系统',12,'2025-11-08 03:36:07','2025-11-08 03:36:07'),(408,'07系统安装部署','标准产品','数字档案馆（室）一体化系统',13,'2025-11-08 03:36:07','2025-11-08 03:36:07'),(409,'08系统参数配置','标准产品','数字档案馆（室）一体化系统',13,'2025-11-08 03:36:07','2025-11-08 03:36:07'),(410,'09存储配置','标准产品','数字档案馆（室）一体化系统',13,'2025-11-08 03:36:07','2025-11-08 03:36:07'),(411,'10导入通用初始化库','标准产品','数字档案馆（室）一体化系统',13,'2025-11-08 03:36:07','2025-11-08 03:36:07'),(412,'11设置全宗','标准产品','数字档案馆（室）一体化系统',13,'2025-11-08 03:36:07','2025-11-08 03:36:07'),(413,'12设置门类结构、规则及字典','标准产品','数字档案馆（室）一体化系统',13,'2025-11-08 03:36:07','2025-11-08 03:36:07'),(414,'13设置档案门类','标准产品','数字档案馆（室）一体化系统',13,'2025-11-08 03:36:07','2025-11-08 03:36:07'),(415,'14关联门类结构并检查门类规则','标准产品','数字档案馆（室）一体化系统',13,'2025-11-08 03:36:07','2025-11-08 03:36:07'),(416,'15设置（同步）组织机构','标准产品','数字档案馆（室）一体化系统',13,'2025-11-08 03:36:07','2025-11-08 03:36:07'),(417,'16设置（同步）用户','标准产品','数字档案馆（室）一体化系统',13,'2025-11-08 03:36:07','2025-11-08 03:36:07'),(418,'17设置角色权限及用户赋权','标准产品','数字档案馆（室）一体化系统',13,'2025-11-08 03:36:07','2025-11-08 03:36:07'),(419,'18设置档案利用表单及流程','标准产品','数字档案馆（室）一体化系统',13,'2025-11-08 03:36:07','2025-11-08 03:36:07'),(420,'19设置档案移交归档表单及流程','标准产品','数字档案馆（室）一体化系统',13,'2025-11-08 03:36:07','2025-11-08 03:36:07'),(421,'20设置其他表单及流程（如需要）','标准产品','数字档案馆（室）一体化系统',13,'2025-11-08 03:36:07','2025-11-08 03:36:07'),(422,'21设置数据报表','标准产品','数字档案馆（室）一体化系统',13,'2025-11-08 03:36:07','2025-11-08 03:36:07'),(423,'22设置档案资源统计、移交接收统计、档案利用统计','标准产品','数字档案馆（室）一体化系统',13,'2025-11-08 03:36:07','2025-11-08 03:36:07'),(424,'23配置手动登记借阅模板','标准产品','数字档案馆（室）一体化系统',13,'2025-11-08 03:36:07','2025-11-08 03:36:07'),(425,'24配置“三合一”表（如需要）','标准产品','数字档案馆（室）一体化系统',13,'2025-11-08 03:36:07','2025-11-08 03:36:07'),(426,'25配置水印','标准产品','数字档案馆（室）一体化系统',13,'2025-11-08 03:36:07','2025-11-08 03:36:07'),(427,'26配置备份策略','标准产品','数字档案馆（室）一体化系统',13,'2025-11-08 03:36:07','2025-11-08 03:36:07'),(428,'27配置四性检测（如需要）','标准产品','数字档案馆（室）一体化系统',13,'2025-11-08 03:36:07','2025-11-08 03:36:07'),(429,'28完成其他功能设置（如需要，例如发布管理、库房配置、菜单管理）','标准产品','数字档案馆（室）一体化系统',13,'2025-11-08 03:36:07','2025-11-08 03:36:07'),(430,'29标准产品整体功能跑通确认及调整','标准产品','数字档案馆（室）一体化系统',13,'2025-11-08 03:36:07','2025-11-08 03:36:07'),(431,'30数据迁移技术方式确定','数据迁移','数字档案馆（室）一体化系统',14,'2025-11-08 03:36:07','2025-11-08 03:36:07'),(432,'31执行数据迁移','数据迁移','数字档案馆（室）一体化系统',14,'2025-11-08 03:36:07','2025-11-08 03:36:07'),(433,'32数据迁移后核验','数据迁移','数字档案馆（室）一体化系统',14,'2025-11-08 03:36:07','2025-11-08 03:36:07'),(434,'33存量数据创建全文索引，并开启增量索引任务','数据迁移','数字档案馆（室）一体化系统',14,'2025-11-08 03:36:07','2025-11-08 03:36:07'),(435,'34接口需求及集成方式最终确定','接口开发','数字档案馆（室）一体化系统',15,'2025-11-08 03:36:07','2025-11-08 03:36:07'),(436,'35接口开发','接口开发','数字档案馆（室）一体化系统',15,'2025-11-08 03:36:07','2025-11-08 03:36:07'),(437,'36接口联调测试','接口开发','数字档案馆（室）一体化系统',15,'2025-11-08 03:36:07','2025-11-08 03:36:07'),(438,'37接口部署','接口开发','数字档案馆（室）一体化系统',15,'2025-11-08 03:36:07','2025-11-08 03:36:07'),(439,'38个性化需求整体最终确定','个性化功能开发','数字档案馆（室）一体化系统',16,'2025-11-08 03:36:07','2025-11-08 03:36:07'),(440,'39个性化需求整体设计','个性化功能开发','数字档案馆（室）一体化系统',16,'2025-11-08 03:36:07','2025-11-08 03:36:07'),(441,'40个性化需求开发','个性化功能开发','数字档案馆（室）一体化系统',16,'2025-11-08 03:36:07','2025-11-08 03:36:07'),(442,'41个性化需求测试验证','个性化功能开发','数字档案馆（室）一体化系统',16,'2025-11-08 03:36:07','2025-11-08 03:36:07'),(443,'42个性化需求部署及用户验证','个性化功能开发','数字档案馆（室）一体化系统',16,'2025-11-08 03:36:07','2025-11-08 03:36:07'),(444,'43系统上线前功能调整及其他准备工作','系统上线试运行','数字档案馆（室）一体化系统',17,'2025-11-08 03:36:07','2025-11-08 03:36:07'),(445,'44用户培训','用户培训','数字档案馆（室）一体化系统',17,'2025-11-08 03:36:07','2025-11-08 03:36:07'),(446,'45系统正式上线','系统上线试运行','数字档案馆（室）一体化系统',18,'2025-11-08 03:36:07','2025-11-08 03:36:07'),(447,'46项目正式验收','系统上线试运行','数字档案馆（室）一体化系统',19,'2025-11-08 03:36:07','2025-11-08 03:36:07'),(448,'01召开项目启动会','标准产品','综合档案管理系统专业版',11,'2025-11-08 03:36:16','2025-11-08 03:36:16'),(449,'02系统应用规模及部署需求调研','标准产品','综合档案管理系统专业版',12,'2025-11-08 03:36:16','2025-11-08 03:36:16'),(450,'03档案业务现状及需求调研','标准产品','综合档案管理系统专业版',12,'2025-11-08 03:36:16','2025-11-08 03:36:16'),(451,'04历史数据迁移需求调研','数据迁移','综合档案管理系统专业版',12,'2025-11-08 03:36:16','2025-11-08 03:36:16'),(452,'05业务系统接口需求调研','接口开发','综合档案管理系统专业版',12,'2025-11-08 03:36:16','2025-11-08 03:36:16'),(453,'06个性化功能需求调研','个性化功能开发','综合档案管理系统专业版',12,'2025-11-08 03:36:16','2025-11-08 03:36:16'),(454,'07系统安装部署','标准产品','综合档案管理系统专业版',13,'2025-11-08 03:36:16','2025-11-08 03:36:16'),(455,'08系统参数配置','标准产品','综合档案管理系统专业版',13,'2025-11-08 03:36:16','2025-11-08 03:36:16'),(456,'09存储配置','标准产品','综合档案管理系统专业版',13,'2025-11-08 03:36:16','2025-11-08 03:36:16'),(457,'10导入通用初始化库','标准产品','综合档案管理系统专业版',13,'2025-11-08 03:36:16','2025-11-08 03:36:16'),(458,'11设置全宗','标准产品','综合档案管理系统专业版',13,'2025-11-08 03:36:16','2025-11-08 03:36:16'),(459,'12设置门类结构、规则及字典','标准产品','综合档案管理系统专业版',13,'2025-11-08 03:36:16','2025-11-08 03:36:16'),(460,'13设置档案门类','标准产品','综合档案管理系统专业版',13,'2025-11-08 03:36:16','2025-11-08 03:36:16'),(461,'14关联门类结构并检查门类规则','标准产品','综合档案管理系统专业版',13,'2025-11-08 03:36:16','2025-11-08 03:36:16'),(462,'15设置（同步）组织机构','标准产品','综合档案管理系统专业版',13,'2025-11-08 03:36:16','2025-11-08 03:36:16'),(463,'16设置（同步）用户','标准产品','综合档案管理系统专业版',13,'2025-11-08 03:36:16','2025-11-08 03:36:16'),(464,'17设置角色权限及用户赋权','标准产品','综合档案管理系统专业版',13,'2025-11-08 03:36:16','2025-11-08 03:36:16'),(465,'18设置档案利用审核人','标准产品','综合档案管理系统专业版',13,'2025-11-08 03:36:16','2025-11-08 03:36:16'),(466,'19设置档案归档审核人','标准产品','综合档案管理系统专业版',13,'2025-11-08 03:36:16','2025-11-08 03:36:16'),(467,'20设置数据报表','标准产品','综合档案管理系统专业版',13,'2025-11-08 03:36:16','2025-11-08 03:36:16'),(468,'21设置档案资源统计、移交接收统计、档案利用统计','标准产品','综合档案管理系统专业版',13,'2025-11-08 03:36:16','2025-11-08 03:36:16'),(469,'22配置手动登记借阅模板','标准产品','综合档案管理系统专业版',13,'2025-11-08 03:36:16','2025-11-08 03:36:16'),(470,'23配置“三合一”表（如需要）','标准产品','综合档案管理系统专业版',13,'2025-11-08 03:36:16','2025-11-08 03:36:16'),(471,'24配置水印','标准产品','综合档案管理系统专业版',13,'2025-11-08 03:36:16','2025-11-08 03:36:16'),(472,'25配置备份策略','标准产品','综合档案管理系统专业版',13,'2025-11-08 03:36:16','2025-11-08 03:36:16'),(473,'26配置四性检测（如需要）','标准产品','综合档案管理系统专业版',13,'2025-11-08 03:36:16','2025-11-08 03:36:16'),(474,'27完成其他功能设置（如需要，例如库房配置、菜单管理、销毁业务等）','标准产品','综合档案管理系统专业版',13,'2025-11-08 03:36:16','2025-11-08 03:36:16'),(475,'28标准产品整体功能跑通确认及调整','标准产品','综合档案管理系统专业版',13,'2025-11-08 03:36:17','2025-11-08 03:36:17'),(476,'29数据迁移技术方式确定','数据迁移','综合档案管理系统专业版',14,'2025-11-08 03:36:17','2025-11-08 03:36:17'),(477,'30执行数据迁移','数据迁移','综合档案管理系统专业版',14,'2025-11-08 03:36:17','2025-11-08 03:36:17'),(478,'31数据迁移后核验','数据迁移','综合档案管理系统专业版',14,'2025-11-08 03:36:17','2025-11-08 03:36:17'),(479,'32存量数据创建全文索引，并开启增量索引任务','数据迁移','综合档案管理系统专业版',14,'2025-11-08 03:36:17','2025-11-08 03:36:17'),(480,'33接口需求及集成方式最终确定','接口开发','综合档案管理系统专业版',15,'2025-11-08 03:36:17','2025-11-08 03:36:17'),(481,'34接口开发','接口开发','综合档案管理系统专业版',15,'2025-11-08 03:36:17','2025-11-08 03:36:17'),(482,'35接口联调测试','接口开发','综合档案管理系统专业版',15,'2025-11-08 03:36:17','2025-11-08 03:36:17'),(483,'36接口部署','接口开发','综合档案管理系统专业版',15,'2025-11-08 03:36:17','2025-11-08 03:36:17'),(484,'37个性化需求整体最终确定','个性化功能开发','综合档案管理系统专业版',16,'2025-11-08 03:36:17','2025-11-08 03:36:17'),(485,'38个性化需求整体设计','个性化功能开发','综合档案管理系统专业版',16,'2025-11-08 03:36:17','2025-11-08 03:36:17'),(486,'39个性化需求开发','个性化功能开发','综合档案管理系统专业版',16,'2025-11-08 03:36:17','2025-11-08 03:36:17'),(487,'40个性化需求测试验证','个性化功能开发','综合档案管理系统专业版',16,'2025-11-08 03:36:17','2025-11-08 03:36:17'),(488,'41个性化需求部署及用户验证','个性化功能开发','综合档案管理系统专业版',16,'2025-11-08 03:36:17','2025-11-08 03:36:17'),(489,'42系统上线前功能调整及其他准备工作','系统上线试运行','综合档案管理系统专业版',17,'2025-11-08 03:36:17','2025-11-08 03:36:17'),(490,'43用户培训','用户培训','综合档案管理系统专业版',17,'2025-11-08 03:36:17','2025-11-08 03:36:17'),(491,'44系统正式上线','系统上线试运行','综合档案管理系统专业版',18,'2025-11-08 03:36:17','2025-11-08 03:36:17'),(492,'45项目正式验收','系统上线试运行','综合档案管理系统专业版',19,'2025-11-08 03:36:17','2025-11-08 03:36:17'),(493,'01召开项目启动会','标准产品','综合档案管理系统网络版',11,'2025-11-08 03:36:23','2025-11-08 03:36:23'),(494,'02系统应用规模及部署需求调研','标准产品','综合档案管理系统网络版',12,'2025-11-08 03:36:23','2025-11-08 03:36:23'),(495,'03档案业务现状及需求调研','标准产品','综合档案管理系统网络版',12,'2025-11-08 03:36:23','2025-11-08 03:36:23'),(496,'04历史数据迁移需求调研','数据迁移','综合档案管理系统网络版',12,'2025-11-08 03:36:23','2025-11-08 03:36:23'),(497,'05个性化功能需求调研','个性化功能开发','综合档案管理系统网络版',12,'2025-11-08 03:36:23','2025-11-08 03:36:23'),(498,'06系统安装部署','标准产品','综合档案管理系统网络版',13,'2025-11-08 03:36:23','2025-11-08 03:36:23'),(499,'07系统参数配置','标准产品','综合档案管理系统网络版',13,'2025-11-08 03:36:23','2025-11-08 03:36:23'),(500,'08存储配置','标准产品','综合档案管理系统网络版',13,'2025-11-08 03:36:23','2025-11-08 03:36:23'),(501,'09导入通用初始化库','标准产品','综合档案管理系统网络版',13,'2025-11-08 03:36:23','2025-11-08 03:36:23'),(502,'10设置全宗','标准产品','综合档案管理系统网络版',13,'2025-11-08 03:36:23','2025-11-08 03:36:23'),(503,'11设置门类结构、规则及字典','标准产品','综合档案管理系统网络版',13,'2025-11-08 03:36:23','2025-11-08 03:36:23'),(504,'12设置档案门类','标准产品','综合档案管理系统网络版',13,'2025-11-08 03:36:23','2025-11-08 03:36:23'),(505,'13关联门类结构并检查门类规则','标准产品','综合档案管理系统网络版',13,'2025-11-08 03:36:23','2025-11-08 03:36:23'),(506,'14设置组织机构','标准产品','综合档案管理系统网络版',13,'2025-11-08 03:36:23','2025-11-08 03:36:23'),(507,'15设置用户','标准产品','综合档案管理系统网络版',13,'2025-11-08 03:36:23','2025-11-08 03:36:23'),(508,'16设置角色权限及用户赋权','标准产品','综合档案管理系统网络版',13,'2025-11-08 03:36:23','2025-11-08 03:36:23'),(509,'17设置档案利用审核人','标准产品','综合档案管理系统网络版',13,'2025-11-08 03:36:23','2025-11-08 03:36:23'),(510,'18设置数据报表','标准产品','综合档案管理系统网络版',13,'2025-11-08 03:36:23','2025-11-08 03:36:23'),(511,'19设置档案统计','标准产品','综合档案管理系统网络版',13,'2025-11-08 03:36:23','2025-11-08 03:36:23'),(512,'20配置手动登记借阅模板','标准产品','综合档案管理系统网络版',13,'2025-11-08 03:36:23','2025-11-08 03:36:23'),(513,'21配置“三合一”表（如需要）','标准产品','综合档案管理系统网络版',13,'2025-11-08 03:36:23','2025-11-08 03:36:23'),(514,'22配置水印','标准产品','综合档案管理系统网络版',13,'2025-11-08 03:36:23','2025-11-08 03:36:23'),(515,'23配置备份策略','标准产品','综合档案管理系统网络版',13,'2025-11-08 03:36:23','2025-11-08 03:36:23'),(516,'24配置四性检测（如需要）','标准产品','综合档案管理系统网络版',13,'2025-11-08 03:36:23','2025-11-08 03:36:23'),(517,'25标准产品整体功能跑通确认及调整','标准产品','综合档案管理系统网络版',13,'2025-11-08 03:36:23','2025-11-08 03:36:23'),(518,'26数据迁移技术方式确定','数据迁移','综合档案管理系统网络版',14,'2025-11-08 03:36:23','2025-11-08 03:36:23'),(519,'27执行数据迁移','数据迁移','综合档案管理系统网络版',14,'2025-11-08 03:36:23','2025-11-08 03:36:23'),(520,'28数据迁移后核验','数据迁移','综合档案管理系统网络版',14,'2025-11-08 03:36:23','2025-11-08 03:36:23'),(521,'29存量数据创建全文索引，并开启增量索引任务','数据迁移','综合档案管理系统网络版',14,'2025-11-08 03:36:23','2025-11-08 03:36:23'),(522,'30个性化需求整体最终确定','个性化功能开发','综合档案管理系统网络版',16,'2025-11-08 03:36:23','2025-11-08 03:36:23'),(523,'31个性化需求整体设计','个性化功能开发','综合档案管理系统网络版',16,'2025-11-08 03:36:23','2025-11-08 03:36:23'),(524,'32个性化需求开发','个性化功能开发','综合档案管理系统网络版',16,'2025-11-08 03:36:23','2025-11-08 03:36:23'),(525,'33个性化需求测试验证','个性化功能开发','综合档案管理系统网络版',16,'2025-11-08 03:36:23','2025-11-08 03:36:23'),(526,'34个性化需求部署及用户验证','个性化功能开发','综合档案管理系统网络版',16,'2025-11-08 03:36:23','2025-11-08 03:36:23'),(527,'35系统上线前功能调整及其他准备工作','系统上线试运行','综合档案管理系统网络版',17,'2025-11-08 03:36:23','2025-11-08 03:36:23'),(528,'36用户培训','用户培训','综合档案管理系统网络版',17,'2025-11-08 03:36:23','2025-11-08 03:36:23'),(529,'37系统正式上线','系统上线试运行','综合档案管理系统网络版',18,'2025-11-08 03:36:23','2025-11-08 03:36:23'),(530,'38项目正式验收','系统上线试运行','综合档案管理系统网络版',19,'2025-11-08 03:36:23','2025-11-08 03:36:23'),(531,'01召开项目启动会','标准产品','馆藏资源系统',11,'2025-11-08 03:36:30','2025-11-08 03:36:30'),(532,'02系统部署需求调研','标准产品','馆藏资源系统',12,'2025-11-08 03:36:30','2025-11-08 03:36:30'),(533,'03档案业务现状及需求调研','标准产品','馆藏资源系统',12,'2025-11-08 03:36:30','2025-11-08 03:36:30'),(534,'04历史数据迁移需求调研','数据迁移','馆藏资源系统',12,'2025-11-08 03:36:30','2025-11-08 03:36:30'),(535,'05接口需求调研','接口开发','馆藏资源系统',12,'2025-11-08 03:36:30','2025-11-08 03:36:30'),(536,'06个性化功能需求调研','个性化功能开发','馆藏资源系统',12,'2025-11-08 03:36:30','2025-11-08 03:36:30'),(537,'07系统安装部署','标准产品','馆藏资源系统',13,'2025-11-08 03:36:30','2025-11-08 03:36:30'),(538,'08系统参数配置','标准产品','馆藏资源系统',13,'2025-11-08 03:36:30','2025-11-08 03:36:30'),(539,'09存储配置','标准产品','馆藏资源系统',13,'2025-11-08 03:36:30','2025-11-08 03:36:30'),(540,'10导入通用初始化库（不需要导档案门类）','标准产品','馆藏资源系统',13,'2025-11-08 03:36:30','2025-11-08 03:36:30'),(541,'11设置全宗','标准产品','馆藏资源系统',13,'2025-11-08 03:36:30','2025-11-08 03:36:30'),(542,'12设置门类结构、规则及字典','标准产品','馆藏资源系统',13,'2025-11-08 03:36:30','2025-11-08 03:36:30'),(543,'13设置档案门类','标准产品','馆藏资源系统',13,'2025-11-08 03:36:30','2025-11-08 03:36:30'),(544,'14关联门类结构并检查门类规则','标准产品','馆藏资源系统',13,'2025-11-08 03:36:30','2025-11-08 03:36:30'),(545,'15设置组织机构','标准产品','馆藏资源系统',13,'2025-11-08 03:36:30','2025-11-08 03:36:30'),(546,'16设置用户','标准产品','馆藏资源系统',13,'2025-11-08 03:36:30','2025-11-08 03:36:30'),(547,'17设置角色权限及用户赋权','标准产品','馆藏资源系统',13,'2025-11-08 03:36:30','2025-11-08 03:36:30'),(548,'18设置鉴定、销毁表单及流程（如需要）','标准产品','馆藏资源系统',13,'2025-11-08 03:36:30','2025-11-08 03:36:30'),(549,'19设置数据报表','标准产品','馆藏资源系统',13,'2025-11-08 03:36:30','2025-11-08 03:36:30'),(550,'20设置档案资源统计、移交接收统计、档案利用统计','标准产品','馆藏资源系统',13,'2025-11-08 03:36:30','2025-11-08 03:36:30'),(551,'21配置手动登记借阅模板','标准产品','馆藏资源系统',13,'2025-11-08 03:36:30','2025-11-08 03:36:30'),(552,'22配置水印','标准产品','馆藏资源系统',13,'2025-11-08 03:36:30','2025-11-08 03:36:30'),(553,'23配置备份策略','标准产品','馆藏资源系统',13,'2025-11-08 03:36:30','2025-11-08 03:36:30'),(554,'24配置四性检测（如需要）','标准产品','馆藏资源系统',13,'2025-11-08 03:36:30','2025-11-08 03:36:30'),(555,'25完成其他功能设置（如需要，例如发布管理、库房配置、菜单管理）','标准产品','馆藏资源系统',13,'2025-11-08 03:36:30','2025-11-08 03:36:30'),(556,'26标准产品整体功能跑通确认及调整','标准产品','馆藏资源系统',13,'2025-11-08 03:36:30','2025-11-08 03:36:30'),(557,'27数据迁移技术方式确定','数据迁移','馆藏资源系统',14,'2025-11-08 03:36:30','2025-11-08 03:36:30'),(558,'28执行数据迁移','数据迁移','馆藏资源系统',14,'2025-11-08 03:36:30','2025-11-08 03:36:30'),(559,'29数据迁移后核验','数据迁移','馆藏资源系统',14,'2025-11-08 03:36:30','2025-11-08 03:36:30'),(560,'30存量数据创建全文索引，并开启增量索引任务','数据迁移','馆藏资源系统',14,'2025-11-08 03:36:30','2025-11-08 03:36:30'),(561,'31接口需求及集成方式最终确定','接口开发','馆藏资源系统',15,'2025-11-08 03:36:30','2025-11-08 03:36:30'),(562,'32接口开发','接口开发','馆藏资源系统',15,'2025-11-08 03:36:30','2025-11-08 03:36:30'),(563,'33接口联调测试','接口开发','馆藏资源系统',15,'2025-11-08 03:36:30','2025-11-08 03:36:30'),(564,'34接口部署','接口开发','馆藏资源系统',15,'2025-11-08 03:36:30','2025-11-08 03:36:30'),(565,'35个性化需求整体最终确定','个性化功能开发','馆藏资源系统',16,'2025-11-08 03:36:30','2025-11-08 03:36:30'),(566,'36个性化需求整体设计','个性化功能开发','馆藏资源系统',16,'2025-11-08 03:36:30','2025-11-08 03:36:30'),(567,'37个性化需求开发','个性化功能开发','馆藏资源系统',16,'2025-11-08 03:36:30','2025-11-08 03:36:30'),(568,'38个性化需求测试验证','个性化功能开发','馆藏资源系统',16,'2025-11-08 03:36:30','2025-11-08 03:36:30'),(569,'39个性化需求部署及用户验证','个性化功能开发','馆藏资源系统',16,'2025-11-08 03:36:30','2025-11-08 03:36:30'),(570,'40系统上线前功能调整及其他准备工作','系统上线试运行','馆藏资源系统',17,'2025-11-08 03:36:30','2025-11-08 03:36:30'),(571,'41用户培训','用户培训','馆藏资源系统',17,'2025-11-08 03:36:30','2025-11-08 03:36:30'),(572,'42系统正式上线','系统上线试运行','馆藏资源系统',18,'2025-11-08 03:36:30','2025-11-08 03:36:30'),(573,'43项目正式验收','系统上线试运行','馆藏资源系统',19,'2025-11-08 03:36:30','2025-11-08 03:36:30');
/*!40000 ALTER TABLE `standard_construct_step` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `standard_deliverable`
--

DROP TABLE IF EXISTS `standard_deliverable`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `standard_deliverable` (
  `deliverableId` bigint NOT NULL AUTO_INCREMENT COMMENT '交付物ID，主键',
  `deliverableName` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '交付物名称',
  `systemName` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '系统名称',
  `deliverableType` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '交付物类型',
  `deliveryTempletePath` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '模板文件相对路径',
  `isMustLoad` tinyint(1) DEFAULT '0' COMMENT '是否必须上传，0-非必须，1-必须',
  `sstepId` bigint DEFAULT NULL COMMENT '标准步骤ID，standard_construct_step表外键',
  `milestoneId` bigint DEFAULT NULL COMMENT '里程碑ID，construct_milestone表外键',
  `createTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`deliverableId`),
  KEY `idx_construct_deliverable_name` (`deliverableName`),
  KEY `idx_construct_standard_deliverable_must_load` (`isMustLoad`),
  KEY `idx_construct_standard_deliverable_sstep` (`sstepId`),
  KEY `idx_standard_deliverable_system_name` (`systemName`),
  KEY `idx_standard_deliverable_type` (`deliverableType`),
  KEY `idx_standard_deliverable_milestone` (`milestoneId`),
  CONSTRAINT `fk_construct_standard_deliverable_sstep` FOREIGN KEY (`sstepId`) REFERENCES `standard_construct_step` (`sstepId`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_standard_deliverable_standard_milestone` FOREIGN KEY (`milestoneId`) REFERENCES `standard_milestone` (`milestone_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=400 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='交付物表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `standard_deliverable`
--

LOCK TABLES `standard_deliverable` WRITE;
/*!40000 ALTER TABLE `standard_deliverable` DISABLE KEYS */;
INSERT INTO `standard_deliverable` VALUES (224,'项目启动会汇报材料','数字档案馆（室）一体化系统','里程碑交付物','deliveryTempletes/数字档案馆（室）一体化系统/',1,NULL,11,'2025-11-08 03:36:38','2025-11-10 13:27:35'),(225,'系统应用规模及部署需求调研结果','数字档案馆（室）一体化系统','步骤交付物','docs/deliveryTempletes/数字档案馆（室）一体化系统/',1,403,NULL,'2025-11-08 03:36:38','2025-11-21 15:17:22'),(226,'档案业务现状及需求调研报告','数字档案馆（室）一体化系统','步骤交付物','docs/deliveryTempletes/数字档案馆（室）一体化系统/',1,404,NULL,'2025-11-08 03:36:38','2025-11-21 14:25:50'),(227,'历史数据迁移需求调研报告','数字档案馆（室）一体化系统','步骤交付物',NULL,1,405,NULL,'2025-11-08 03:36:38','2025-11-08 03:36:38'),(228,'业务系统接口需求调研报告','数字档案馆（室）一体化系统','步骤交付物','deliveryTempletes/数字档案馆（室）一体化系统/',1,406,NULL,'2025-11-08 03:36:38','2025-11-10 13:34:16'),(229,'个性化功能需求调研报告','数字档案馆（室）一体化系统','步骤交付物',NULL,1,407,NULL,'2025-11-08 03:36:38','2025-11-08 03:36:38'),(230,'项目整体需求调研报告','数字档案馆（室）一体化系统','里程碑交付物',NULL,1,NULL,12,'2025-11-08 03:36:38','2025-11-15 03:13:33'),(231,'系统安装部署文档','数字档案馆（室）一体化系统','步骤交付物',NULL,1,408,NULL,'2025-11-08 03:36:38','2025-11-08 03:36:38'),(232,'配置截图','数字档案馆（室）一体化系统','步骤交付物',NULL,0,409,NULL,'2025-11-08 03:36:38','2025-11-08 03:36:38'),(233,'配置截图','数字档案馆（室）一体化系统','步骤交付物',NULL,1,410,NULL,'2025-11-08 03:36:38','2025-11-08 03:36:38'),(234,'配置截图','数字档案馆（室）一体化系统','步骤交付物',NULL,0,411,NULL,'2025-11-08 03:36:38','2025-11-08 03:36:38'),(235,'配置截图','数字档案馆（室）一体化系统','步骤交付物',NULL,1,412,NULL,'2025-11-08 03:36:38','2025-11-08 03:36:38'),(236,'配置截图','数字档案馆（室）一体化系统','步骤交付物',NULL,1,413,NULL,'2025-11-08 03:36:38','2025-11-08 03:36:38'),(237,'配置截图','数字档案馆（室）一体化系统','步骤交付物',NULL,1,414,NULL,'2025-11-08 03:36:38','2025-11-08 03:36:38'),(238,'配置截图','数字档案馆（室）一体化系统','步骤交付物',NULL,0,415,NULL,'2025-11-08 03:36:38','2025-11-08 03:36:38'),(239,'配置截图','数字档案馆（室）一体化系统','步骤交付物',NULL,1,416,NULL,'2025-11-08 03:36:38','2025-11-08 03:36:38'),(240,'配置截图','数字档案馆（室）一体化系统','步骤交付物',NULL,0,417,NULL,'2025-11-08 03:36:38','2025-11-08 03:36:38'),(241,'配置截图','数字档案馆（室）一体化系统','步骤交付物',NULL,1,418,NULL,'2025-11-08 03:36:38','2025-11-08 03:36:38'),(242,'配置截图','数字档案馆（室）一体化系统','步骤交付物',NULL,1,419,NULL,'2025-11-08 03:36:38','2025-11-08 03:36:38'),(243,'配置截图','数字档案馆（室）一体化系统','步骤交付物',NULL,1,420,NULL,'2025-11-08 03:36:38','2025-11-08 03:36:38'),(244,'配置截图','数字档案馆（室）一体化系统','步骤交付物',NULL,0,421,NULL,'2025-11-08 03:36:38','2025-11-08 03:36:38'),(245,'配置截图','数字档案馆（室）一体化系统','步骤交付物',NULL,1,422,NULL,'2025-11-08 03:36:38','2025-11-08 03:36:38'),(246,'配置截图','数字档案馆（室）一体化系统','步骤交付物',NULL,1,423,NULL,'2025-11-08 03:36:38','2025-11-08 03:36:38'),(247,'配置截图','数字档案馆（室）一体化系统','步骤交付物',NULL,1,424,NULL,'2025-11-08 03:36:38','2025-11-08 03:36:38'),(248,'配置截图','数字档案馆（室）一体化系统','步骤交付物',NULL,0,425,NULL,'2025-11-08 03:36:38','2025-11-08 03:36:38'),(249,'配置截图','数字档案馆（室）一体化系统','步骤交付物',NULL,1,426,NULL,'2025-11-08 03:36:38','2025-11-08 03:36:38'),(250,'配置截图','数字档案馆（室）一体化系统','步骤交付物',NULL,1,427,NULL,'2025-11-08 03:36:38','2025-11-08 03:36:38'),(251,'配置截图','数字档案馆（室）一体化系统','步骤交付物',NULL,0,428,NULL,'2025-11-08 03:36:38','2025-11-08 03:36:38'),(252,'配置截图','数字档案馆（室）一体化系统','步骤交付物',NULL,0,429,NULL,'2025-11-08 03:36:38','2025-11-08 03:36:38'),(253,'标准产品功能配置报告','数字档案馆（室）一体化系统','里程碑交付物',NULL,1,NULL,13,'2025-11-08 03:36:38','2025-11-08 03:36:38'),(254,'数据迁移技术方案','数字档案馆（室）一体化系统','步骤交付物',NULL,1,431,NULL,'2025-11-08 03:36:38','2025-11-08 03:36:38'),(255,'数据迁移核验表','数字档案馆（室）一体化系统','步骤交付物',NULL,1,433,NULL,'2025-11-08 03:36:39','2025-11-08 03:36:39'),(256,'数据迁移报告','数字档案馆（室）一体化系统','里程碑交付物',NULL,1,NULL,14,'2025-11-08 03:36:39','2025-11-08 03:36:39'),(257,'接口需求文档（用户签字版）','数字档案馆（室）一体化系统','步骤交付物',NULL,1,435,NULL,'2025-11-08 03:36:39','2025-11-08 03:36:39'),(258,'接口文档','数字档案馆（室）一体化系统','步骤交付物','docs/deliveryTempletes/数字档案馆（室）一体化系统/',1,436,NULL,'2025-11-08 03:36:39','2025-11-21 15:30:36'),(259,'接口开发代码或配置文件','数字档案馆（室）一体化系统','步骤交付物',NULL,1,438,NULL,'2025-11-08 03:36:39','2025-11-15 03:08:26'),(260,'接口集成报告','数字档案馆（室）一体化系统','里程碑交付物',NULL,1,NULL,15,'2025-11-08 03:36:39','2025-11-08 03:36:39'),(261,'个性化需求文档（用户签字版）','数字档案馆（室）一体化系统','步骤交付物',NULL,1,439,NULL,'2025-11-08 03:36:39','2025-11-08 03:36:39'),(262,'个性化需求设计方案（用户签字版）','数字档案馆（室）一体化系统','步骤交付物',NULL,1,440,NULL,'2025-11-08 03:36:39','2025-11-08 03:36:39'),(263,'个性化需求开发代码','数字档案馆（室）一体化系统','步骤交付物',NULL,1,443,NULL,'2025-11-08 03:36:39','2025-11-15 03:09:11'),(264,'个性化需求开发报告','数字档案馆（室）一体化系统','里程碑交付物',NULL,1,NULL,16,'2025-11-08 03:36:39','2025-11-08 03:36:39'),(265,'产生变动的里程碑交付物','数字档案馆（室）一体化系统','步骤交付物',NULL,0,444,NULL,'2025-11-08 03:36:39','2025-11-08 03:36:39'),(266,'用户操作手册及培训材料','数字档案馆（室）一体化系统','步骤交付物',NULL,1,445,NULL,'2025-11-08 03:36:39','2025-11-08 03:36:39'),(267,'项目产生的其他材料','数字档案馆（室）一体化系统','里程碑交付物',NULL,0,NULL,17,'2025-11-08 03:36:39','2025-11-08 03:36:39'),(268,'上线试运行申请（用户签字版）','数字档案馆（室）一体化系统','里程碑交付物',NULL,1,NULL,18,'2025-11-08 03:36:39','2025-11-08 03:36:39'),(269,'试运行报告（如需）','数字档案馆（室）一体化系统','里程碑交付物',NULL,0,NULL,19,'2025-11-08 03:36:39','2025-11-08 03:36:39'),(270,'项目验收报告','数字档案馆（室）一体化系统','里程碑交付物',NULL,1,NULL,19,'2025-11-08 03:36:39','2025-11-08 03:36:39'),(271,'项目启动会汇报材料','综合档案管理系统专业版','里程碑交付物',NULL,1,NULL,11,'2025-11-08 03:36:47','2025-11-08 03:36:47'),(272,'系统应用规模及部署需求调研结果','综合档案管理系统专业版','步骤交付物',NULL,1,449,NULL,'2025-11-08 03:36:47','2025-11-08 03:36:47'),(273,'档案业务现状及需求调研报告','综合档案管理系统专业版','步骤交付物',NULL,1,450,NULL,'2025-11-08 03:36:47','2025-11-08 03:36:47'),(274,'历史数据迁移需求调研报告','综合档案管理系统专业版','步骤交付物',NULL,1,451,NULL,'2025-11-08 03:36:47','2025-11-08 03:36:47'),(275,'业务系统接口需求调研报告','综合档案管理系统专业版','步骤交付物',NULL,1,452,NULL,'2025-11-08 03:36:47','2025-11-08 03:36:47'),(276,'个性化功能需求调研报告','综合档案管理系统专业版','步骤交付物',NULL,1,453,NULL,'2025-11-08 03:36:47','2025-11-08 03:36:47'),(277,'项目整体需求调研报告','综合档案管理系统专业版','里程碑交付物',NULL,1,NULL,12,'2025-11-08 03:36:47','2025-11-08 03:36:47'),(278,'系统安装部署文档','综合档案管理系统专业版','步骤交付物',NULL,1,454,NULL,'2025-11-08 03:36:47','2025-11-08 03:36:47'),(279,'配置截图','综合档案管理系统专业版','步骤交付物',NULL,0,455,NULL,'2025-11-08 03:36:47','2025-11-08 03:36:47'),(280,'配置截图','综合档案管理系统专业版','步骤交付物',NULL,1,456,NULL,'2025-11-08 03:36:47','2025-11-08 03:36:47'),(281,'配置截图','综合档案管理系统专业版','步骤交付物',NULL,0,457,NULL,'2025-11-08 03:36:47','2025-11-08 03:36:47'),(282,'配置截图','综合档案管理系统专业版','步骤交付物',NULL,1,458,NULL,'2025-11-08 03:36:47','2025-11-08 03:36:47'),(283,'配置截图','综合档案管理系统专业版','步骤交付物',NULL,1,459,NULL,'2025-11-08 03:36:47','2025-11-08 03:36:47'),(284,'配置截图','综合档案管理系统专业版','步骤交付物',NULL,1,460,NULL,'2025-11-08 03:36:47','2025-11-08 03:36:47'),(285,'配置截图','综合档案管理系统专业版','步骤交付物',NULL,0,461,NULL,'2025-11-08 03:36:47','2025-11-08 03:36:47'),(286,'配置截图','综合档案管理系统专业版','步骤交付物',NULL,1,462,NULL,'2025-11-08 03:36:47','2025-11-08 03:36:47'),(287,'配置截图','综合档案管理系统专业版','步骤交付物',NULL,0,463,NULL,'2025-11-08 03:36:47','2025-11-08 03:36:47'),(288,'配置截图','综合档案管理系统专业版','步骤交付物',NULL,1,464,NULL,'2025-11-08 03:36:47','2025-11-08 03:36:47'),(289,'配置截图','综合档案管理系统专业版','步骤交付物',NULL,1,465,NULL,'2025-11-08 03:36:47','2025-11-08 03:36:47'),(290,'配置截图','综合档案管理系统专业版','步骤交付物',NULL,1,466,NULL,'2025-11-08 03:36:47','2025-11-08 03:36:47'),(291,'配置截图','综合档案管理系统专业版','步骤交付物',NULL,1,467,NULL,'2025-11-08 03:36:47','2025-11-08 03:36:47'),(292,'配置截图','综合档案管理系统专业版','步骤交付物',NULL,1,468,NULL,'2025-11-08 03:36:47','2025-11-08 03:36:47'),(293,'配置截图','综合档案管理系统专业版','步骤交付物',NULL,1,469,NULL,'2025-11-08 03:36:47','2025-11-08 03:36:47'),(294,'配置截图','综合档案管理系统专业版','步骤交付物',NULL,0,470,NULL,'2025-11-08 03:36:47','2025-11-08 03:36:47'),(295,'配置截图','综合档案管理系统专业版','步骤交付物',NULL,1,471,NULL,'2025-11-08 03:36:47','2025-11-08 03:36:47'),(296,'配置截图','综合档案管理系统专业版','步骤交付物',NULL,1,472,NULL,'2025-11-08 03:36:47','2025-11-08 03:36:47'),(297,'配置截图','综合档案管理系统专业版','步骤交付物',NULL,0,473,NULL,'2025-11-08 03:36:47','2025-11-08 03:36:47'),(298,'配置截图','综合档案管理系统专业版','步骤交付物',NULL,0,474,NULL,'2025-11-08 03:36:47','2025-11-08 03:36:47'),(299,'标准产品功能配置报告','综合档案管理系统专业版','里程碑交付物',NULL,1,NULL,13,'2025-11-08 03:36:47','2025-11-08 03:36:47'),(300,'数据迁移技术方案','综合档案管理系统专业版','步骤交付物',NULL,1,476,NULL,'2025-11-08 03:36:47','2025-11-08 03:36:47'),(301,'数据迁移核验表','综合档案管理系统专业版','步骤交付物',NULL,1,478,NULL,'2025-11-08 03:36:47','2025-11-08 03:36:47'),(302,'数据迁移报告','综合档案管理系统专业版','里程碑交付物',NULL,1,NULL,14,'2025-11-08 03:36:47','2025-11-08 03:36:47'),(303,'接口需求文档（用户签字版）','综合档案管理系统专业版','步骤交付物',NULL,1,480,NULL,'2025-11-08 03:36:47','2025-11-08 03:36:47'),(304,'接口文档','综合档案管理系统专业版','步骤交付物',NULL,1,481,NULL,'2025-11-08 03:36:47','2025-11-08 03:36:47'),(305,'接口开发代码或配置文件','综合档案管理系统专业版','步骤交付物',NULL,1,483,NULL,'2025-11-08 03:36:47','2025-11-08 03:36:47'),(306,'接口集成报告','综合档案管理系统专业版','里程碑交付物',NULL,1,NULL,15,'2025-11-08 03:36:47','2025-11-08 03:36:47'),(307,'个性化需求文档（用户签字版）','综合档案管理系统专业版','步骤交付物',NULL,1,484,NULL,'2025-11-08 03:36:47','2025-11-08 03:36:47'),(308,'个性化需求设计方案（用户签字版）','综合档案管理系统专业版','步骤交付物',NULL,1,485,NULL,'2025-11-08 03:36:47','2025-11-08 03:36:47'),(309,'个性化需求开发代码','综合档案管理系统专业版','步骤交付物',NULL,1,488,NULL,'2025-11-08 03:36:47','2025-11-08 03:36:47'),(310,'个性化需求开发报告','综合档案管理系统专业版','里程碑交付物',NULL,1,NULL,16,'2025-11-08 03:36:47','2025-11-08 03:36:47'),(311,'产生变动的里程碑交付物','综合档案管理系统专业版','步骤交付物',NULL,0,489,NULL,'2025-11-08 03:36:47','2025-11-08 03:36:47'),(312,'用户操作手册及培训材料','综合档案管理系统专业版','步骤交付物',NULL,1,490,NULL,'2025-11-08 03:36:47','2025-11-08 03:36:47'),(313,'项目产生的其他材料','综合档案管理系统专业版','里程碑交付物',NULL,0,NULL,17,'2025-11-08 03:36:47','2025-11-08 03:36:47'),(314,'上线试运行申请（用户签字版）','综合档案管理系统专业版','里程碑交付物',NULL,1,NULL,18,'2025-11-08 03:36:47','2025-11-08 03:36:47'),(315,'试运行报告（如需）','综合档案管理系统专业版','里程碑交付物',NULL,0,NULL,19,'2025-11-08 03:36:47','2025-11-08 03:36:47'),(316,'项目验收报告','综合档案管理系统专业版','里程碑交付物',NULL,1,NULL,19,'2025-11-08 03:36:47','2025-11-08 03:36:47'),(317,'项目启动会汇报材料','综合档案管理系统网络版','里程碑交付物',NULL,1,NULL,11,'2025-11-08 03:36:55','2025-11-08 03:36:55'),(318,'系统应用规模及部署需求调研结果','综合档案管理系统网络版','步骤交付物',NULL,1,494,NULL,'2025-11-08 03:36:55','2025-11-08 03:36:55'),(319,'档案业务现状及需求调研报告','综合档案管理系统网络版','步骤交付物',NULL,1,495,NULL,'2025-11-08 03:36:55','2025-11-08 03:36:55'),(320,'历史数据迁移需求调研报告','综合档案管理系统网络版','步骤交付物',NULL,1,496,NULL,'2025-11-08 03:36:55','2025-11-08 03:36:55'),(321,'个性化功能需求调研报告','综合档案管理系统网络版','步骤交付物',NULL,1,497,NULL,'2025-11-08 03:36:55','2025-11-08 03:36:55'),(322,'项目整体需求调研报告','综合档案管理系统网络版','里程碑交付物',NULL,1,NULL,12,'2025-11-08 03:36:55','2025-11-08 03:36:55'),(323,'系统安装部署文档','综合档案管理系统网络版','步骤交付物',NULL,1,498,NULL,'2025-11-08 03:36:55','2025-11-08 03:36:55'),(324,'配置截图','综合档案管理系统网络版','步骤交付物',NULL,0,499,NULL,'2025-11-08 03:36:55','2025-11-08 03:36:55'),(325,'配置截图','综合档案管理系统网络版','步骤交付物',NULL,1,500,NULL,'2025-11-08 03:36:55','2025-11-08 03:36:55'),(326,'配置截图','综合档案管理系统网络版','步骤交付物',NULL,0,501,NULL,'2025-11-08 03:36:55','2025-11-08 03:36:55'),(327,'配置截图','综合档案管理系统网络版','步骤交付物',NULL,1,502,NULL,'2025-11-08 03:36:55','2025-11-08 03:36:55'),(328,'配置截图','综合档案管理系统网络版','步骤交付物',NULL,1,503,NULL,'2025-11-08 03:36:55','2025-11-08 03:36:55'),(329,'配置截图','综合档案管理系统网络版','步骤交付物',NULL,1,504,NULL,'2025-11-08 03:36:55','2025-11-08 03:36:55'),(330,'配置截图','综合档案管理系统网络版','步骤交付物',NULL,0,505,NULL,'2025-11-08 03:36:55','2025-11-08 03:36:55'),(331,'配置截图','综合档案管理系统网络版','步骤交付物',NULL,1,506,NULL,'2025-11-08 03:36:55','2025-11-08 03:36:55'),(332,'配置截图','综合档案管理系统网络版','步骤交付物',NULL,0,507,NULL,'2025-11-08 03:36:55','2025-11-08 03:36:55'),(333,'配置截图','综合档案管理系统网络版','步骤交付物',NULL,1,508,NULL,'2025-11-08 03:36:55','2025-11-08 03:36:55'),(334,'配置截图','综合档案管理系统网络版','步骤交付物',NULL,1,509,NULL,'2025-11-08 03:36:55','2025-11-08 03:36:55'),(335,'配置截图','综合档案管理系统网络版','步骤交付物',NULL,1,510,NULL,'2025-11-08 03:36:55','2025-11-08 03:36:55'),(336,'配置截图','综合档案管理系统网络版','步骤交付物',NULL,1,511,NULL,'2025-11-08 03:36:55','2025-11-08 03:36:55'),(337,'配置截图','综合档案管理系统网络版','步骤交付物',NULL,1,512,NULL,'2025-11-08 03:36:55','2025-11-08 03:36:55'),(338,'配置截图','综合档案管理系统网络版','步骤交付物',NULL,0,513,NULL,'2025-11-08 03:36:55','2025-11-08 03:36:55'),(339,'配置截图','综合档案管理系统网络版','步骤交付物',NULL,1,514,NULL,'2025-11-08 03:36:55','2025-11-08 03:36:55'),(340,'配置截图','综合档案管理系统网络版','步骤交付物',NULL,1,515,NULL,'2025-11-08 03:36:55','2025-11-08 03:36:55'),(341,'配置截图','综合档案管理系统网络版','步骤交付物',NULL,0,516,NULL,'2025-11-08 03:36:55','2025-11-08 03:36:55'),(342,'标准产品功能配置报告','综合档案管理系统网络版','里程碑交付物',NULL,1,NULL,13,'2025-11-08 03:36:55','2025-11-08 03:36:55'),(343,'数据迁移技术方案','综合档案管理系统网络版','步骤交付物',NULL,1,518,NULL,'2025-11-08 03:36:55','2025-11-08 03:36:55'),(344,'数据迁移核验表','综合档案管理系统网络版','步骤交付物',NULL,1,520,NULL,'2025-11-08 03:36:55','2025-11-08 03:36:55'),(345,'数据迁移报告','综合档案管理系统网络版','里程碑交付物',NULL,1,NULL,14,'2025-11-08 03:36:55','2025-11-08 03:36:55'),(346,'个性化需求文档（用户签字版）','综合档案管理系统网络版','步骤交付物',NULL,1,522,NULL,'2025-11-08 03:36:55','2025-11-08 03:36:55'),(347,'个性化需求设计方案（用户签字版）','综合档案管理系统网络版','步骤交付物',NULL,1,523,NULL,'2025-11-08 03:36:55','2025-11-08 03:36:55'),(348,'个性化需求开发代码','综合档案管理系统网络版','步骤交付物',NULL,1,526,NULL,'2025-11-08 03:36:56','2025-11-08 03:36:56'),(349,'个性化需求开发报告','综合档案管理系统网络版','里程碑交付物',NULL,1,NULL,16,'2025-11-08 03:36:56','2025-11-08 03:36:56'),(350,'产生变动的里程碑交付物','综合档案管理系统网络版','步骤交付物',NULL,0,527,NULL,'2025-11-08 03:36:56','2025-11-08 03:36:56'),(351,'用户操作手册及培训材料','综合档案管理系统网络版','步骤交付物',NULL,1,528,NULL,'2025-11-08 03:36:56','2025-11-08 03:36:56'),(352,'项目产生的其他材料','综合档案管理系统网络版','里程碑交付物',NULL,0,NULL,17,'2025-11-08 03:36:56','2025-11-08 03:36:56'),(353,'上线试运行申请（用户签字版）','综合档案管理系统网络版','里程碑交付物',NULL,1,NULL,18,'2025-11-08 03:36:56','2025-11-08 03:36:56'),(354,'试运行报告（如需）','综合档案管理系统网络版','里程碑交付物',NULL,0,NULL,19,'2025-11-08 03:36:56','2025-11-08 03:36:56'),(355,'项目验收报告','综合档案管理系统网络版','里程碑交付物',NULL,1,NULL,19,'2025-11-08 03:36:56','2025-11-08 03:36:56'),(356,'项目启动会汇报材料','馆藏资源系统','里程碑交付物',NULL,1,NULL,11,'2025-11-08 03:37:02','2025-11-08 03:37:02'),(357,'系统部署需求调研结果','馆藏资源系统','步骤交付物',NULL,1,532,NULL,'2025-11-08 03:37:02','2025-11-08 03:37:02'),(358,'档案业务现状及需求调研报告','馆藏资源系统','步骤交付物',NULL,1,533,NULL,'2025-11-08 03:37:02','2025-11-08 03:37:02'),(359,'历史数据迁移需求调研报告','馆藏资源系统','步骤交付物',NULL,1,534,NULL,'2025-11-08 03:37:02','2025-11-08 03:37:02'),(360,'接口需求调研报告','馆藏资源系统','步骤交付物',NULL,1,535,NULL,'2025-11-08 03:37:02','2025-11-08 03:37:02'),(361,'个性化功能需求调研报告','馆藏资源系统','步骤交付物',NULL,1,536,NULL,'2025-11-08 03:37:02','2025-11-08 03:37:02'),(362,'项目整体需求调研报告','馆藏资源系统','里程碑交付物',NULL,1,NULL,12,'2025-11-08 03:37:03','2025-11-08 03:37:03'),(363,'系统安装部署文档','馆藏资源系统','步骤交付物',NULL,1,537,NULL,'2025-11-08 03:37:03','2025-11-08 03:37:03'),(364,'配置截图','馆藏资源系统','步骤交付物',NULL,0,538,NULL,'2025-11-08 03:37:03','2025-11-08 03:37:03'),(365,'配置截图','馆藏资源系统','步骤交付物',NULL,1,539,NULL,'2025-11-08 03:37:03','2025-11-08 03:37:03'),(366,'配置截图','馆藏资源系统','步骤交付物',NULL,0,540,NULL,'2025-11-08 03:37:03','2025-11-08 03:37:03'),(367,'配置截图','馆藏资源系统','步骤交付物',NULL,1,541,NULL,'2025-11-08 03:37:03','2025-11-08 03:37:03'),(368,'配置截图','馆藏资源系统','步骤交付物',NULL,1,542,NULL,'2025-11-08 03:37:03','2025-11-08 03:37:03'),(369,'配置截图','馆藏资源系统','步骤交付物',NULL,1,543,NULL,'2025-11-08 03:37:03','2025-11-08 03:37:03'),(370,'配置截图','馆藏资源系统','步骤交付物',NULL,0,544,NULL,'2025-11-08 03:37:03','2025-11-08 03:37:03'),(371,'配置截图','馆藏资源系统','步骤交付物',NULL,1,545,NULL,'2025-11-08 03:37:03','2025-11-08 03:37:03'),(372,'配置截图','馆藏资源系统','步骤交付物',NULL,0,546,NULL,'2025-11-08 03:37:03','2025-11-08 03:37:03'),(373,'配置截图','馆藏资源系统','步骤交付物',NULL,1,547,NULL,'2025-11-08 03:37:03','2025-11-08 03:37:03'),(374,'配置截图','馆藏资源系统','步骤交付物',NULL,0,548,NULL,'2025-11-08 03:37:03','2025-11-08 03:37:03'),(375,'配置截图','馆藏资源系统','步骤交付物',NULL,1,549,NULL,'2025-11-08 03:37:03','2025-11-08 03:37:03'),(376,'配置截图','馆藏资源系统','步骤交付物',NULL,1,550,NULL,'2025-11-08 03:37:03','2025-11-08 03:37:03'),(377,'配置截图','馆藏资源系统','步骤交付物',NULL,1,551,NULL,'2025-11-08 03:37:03','2025-11-08 03:37:03'),(378,'配置截图','馆藏资源系统','步骤交付物',NULL,1,552,NULL,'2025-11-08 03:37:03','2025-11-08 03:37:03'),(379,'配置截图','馆藏资源系统','步骤交付物',NULL,1,553,NULL,'2025-11-08 03:37:03','2025-11-08 03:37:03'),(380,'配置截图','馆藏资源系统','步骤交付物',NULL,0,554,NULL,'2025-11-08 03:37:03','2025-11-08 03:37:03'),(381,'配置截图','馆藏资源系统','步骤交付物',NULL,0,555,NULL,'2025-11-08 03:37:03','2025-11-08 03:37:03'),(382,'标准产品功能配置报告','馆藏资源系统','里程碑交付物',NULL,1,NULL,13,'2025-11-08 03:37:03','2025-11-08 03:37:03'),(383,'数据迁移技术方案','馆藏资源系统','步骤交付物',NULL,1,557,NULL,'2025-11-08 03:37:03','2025-11-08 03:37:03'),(384,'数据迁移核验表','馆藏资源系统','步骤交付物',NULL,1,559,NULL,'2025-11-08 03:37:03','2025-11-08 03:37:03'),(385,'数据迁移报告','馆藏资源系统','里程碑交付物',NULL,1,NULL,14,'2025-11-08 03:37:03','2025-11-08 03:37:03'),(386,'接口需求文档（用户签字版）','馆藏资源系统','步骤交付物',NULL,1,561,NULL,'2025-11-08 03:37:03','2025-11-08 03:37:03'),(387,'接口文档','馆藏资源系统','步骤交付物',NULL,1,562,NULL,'2025-11-08 03:37:03','2025-11-08 03:37:03'),(388,'接口开发代码或配置文件','馆藏资源系统','步骤交付物',NULL,1,564,NULL,'2025-11-08 03:37:03','2025-11-08 03:37:03'),(389,'接口集成报告','馆藏资源系统','里程碑交付物',NULL,1,NULL,15,'2025-11-08 03:37:03','2025-11-08 03:37:03'),(390,'个性化需求文档（用户签字版）','馆藏资源系统','步骤交付物',NULL,1,565,NULL,'2025-11-08 03:37:03','2025-11-08 03:37:03'),(391,'个性化需求设计方案（用户签字版）','馆藏资源系统','步骤交付物',NULL,1,566,NULL,'2025-11-08 03:37:03','2025-11-08 03:37:03'),(392,'个性化需求开发代码','馆藏资源系统','步骤交付物',NULL,1,569,NULL,'2025-11-08 03:37:03','2025-11-08 03:37:03'),(393,'个性化需求开发报告','馆藏资源系统','里程碑交付物',NULL,1,NULL,16,'2025-11-08 03:37:03','2025-11-08 03:37:03'),(394,'产生变动的里程碑交付物','馆藏资源系统','步骤交付物',NULL,0,570,NULL,'2025-11-08 03:37:03','2025-11-08 03:37:03'),(395,'用户操作手册及培训材料','馆藏资源系统','步骤交付物',NULL,1,571,NULL,'2025-11-08 03:37:03','2025-11-08 03:37:03'),(396,'项目产生的其他材料','馆藏资源系统','里程碑交付物',NULL,0,NULL,17,'2025-11-08 03:37:03','2025-11-08 03:37:03'),(397,'上线试运行申请（用户签字版）','馆藏资源系统','里程碑交付物',NULL,1,NULL,18,'2025-11-08 03:37:03','2025-11-08 03:37:03'),(398,'试运行报告（如需）','馆藏资源系统','里程碑交付物',NULL,0,NULL,19,'2025-11-08 03:37:03','2025-11-08 03:37:03'),(399,'项目验收报告','馆藏资源系统','里程碑交付物',NULL,1,NULL,19,'2025-11-08 03:37:03','2025-11-08 03:37:03');
/*!40000 ALTER TABLE `standard_deliverable` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `standard_milestone`
--

DROP TABLE IF EXISTS `standard_milestone`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `standard_milestone` (
  `milestone_id` bigint NOT NULL AUTO_INCREMENT COMMENT '里程碑ID（主键）',
  `milestone_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '里程碑名称',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`milestone_id`),
  KEY `idx_milestone_name` (`milestone_name`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标准里程碑信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `standard_milestone`
--

LOCK TABLES `standard_milestone` WRITE;
/*!40000 ALTER TABLE `standard_milestone` DISABLE KEYS */;
INSERT INTO `standard_milestone` VALUES (11,'01项目正式启动','2025-10-17 09:54:37','2025-10-17 09:54:37'),(12,'02需求确定','2025-10-17 09:54:37','2025-10-17 09:54:37'),(13,'03完成标准产品功能配置','2025-10-17 09:54:37','2025-10-17 09:54:37'),(14,'04完成数据迁移','2025-10-17 09:54:37','2025-10-17 09:54:37'),(15,'05完成接口开发集成','2025-10-17 09:54:37','2025-10-17 09:54:37'),(16,'06完成个性化功能开发','2025-10-17 09:54:37','2025-10-17 09:54:37'),(17,'07完成系统上线前功能调整及其他准备工作','2025-10-17 09:54:37','2025-10-17 09:54:37'),(18,'08完成系统上线及试运行','2025-10-17 09:54:37','2025-10-17 09:54:37'),(19,'09完成项目验收','2025-10-17 09:54:37','2025-10-17 09:54:37');
/*!40000 ALTER TABLE `standard_milestone` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `userId` bigint NOT NULL AUTO_INCREMENT COMMENT 'User ID',
  `userName` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Username',
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Password',
  `organId` bigint DEFAULT NULL COMMENT 'Organization ID',
  `roleId` bigint DEFAULT NULL COMMENT 'Role ID',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Real Name',
  `locked` tinyint DEFAULT '0' COMMENT 'Lock Status: 0-Normal, 1-Locked',
  `createTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`userId`),
  UNIQUE KEY `userName` (`userName`),
  KEY `idx_user_name` (`userName`),
  KEY `idx_organ_id` (`organId`),
  KEY `idx_role_id` (`roleId`),
  KEY `idx_user_create_time` (`createTime`),
  KEY `idx_user_update_time` (`updateTime`),
  CONSTRAINT `user_ibfk_1` FOREIGN KEY (`organId`) REFERENCES `organ` (`organId`) ON DELETE SET NULL,
  CONSTRAINT `user_ibfk_2` FOREIGN KEY (`roleId`) REFERENCES `role` (`roleId`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User Table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'admin','$2a$10$83xFBa.d70Dr2M0xUnUD0OAZNweDHCs.WoKqgcWytnjrUWQFiiv.i',1,1,'系统管理员',0,'2025-10-08 11:44:34','2025-10-08 11:44:34'),(2,'wangzhile','$2a$10$Wr580cbchq4hznslsXLZrOBK6ARJXZR3BsRw5.v0JUZnnML805a2a',2,4,'王治乐',0,NULL,NULL),(3,'liutingjun','$2a$10$UYqG2OP7UToPC2aX005OpemmYnWysLeLLzAHUOgLVAo9UlPQab.TK',2,4,'刘庭俊',0,NULL,NULL),(4,'wenxiaojun','$2a$10$QhJiMhNIGYb1mnkoMiFsT.1IMh9vvO5SmWUZLbOb3SD6cajlSJqZu',5,5,'温晓军',0,NULL,NULL),(5,'gly','$2a$10$ByCcQhZfipq3JixNNbz6HuDwVa2aaxn6wNpIHsRnLflnjfPCESRDi',1,1,'管理员',0,NULL,NULL),(6,'yanghan','$2a$10$O0mef5IRyzARjRVMIqWDiOmcG5aOJDfx53Te4pYoNd5dy2RoZjgNC',5,2,'杨寒',0,NULL,NULL),(7,'jinkaituo','$2a$10$RX.MNwvDYG.cHAd7.lHFAu0SIQN4kpxSBHucztWXWbjgO0F2x1YTK',2,6,'金开拓',0,NULL,NULL);
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

-- Dump completed on 2025-12-29  9:29:47
