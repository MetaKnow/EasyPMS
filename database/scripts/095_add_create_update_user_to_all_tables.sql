-- Version: 095
-- Description: Add createUser and updateUser columns to all business tables and auto-fill on insert/update

USE pms_db;

ALTER TABLE `afterService_project` ADD COLUMN `createUser` BIGINT NULL COMMENT '创建人（user表外键）', ADD COLUMN `updateUser` BIGINT NULL COMMENT '更新人（user表外键）';
ALTER TABLE `afterservice_project_participants` ADD COLUMN `createUser` BIGINT NULL COMMENT '创建人（user表外键）', ADD COLUMN `updateUser` BIGINT NULL COMMENT '更新人（user表外键）';
ALTER TABLE `constructing_project` ADD COLUMN `createUser` BIGINT NULL COMMENT '创建人（user表外键）', ADD COLUMN `updateUser` BIGINT NULL COMMENT '更新人（user表外键）';
ALTER TABLE `constructing_project_participants` ADD COLUMN `createUser` BIGINT NULL COMMENT '创建人（user表外键）', ADD COLUMN `updateUser` BIGINT NULL COMMENT '更新人（user表外键）';
ALTER TABLE `constructing_project_modifyRecord` ADD COLUMN `createUser` BIGINT NULL COMMENT '创建人（user表外键）', ADD COLUMN `updateUser` BIGINT NULL COMMENT '更新人（user表外键）';
ALTER TABLE `extra_requirement` ADD COLUMN `createUser` BIGINT NULL COMMENT '创建人（user表外键）', ADD COLUMN `updateUser` BIGINT NULL COMMENT '更新人（user表外键）';
ALTER TABLE `constructing_project_risk` ADD COLUMN `createUser` BIGINT NULL COMMENT '创建人（user表外键）', ADD COLUMN `updateUser` BIGINT NULL COMMENT '更新人（user表外键）';
ALTER TABLE `constructing_project_weeklyReportFiles` ADD COLUMN `createUser` BIGINT NULL COMMENT '创建人（user表外键）', ADD COLUMN `updateUser` BIGINT NULL COMMENT '更新人（user表外键）';
ALTER TABLE `constructing_project_weeklyReport` ADD COLUMN `createUser` BIGINT NULL COMMENT '创建人（user表外键）', ADD COLUMN `updateUser` BIGINT NULL COMMENT '更新人（user表外键）';
ALTER TABLE `constructing_project_riskFiles` ADD COLUMN `createUser` BIGINT NULL COMMENT '创建人（user表外键）', ADD COLUMN `updateUser` BIGINT NULL COMMENT '更新人（user表外键）';
ALTER TABLE `constructing_project_comment_replyFiles` ADD COLUMN `createUser` BIGINT NULL COMMENT '创建人（user表外键）', ADD COLUMN `updateUser` BIGINT NULL COMMENT '更新人（user表外键）';
ALTER TABLE `constructing_project_comment_reply` ADD COLUMN `createUser` BIGINT NULL COMMENT '创建人（user表外键）', ADD COLUMN `updateUser` BIGINT NULL COMMENT '更新人（user表外键）';
ALTER TABLE `constructing_project_commentFiles` ADD COLUMN `createUser` BIGINT NULL COMMENT '创建人（user表外键）', ADD COLUMN `updateUser` BIGINT NULL COMMENT '更新人（user表外键）';
ALTER TABLE `constructing_project_comment` ADD COLUMN `createUser` BIGINT NULL COMMENT '创建人（user表外键）', ADD COLUMN `updateUser` BIGINT NULL COMMENT '更新人（user表外键）';
ALTER TABLE `afterService_leads_deliverableFiles` ADD COLUMN `createUser` BIGINT NULL COMMENT '创建人（user表外键）', ADD COLUMN `updateUser` BIGINT NULL COMMENT '更新人（user表外键）';
ALTER TABLE `afterService_leads` ADD COLUMN `createUser` BIGINT NULL COMMENT '创建人（user表外键）', ADD COLUMN `updateUser` BIGINT NULL COMMENT '更新人（user表外键）';
ALTER TABLE `customer_follow_up_deliverableFiles` ADD COLUMN `createUser` BIGINT NULL COMMENT '创建人（user表外键）', ADD COLUMN `updateUser` BIGINT NULL COMMENT '更新人（user表外键）';
ALTER TABLE `customer_follow_up` ADD COLUMN `createUser` BIGINT NULL COMMENT '创建人（user表外键）', ADD COLUMN `updateUser` BIGINT NULL COMMENT '更新人（user表外键）';
ALTER TABLE `extra_requirement_deliverableFiles` ADD COLUMN `createUser` BIGINT NULL COMMENT '创建人（user表外键）', ADD COLUMN `updateUser` BIGINT NULL COMMENT '更新人（user表外键）';
ALTER TABLE `customer` ADD COLUMN `createUser` BIGINT NULL COMMENT '创建人（user表外键）', ADD COLUMN `updateUser` BIGINT NULL COMMENT '更新人（user表外键）';
ALTER TABLE `databaseBackup` ADD COLUMN `createUser` BIGINT NULL COMMENT '创建人（user表外键）', ADD COLUMN `updateUser` BIGINT NULL COMMENT '更新人（user表外键）';
ALTER TABLE `user` ADD COLUMN `createUser` BIGINT NULL COMMENT '创建人（user表外键）', ADD COLUMN `updateUser` BIGINT NULL COMMENT '更新人（user表外键）';
ALTER TABLE `channel_distributor` ADD COLUMN `createUser` BIGINT NULL COMMENT '创建人（user表外键）', ADD COLUMN `updateUser` BIGINT NULL COMMENT '更新人（user表外键）';
ALTER TABLE `afterService_deliverable` ADD COLUMN `createUser` BIGINT NULL COMMENT '创建人（user表外键）', ADD COLUMN `updateUser` BIGINT NULL COMMENT '更新人（user表外键）';
ALTER TABLE `afterService_event` ADD COLUMN `createUser` BIGINT NULL COMMENT '创建人（user表外键）', ADD COLUMN `updateUser` BIGINT NULL COMMENT '更新人（user表外键）';
ALTER TABLE `construct_deliverableFiles` ADD COLUMN `createUser` BIGINT NULL COMMENT '创建人（user表外键）', ADD COLUMN `updateUser` BIGINT NULL COMMENT '更新人（user表外键）';
ALTER TABLE `personal_develope` ADD COLUMN `createUser` BIGINT NULL COMMENT '创建人（user表外键）', ADD COLUMN `updateUser` BIGINT NULL COMMENT '更新人（user表外键）';
ALTER TABLE `project_sstep_relations` ADD COLUMN `createUser` BIGINT NULL COMMENT '创建人（user表外键）', ADD COLUMN `updateUser` BIGINT NULL COMMENT '更新人（user表外键）';
ALTER TABLE `interface` ADD COLUMN `createUser` BIGINT NULL COMMENT '创建人（user表外键）', ADD COLUMN `updateUser` BIGINT NULL COMMENT '更新人（user表外键）';
ALTER TABLE `construct_milestone` ADD COLUMN `createUser` BIGINT NULL COMMENT '创建人（user表外键）', ADD COLUMN `updateUser` BIGINT NULL COMMENT '更新人（user表外键）';
ALTER TABLE `standard_deliverable` ADD COLUMN `createUser` BIGINT NULL COMMENT '创建人（user表外键）', ADD COLUMN `updateUser` BIGINT NULL COMMENT '更新人（user表外键）';
ALTER TABLE `archieveSoft` ADD COLUMN `createUser` BIGINT NULL COMMENT '创建人（user表外键）', ADD COLUMN `updateUser` BIGINT NULL COMMENT '更新人（user表外键）';
ALTER TABLE `standard_construct_step` ADD COLUMN `createUser` BIGINT NULL COMMENT '创建人（user表外键）', ADD COLUMN `updateUser` BIGINT NULL COMMENT '更新人（user表外键）';
ALTER TABLE `standard_milestone` ADD COLUMN `createUser` BIGINT NULL COMMENT '创建人（user表外键）', ADD COLUMN `updateUser` BIGINT NULL COMMENT '更新人（user表外键）';
ALTER TABLE `role` ADD COLUMN `createUser` BIGINT NULL COMMENT '创建人（user表外键）', ADD COLUMN `updateUser` BIGINT NULL COMMENT '更新人（user表外键）';
ALTER TABLE `organ` ADD COLUMN `createUser` BIGINT NULL COMMENT '创建人（user表外键）', ADD COLUMN `updateUser` BIGINT NULL COMMENT '更新人（user表外键）';

ALTER TABLE `afterService_project` ADD CONSTRAINT `fk_afterService_project_createUser` FOREIGN KEY (`createUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `afterService_project` ADD CONSTRAINT `fk_afterService_project_updateUser` FOREIGN KEY (`updateUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `afterservice_project_participants` ADD CONSTRAINT `fk_afterservice_project_participants_createUser` FOREIGN KEY (`createUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `afterservice_project_participants` ADD CONSTRAINT `fk_afterservice_project_participants_updateUser` FOREIGN KEY (`updateUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `constructing_project` ADD CONSTRAINT `fk_constructing_project_createUser` FOREIGN KEY (`createUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `constructing_project` ADD CONSTRAINT `fk_constructing_project_updateUser` FOREIGN KEY (`updateUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `constructing_project_participants` ADD CONSTRAINT `fk_constructing_project_participants_createUser` FOREIGN KEY (`createUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `constructing_project_participants` ADD CONSTRAINT `fk_constructing_project_participants_updateUser` FOREIGN KEY (`updateUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `constructing_project_modifyRecord` ADD CONSTRAINT `fk_constructing_project_modifyRecord_createUser` FOREIGN KEY (`createUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `constructing_project_modifyRecord` ADD CONSTRAINT `fk_constructing_project_modifyRecord_updateUser` FOREIGN KEY (`updateUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `extra_requirement` ADD CONSTRAINT `fk_extra_requirement_createUser` FOREIGN KEY (`createUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `extra_requirement` ADD CONSTRAINT `fk_extra_requirement_updateUser` FOREIGN KEY (`updateUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `constructing_project_risk` ADD CONSTRAINT `fk_constructing_project_risk_createUser` FOREIGN KEY (`createUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `constructing_project_risk` ADD CONSTRAINT `fk_constructing_project_risk_updateUser` FOREIGN KEY (`updateUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `constructing_project_weeklyReportFiles` ADD CONSTRAINT `fk_constructing_project_weeklyReportFiles_createUser` FOREIGN KEY (`createUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `constructing_project_weeklyReportFiles` ADD CONSTRAINT `fk_constructing_project_weeklyReportFiles_updateUser` FOREIGN KEY (`updateUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `constructing_project_weeklyReport` ADD CONSTRAINT `fk_constructing_project_weeklyReport_createUser` FOREIGN KEY (`createUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `constructing_project_weeklyReport` ADD CONSTRAINT `fk_constructing_project_weeklyReport_updateUser` FOREIGN KEY (`updateUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `constructing_project_riskFiles` ADD CONSTRAINT `fk_constructing_project_riskFiles_createUser` FOREIGN KEY (`createUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `constructing_project_riskFiles` ADD CONSTRAINT `fk_constructing_project_riskFiles_updateUser` FOREIGN KEY (`updateUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `constructing_project_comment_replyFiles` ADD CONSTRAINT `fk_constructing_project_comment_replyFiles_createUser` FOREIGN KEY (`createUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `constructing_project_comment_replyFiles` ADD CONSTRAINT `fk_constructing_project_comment_replyFiles_updateUser` FOREIGN KEY (`updateUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `constructing_project_comment_reply` ADD CONSTRAINT `fk_constructing_project_comment_reply_createUser` FOREIGN KEY (`createUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `constructing_project_comment_reply` ADD CONSTRAINT `fk_constructing_project_comment_reply_updateUser` FOREIGN KEY (`updateUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `constructing_project_commentFiles` ADD CONSTRAINT `fk_constructing_project_commentFiles_createUser` FOREIGN KEY (`createUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `constructing_project_commentFiles` ADD CONSTRAINT `fk_constructing_project_commentFiles_updateUser` FOREIGN KEY (`updateUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `constructing_project_comment` ADD CONSTRAINT `fk_constructing_project_comment_createUser` FOREIGN KEY (`createUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `constructing_project_comment` ADD CONSTRAINT `fk_constructing_project_comment_updateUser` FOREIGN KEY (`updateUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `afterService_leads_deliverableFiles` ADD CONSTRAINT `fk_afterService_leads_deliverableFiles_createUser` FOREIGN KEY (`createUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `afterService_leads_deliverableFiles` ADD CONSTRAINT `fk_afterService_leads_deliverableFiles_updateUser` FOREIGN KEY (`updateUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `afterService_leads` ADD CONSTRAINT `fk_afterService_leads_createUser` FOREIGN KEY (`createUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `afterService_leads` ADD CONSTRAINT `fk_afterService_leads_updateUser` FOREIGN KEY (`updateUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `customer_follow_up_deliverableFiles` ADD CONSTRAINT `fk_customer_follow_up_deliverableFiles_createUser` FOREIGN KEY (`createUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `customer_follow_up_deliverableFiles` ADD CONSTRAINT `fk_customer_follow_up_deliverableFiles_updateUser` FOREIGN KEY (`updateUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `customer_follow_up` ADD CONSTRAINT `fk_customer_follow_up_createUser` FOREIGN KEY (`createUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `customer_follow_up` ADD CONSTRAINT `fk_customer_follow_up_updateUser` FOREIGN KEY (`updateUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `extra_requirement_deliverableFiles` ADD CONSTRAINT `fk_extra_requirement_deliverableFiles_createUser` FOREIGN KEY (`createUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `extra_requirement_deliverableFiles` ADD CONSTRAINT `fk_extra_requirement_deliverableFiles_updateUser` FOREIGN KEY (`updateUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `customer` ADD CONSTRAINT `fk_customer_createUser` FOREIGN KEY (`createUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `customer` ADD CONSTRAINT `fk_customer_updateUser` FOREIGN KEY (`updateUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `databaseBackup` ADD CONSTRAINT `fk_databaseBackup_createUser` FOREIGN KEY (`createUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `databaseBackup` ADD CONSTRAINT `fk_databaseBackup_updateUser` FOREIGN KEY (`updateUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `user` ADD CONSTRAINT `fk_user_createUser` FOREIGN KEY (`createUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `user` ADD CONSTRAINT `fk_user_updateUser` FOREIGN KEY (`updateUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `channel_distributor` ADD CONSTRAINT `fk_channel_distributor_createUser` FOREIGN KEY (`createUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `channel_distributor` ADD CONSTRAINT `fk_channel_distributor_updateUser` FOREIGN KEY (`updateUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `afterService_deliverable` ADD CONSTRAINT `fk_afterService_deliverable_createUser` FOREIGN KEY (`createUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `afterService_deliverable` ADD CONSTRAINT `fk_afterService_deliverable_updateUser` FOREIGN KEY (`updateUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `afterService_event` ADD CONSTRAINT `fk_afterService_event_createUser` FOREIGN KEY (`createUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `afterService_event` ADD CONSTRAINT `fk_afterService_event_updateUser` FOREIGN KEY (`updateUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `construct_deliverableFiles` ADD CONSTRAINT `fk_construct_deliverableFiles_createUser` FOREIGN KEY (`createUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `construct_deliverableFiles` ADD CONSTRAINT `fk_construct_deliverableFiles_updateUser` FOREIGN KEY (`updateUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `personal_develope` ADD CONSTRAINT `fk_personal_develope_createUser` FOREIGN KEY (`createUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `personal_develope` ADD CONSTRAINT `fk_personal_develope_updateUser` FOREIGN KEY (`updateUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `project_sstep_relations` ADD CONSTRAINT `fk_project_sstep_relations_createUser` FOREIGN KEY (`createUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `project_sstep_relations` ADD CONSTRAINT `fk_project_sstep_relations_updateUser` FOREIGN KEY (`updateUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `interface` ADD CONSTRAINT `fk_interface_createUser` FOREIGN KEY (`createUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `interface` ADD CONSTRAINT `fk_interface_updateUser` FOREIGN KEY (`updateUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `construct_milestone` ADD CONSTRAINT `fk_construct_milestone_createUser` FOREIGN KEY (`createUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `construct_milestone` ADD CONSTRAINT `fk_construct_milestone_updateUser` FOREIGN KEY (`updateUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `standard_deliverable` ADD CONSTRAINT `fk_standard_deliverable_createUser` FOREIGN KEY (`createUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `standard_deliverable` ADD CONSTRAINT `fk_standard_deliverable_updateUser` FOREIGN KEY (`updateUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `archieveSoft` ADD CONSTRAINT `fk_archieveSoft_createUser` FOREIGN KEY (`createUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `archieveSoft` ADD CONSTRAINT `fk_archieveSoft_updateUser` FOREIGN KEY (`updateUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `standard_construct_step` ADD CONSTRAINT `fk_standard_construct_step_createUser` FOREIGN KEY (`createUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `standard_construct_step` ADD CONSTRAINT `fk_standard_construct_step_updateUser` FOREIGN KEY (`updateUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `standard_milestone` ADD CONSTRAINT `fk_standard_milestone_createUser` FOREIGN KEY (`createUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `standard_milestone` ADD CONSTRAINT `fk_standard_milestone_updateUser` FOREIGN KEY (`updateUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `role` ADD CONSTRAINT `fk_role_createUser` FOREIGN KEY (`createUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `role` ADD CONSTRAINT `fk_role_updateUser` FOREIGN KEY (`updateUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `organ` ADD CONSTRAINT `fk_organ_createUser` FOREIGN KEY (`createUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `organ` ADD CONSTRAINT `fk_organ_updateUser` FOREIGN KEY (`updateUser`) REFERENCES `user`(`userId`) ON DELETE SET NULL ON UPDATE CASCADE;

DROP TRIGGER IF EXISTS `trg_afterService_project_bi`;
CREATE TRIGGER `trg_afterService_project_bi` BEFORE INSERT ON `afterService_project` FOR EACH ROW SET NEW.createUser = COALESCE(NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(NEW.updateUser, @current_user_id, NEW.createUser, 1);
DROP TRIGGER IF EXISTS `trg_afterService_project_bu`;
CREATE TRIGGER `trg_afterService_project_bu` BEFORE UPDATE ON `afterService_project` FOR EACH ROW SET NEW.createUser = COALESCE(OLD.createUser, NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(@current_user_id, NEW.updateUser, OLD.updateUser, NEW.createUser, 1);

DROP TRIGGER IF EXISTS `trg_constructing_project_bi`;
CREATE TRIGGER `trg_constructing_project_bi` BEFORE INSERT ON `constructing_project` FOR EACH ROW SET NEW.createUser = COALESCE(NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(NEW.updateUser, @current_user_id, NEW.createUser, 1);
DROP TRIGGER IF EXISTS `trg_constructing_project_bu`;
CREATE TRIGGER `trg_constructing_project_bu` BEFORE UPDATE ON `constructing_project` FOR EACH ROW SET NEW.createUser = COALESCE(OLD.createUser, NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(@current_user_id, NEW.updateUser, OLD.updateUser, NEW.createUser, 1);

DROP TRIGGER IF EXISTS `trg_user_bi`;
CREATE TRIGGER `trg_user_bi` BEFORE INSERT ON `user` FOR EACH ROW SET NEW.createUser = COALESCE(NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(NEW.updateUser, @current_user_id, NEW.createUser, 1);
DROP TRIGGER IF EXISTS `trg_user_bu`;
CREATE TRIGGER `trg_user_bu` BEFORE UPDATE ON `user` FOR EACH ROW SET NEW.createUser = COALESCE(OLD.createUser, NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(@current_user_id, NEW.updateUser, OLD.updateUser, NEW.createUser, 1);

DROP TRIGGER IF EXISTS `trg_afterservice_project_participants_bi`;
CREATE TRIGGER `trg_afterservice_project_participants_bi` BEFORE INSERT ON `afterservice_project_participants` FOR EACH ROW SET NEW.createUser = COALESCE(NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(NEW.updateUser, @current_user_id, NEW.createUser, 1);
DROP TRIGGER IF EXISTS `trg_afterservice_project_participants_bu`;
CREATE TRIGGER `trg_afterservice_project_participants_bu` BEFORE UPDATE ON `afterservice_project_participants` FOR EACH ROW SET NEW.createUser = COALESCE(OLD.createUser, NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(@current_user_id, NEW.updateUser, OLD.updateUser, NEW.createUser, 1);

DROP TRIGGER IF EXISTS `trg_constructing_project_participants_bi`;
CREATE TRIGGER `trg_constructing_project_participants_bi` BEFORE INSERT ON `constructing_project_participants` FOR EACH ROW SET NEW.createUser = COALESCE(NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(NEW.updateUser, @current_user_id, NEW.createUser, 1);
DROP TRIGGER IF EXISTS `trg_constructing_project_participants_bu`;
CREATE TRIGGER `trg_constructing_project_participants_bu` BEFORE UPDATE ON `constructing_project_participants` FOR EACH ROW SET NEW.createUser = COALESCE(OLD.createUser, NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(@current_user_id, NEW.updateUser, OLD.updateUser, NEW.createUser, 1);

DROP TRIGGER IF EXISTS `trg_constructing_project_modifyRecord_bi`;
CREATE TRIGGER `trg_constructing_project_modifyRecord_bi` BEFORE INSERT ON `constructing_project_modifyRecord` FOR EACH ROW SET NEW.createUser = COALESCE(NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(NEW.updateUser, @current_user_id, NEW.createUser, 1);
DROP TRIGGER IF EXISTS `trg_constructing_project_modifyRecord_bu`;
CREATE TRIGGER `trg_constructing_project_modifyRecord_bu` BEFORE UPDATE ON `constructing_project_modifyRecord` FOR EACH ROW SET NEW.createUser = COALESCE(OLD.createUser, NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(@current_user_id, NEW.updateUser, OLD.updateUser, NEW.createUser, 1);

DROP TRIGGER IF EXISTS `trg_extra_requirement_bi`;
CREATE TRIGGER `trg_extra_requirement_bi` BEFORE INSERT ON `extra_requirement` FOR EACH ROW SET NEW.createUser = COALESCE(NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(NEW.updateUser, @current_user_id, NEW.createUser, 1);
DROP TRIGGER IF EXISTS `trg_extra_requirement_bu`;
CREATE TRIGGER `trg_extra_requirement_bu` BEFORE UPDATE ON `extra_requirement` FOR EACH ROW SET NEW.createUser = COALESCE(OLD.createUser, NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(@current_user_id, NEW.updateUser, OLD.updateUser, NEW.createUser, 1);

DROP TRIGGER IF EXISTS `trg_constructing_project_risk_bi`;
CREATE TRIGGER `trg_constructing_project_risk_bi` BEFORE INSERT ON `constructing_project_risk` FOR EACH ROW SET NEW.createUser = COALESCE(NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(NEW.updateUser, @current_user_id, NEW.createUser, 1);
DROP TRIGGER IF EXISTS `trg_constructing_project_risk_bu`;
CREATE TRIGGER `trg_constructing_project_risk_bu` BEFORE UPDATE ON `constructing_project_risk` FOR EACH ROW SET NEW.createUser = COALESCE(OLD.createUser, NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(@current_user_id, NEW.updateUser, OLD.updateUser, NEW.createUser, 1);

DROP TRIGGER IF EXISTS `trg_constructing_project_weeklyReportFiles_bi`;
CREATE TRIGGER `trg_constructing_project_weeklyReportFiles_bi` BEFORE INSERT ON `constructing_project_weeklyReportFiles` FOR EACH ROW SET NEW.createUser = COALESCE(NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(NEW.updateUser, @current_user_id, NEW.createUser, 1);
DROP TRIGGER IF EXISTS `trg_constructing_project_weeklyReportFiles_bu`;
CREATE TRIGGER `trg_constructing_project_weeklyReportFiles_bu` BEFORE UPDATE ON `constructing_project_weeklyReportFiles` FOR EACH ROW SET NEW.createUser = COALESCE(OLD.createUser, NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(@current_user_id, NEW.updateUser, OLD.updateUser, NEW.createUser, 1);

DROP TRIGGER IF EXISTS `trg_constructing_project_weeklyReport_bi`;
CREATE TRIGGER `trg_constructing_project_weeklyReport_bi` BEFORE INSERT ON `constructing_project_weeklyReport` FOR EACH ROW SET NEW.createUser = COALESCE(NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(NEW.updateUser, @current_user_id, NEW.createUser, 1);
DROP TRIGGER IF EXISTS `trg_constructing_project_weeklyReport_bu`;
CREATE TRIGGER `trg_constructing_project_weeklyReport_bu` BEFORE UPDATE ON `constructing_project_weeklyReport` FOR EACH ROW SET NEW.createUser = COALESCE(OLD.createUser, NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(@current_user_id, NEW.updateUser, OLD.updateUser, NEW.createUser, 1);

DROP TRIGGER IF EXISTS `trg_constructing_project_riskFiles_bi`;
CREATE TRIGGER `trg_constructing_project_riskFiles_bi` BEFORE INSERT ON `constructing_project_riskFiles` FOR EACH ROW SET NEW.createUser = COALESCE(NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(NEW.updateUser, @current_user_id, NEW.createUser, 1);
DROP TRIGGER IF EXISTS `trg_constructing_project_riskFiles_bu`;
CREATE TRIGGER `trg_constructing_project_riskFiles_bu` BEFORE UPDATE ON `constructing_project_riskFiles` FOR EACH ROW SET NEW.createUser = COALESCE(OLD.createUser, NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(@current_user_id, NEW.updateUser, OLD.updateUser, NEW.createUser, 1);

DROP TRIGGER IF EXISTS `trg_constructing_project_comment_replyFiles_bi`;
CREATE TRIGGER `trg_constructing_project_comment_replyFiles_bi` BEFORE INSERT ON `constructing_project_comment_replyFiles` FOR EACH ROW SET NEW.createUser = COALESCE(NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(NEW.updateUser, @current_user_id, NEW.createUser, 1);
DROP TRIGGER IF EXISTS `trg_constructing_project_comment_replyFiles_bu`;
CREATE TRIGGER `trg_constructing_project_comment_replyFiles_bu` BEFORE UPDATE ON `constructing_project_comment_replyFiles` FOR EACH ROW SET NEW.createUser = COALESCE(OLD.createUser, NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(@current_user_id, NEW.updateUser, OLD.updateUser, NEW.createUser, 1);

DROP TRIGGER IF EXISTS `trg_constructing_project_comment_reply_bi`;
CREATE TRIGGER `trg_constructing_project_comment_reply_bi` BEFORE INSERT ON `constructing_project_comment_reply` FOR EACH ROW SET NEW.createUser = COALESCE(NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(NEW.updateUser, @current_user_id, NEW.createUser, 1);
DROP TRIGGER IF EXISTS `trg_constructing_project_comment_reply_bu`;
CREATE TRIGGER `trg_constructing_project_comment_reply_bu` BEFORE UPDATE ON `constructing_project_comment_reply` FOR EACH ROW SET NEW.createUser = COALESCE(OLD.createUser, NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(@current_user_id, NEW.updateUser, OLD.updateUser, NEW.createUser, 1);

DROP TRIGGER IF EXISTS `trg_constructing_project_commentFiles_bi`;
CREATE TRIGGER `trg_constructing_project_commentFiles_bi` BEFORE INSERT ON `constructing_project_commentFiles` FOR EACH ROW SET NEW.createUser = COALESCE(NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(NEW.updateUser, @current_user_id, NEW.createUser, 1);
DROP TRIGGER IF EXISTS `trg_constructing_project_commentFiles_bu`;
CREATE TRIGGER `trg_constructing_project_commentFiles_bu` BEFORE UPDATE ON `constructing_project_commentFiles` FOR EACH ROW SET NEW.createUser = COALESCE(OLD.createUser, NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(@current_user_id, NEW.updateUser, OLD.updateUser, NEW.createUser, 1);

DROP TRIGGER IF EXISTS `trg_constructing_project_comment_bi`;
CREATE TRIGGER `trg_constructing_project_comment_bi` BEFORE INSERT ON `constructing_project_comment` FOR EACH ROW SET NEW.createUser = COALESCE(NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(NEW.updateUser, @current_user_id, NEW.createUser, 1);
DROP TRIGGER IF EXISTS `trg_constructing_project_comment_bu`;
CREATE TRIGGER `trg_constructing_project_comment_bu` BEFORE UPDATE ON `constructing_project_comment` FOR EACH ROW SET NEW.createUser = COALESCE(OLD.createUser, NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(@current_user_id, NEW.updateUser, OLD.updateUser, NEW.createUser, 1);

DROP TRIGGER IF EXISTS `trg_afterService_leads_deliverableFiles_bi`;
CREATE TRIGGER `trg_afterService_leads_deliverableFiles_bi` BEFORE INSERT ON `afterService_leads_deliverableFiles` FOR EACH ROW SET NEW.createUser = COALESCE(NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(NEW.updateUser, @current_user_id, NEW.createUser, 1);
DROP TRIGGER IF EXISTS `trg_afterService_leads_deliverableFiles_bu`;
CREATE TRIGGER `trg_afterService_leads_deliverableFiles_bu` BEFORE UPDATE ON `afterService_leads_deliverableFiles` FOR EACH ROW SET NEW.createUser = COALESCE(OLD.createUser, NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(@current_user_id, NEW.updateUser, OLD.updateUser, NEW.createUser, 1);

DROP TRIGGER IF EXISTS `trg_afterService_leads_bi`;
CREATE TRIGGER `trg_afterService_leads_bi` BEFORE INSERT ON `afterService_leads` FOR EACH ROW SET NEW.createUser = COALESCE(NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(NEW.updateUser, @current_user_id, NEW.createUser, 1);
DROP TRIGGER IF EXISTS `trg_afterService_leads_bu`;
CREATE TRIGGER `trg_afterService_leads_bu` BEFORE UPDATE ON `afterService_leads` FOR EACH ROW SET NEW.createUser = COALESCE(OLD.createUser, NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(@current_user_id, NEW.updateUser, OLD.updateUser, NEW.createUser, 1);

DROP TRIGGER IF EXISTS `trg_customer_follow_up_deliverableFiles_bi`;
CREATE TRIGGER `trg_customer_follow_up_deliverableFiles_bi` BEFORE INSERT ON `customer_follow_up_deliverableFiles` FOR EACH ROW SET NEW.createUser = COALESCE(NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(NEW.updateUser, @current_user_id, NEW.createUser, 1);
DROP TRIGGER IF EXISTS `trg_customer_follow_up_deliverableFiles_bu`;
CREATE TRIGGER `trg_customer_follow_up_deliverableFiles_bu` BEFORE UPDATE ON `customer_follow_up_deliverableFiles` FOR EACH ROW SET NEW.createUser = COALESCE(OLD.createUser, NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(@current_user_id, NEW.updateUser, OLD.updateUser, NEW.createUser, 1);

DROP TRIGGER IF EXISTS `trg_customer_follow_up_bi`;
CREATE TRIGGER `trg_customer_follow_up_bi` BEFORE INSERT ON `customer_follow_up` FOR EACH ROW SET NEW.createUser = COALESCE(NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(NEW.updateUser, @current_user_id, NEW.createUser, 1);
DROP TRIGGER IF EXISTS `trg_customer_follow_up_bu`;
CREATE TRIGGER `trg_customer_follow_up_bu` BEFORE UPDATE ON `customer_follow_up` FOR EACH ROW SET NEW.createUser = COALESCE(OLD.createUser, NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(@current_user_id, NEW.updateUser, OLD.updateUser, NEW.createUser, 1);

DROP TRIGGER IF EXISTS `trg_extra_requirement_deliverableFiles_bi`;
CREATE TRIGGER `trg_extra_requirement_deliverableFiles_bi` BEFORE INSERT ON `extra_requirement_deliverableFiles` FOR EACH ROW SET NEW.createUser = COALESCE(NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(NEW.updateUser, @current_user_id, NEW.createUser, 1);
DROP TRIGGER IF EXISTS `trg_extra_requirement_deliverableFiles_bu`;
CREATE TRIGGER `trg_extra_requirement_deliverableFiles_bu` BEFORE UPDATE ON `extra_requirement_deliverableFiles` FOR EACH ROW SET NEW.createUser = COALESCE(OLD.createUser, NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(@current_user_id, NEW.updateUser, OLD.updateUser, NEW.createUser, 1);

DROP TRIGGER IF EXISTS `trg_customer_bi`;
CREATE TRIGGER `trg_customer_bi` BEFORE INSERT ON `customer` FOR EACH ROW SET NEW.createUser = COALESCE(NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(NEW.updateUser, @current_user_id, NEW.createUser, 1);
DROP TRIGGER IF EXISTS `trg_customer_bu`;
CREATE TRIGGER `trg_customer_bu` BEFORE UPDATE ON `customer` FOR EACH ROW SET NEW.createUser = COALESCE(OLD.createUser, NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(@current_user_id, NEW.updateUser, OLD.updateUser, NEW.createUser, 1);

DROP TRIGGER IF EXISTS `trg_databaseBackup_bi`;
CREATE TRIGGER `trg_databaseBackup_bi` BEFORE INSERT ON `databaseBackup` FOR EACH ROW SET NEW.createUser = COALESCE(NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(NEW.updateUser, @current_user_id, NEW.createUser, 1);
DROP TRIGGER IF EXISTS `trg_databaseBackup_bu`;
CREATE TRIGGER `trg_databaseBackup_bu` BEFORE UPDATE ON `databaseBackup` FOR EACH ROW SET NEW.createUser = COALESCE(OLD.createUser, NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(@current_user_id, NEW.updateUser, OLD.updateUser, NEW.createUser, 1);

DROP TRIGGER IF EXISTS `trg_channel_distributor_bi`;
CREATE TRIGGER `trg_channel_distributor_bi` BEFORE INSERT ON `channel_distributor` FOR EACH ROW SET NEW.createUser = COALESCE(NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(NEW.updateUser, @current_user_id, NEW.createUser, 1);
DROP TRIGGER IF EXISTS `trg_channel_distributor_bu`;
CREATE TRIGGER `trg_channel_distributor_bu` BEFORE UPDATE ON `channel_distributor` FOR EACH ROW SET NEW.createUser = COALESCE(OLD.createUser, NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(@current_user_id, NEW.updateUser, OLD.updateUser, NEW.createUser, 1);

DROP TRIGGER IF EXISTS `trg_afterService_deliverable_bi`;
CREATE TRIGGER `trg_afterService_deliverable_bi` BEFORE INSERT ON `afterService_deliverable` FOR EACH ROW SET NEW.createUser = COALESCE(NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(NEW.updateUser, @current_user_id, NEW.createUser, 1);
DROP TRIGGER IF EXISTS `trg_afterService_deliverable_bu`;
CREATE TRIGGER `trg_afterService_deliverable_bu` BEFORE UPDATE ON `afterService_deliverable` FOR EACH ROW SET NEW.createUser = COALESCE(OLD.createUser, NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(@current_user_id, NEW.updateUser, OLD.updateUser, NEW.createUser, 1);

DROP TRIGGER IF EXISTS `trg_afterService_event_bi`;
CREATE TRIGGER `trg_afterService_event_bi` BEFORE INSERT ON `afterService_event` FOR EACH ROW SET NEW.createUser = COALESCE(NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(NEW.updateUser, @current_user_id, NEW.createUser, 1);
DROP TRIGGER IF EXISTS `trg_afterService_event_bu`;
CREATE TRIGGER `trg_afterService_event_bu` BEFORE UPDATE ON `afterService_event` FOR EACH ROW SET NEW.createUser = COALESCE(OLD.createUser, NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(@current_user_id, NEW.updateUser, OLD.updateUser, NEW.createUser, 1);

DROP TRIGGER IF EXISTS `trg_construct_deliverableFiles_bi`;
CREATE TRIGGER `trg_construct_deliverableFiles_bi` BEFORE INSERT ON `construct_deliverableFiles` FOR EACH ROW SET NEW.createUser = COALESCE(NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(NEW.updateUser, @current_user_id, NEW.createUser, 1);
DROP TRIGGER IF EXISTS `trg_construct_deliverableFiles_bu`;
CREATE TRIGGER `trg_construct_deliverableFiles_bu` BEFORE UPDATE ON `construct_deliverableFiles` FOR EACH ROW SET NEW.createUser = COALESCE(OLD.createUser, NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(@current_user_id, NEW.updateUser, OLD.updateUser, NEW.createUser, 1);

DROP TRIGGER IF EXISTS `trg_personal_develope_bi`;
CREATE TRIGGER `trg_personal_develope_bi` BEFORE INSERT ON `personal_develope` FOR EACH ROW SET NEW.createUser = COALESCE(NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(NEW.updateUser, @current_user_id, NEW.createUser, 1);
DROP TRIGGER IF EXISTS `trg_personal_develope_bu`;
CREATE TRIGGER `trg_personal_develope_bu` BEFORE UPDATE ON `personal_develope` FOR EACH ROW SET NEW.createUser = COALESCE(OLD.createUser, NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(@current_user_id, NEW.updateUser, OLD.updateUser, NEW.createUser, 1);

DROP TRIGGER IF EXISTS `trg_project_sstep_relations_bi`;
CREATE TRIGGER `trg_project_sstep_relations_bi` BEFORE INSERT ON `project_sstep_relations` FOR EACH ROW SET NEW.createUser = COALESCE(NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(NEW.updateUser, @current_user_id, NEW.createUser, 1);
DROP TRIGGER IF EXISTS `trg_project_sstep_relations_bu`;
CREATE TRIGGER `trg_project_sstep_relations_bu` BEFORE UPDATE ON `project_sstep_relations` FOR EACH ROW SET NEW.createUser = COALESCE(OLD.createUser, NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(@current_user_id, NEW.updateUser, OLD.updateUser, NEW.createUser, 1);

DROP TRIGGER IF EXISTS `trg_interface_bi`;
CREATE TRIGGER `trg_interface_bi` BEFORE INSERT ON `interface` FOR EACH ROW SET NEW.createUser = COALESCE(NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(NEW.updateUser, @current_user_id, NEW.createUser, 1);
DROP TRIGGER IF EXISTS `trg_interface_bu`;
CREATE TRIGGER `trg_interface_bu` BEFORE UPDATE ON `interface` FOR EACH ROW SET NEW.createUser = COALESCE(OLD.createUser, NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(@current_user_id, NEW.updateUser, OLD.updateUser, NEW.createUser, 1);

DROP TRIGGER IF EXISTS `trg_construct_milestone_bi`;
CREATE TRIGGER `trg_construct_milestone_bi` BEFORE INSERT ON `construct_milestone` FOR EACH ROW SET NEW.createUser = COALESCE(NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(NEW.updateUser, @current_user_id, NEW.createUser, 1);
DROP TRIGGER IF EXISTS `trg_construct_milestone_bu`;
CREATE TRIGGER `trg_construct_milestone_bu` BEFORE UPDATE ON `construct_milestone` FOR EACH ROW SET NEW.createUser = COALESCE(OLD.createUser, NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(@current_user_id, NEW.updateUser, OLD.updateUser, NEW.createUser, 1);

DROP TRIGGER IF EXISTS `trg_standard_deliverable_bi`;
CREATE TRIGGER `trg_standard_deliverable_bi` BEFORE INSERT ON `standard_deliverable` FOR EACH ROW SET NEW.createUser = COALESCE(NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(NEW.updateUser, @current_user_id, NEW.createUser, 1);
DROP TRIGGER IF EXISTS `trg_standard_deliverable_bu`;
CREATE TRIGGER `trg_standard_deliverable_bu` BEFORE UPDATE ON `standard_deliverable` FOR EACH ROW SET NEW.createUser = COALESCE(OLD.createUser, NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(@current_user_id, NEW.updateUser, OLD.updateUser, NEW.createUser, 1);

DROP TRIGGER IF EXISTS `trg_archieveSoft_bi`;
CREATE TRIGGER `trg_archieveSoft_bi` BEFORE INSERT ON `archieveSoft` FOR EACH ROW SET NEW.createUser = COALESCE(NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(NEW.updateUser, @current_user_id, NEW.createUser, 1);
DROP TRIGGER IF EXISTS `trg_archieveSoft_bu`;
CREATE TRIGGER `trg_archieveSoft_bu` BEFORE UPDATE ON `archieveSoft` FOR EACH ROW SET NEW.createUser = COALESCE(OLD.createUser, NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(@current_user_id, NEW.updateUser, OLD.updateUser, NEW.createUser, 1);

DROP TRIGGER IF EXISTS `trg_standard_construct_step_bi`;
CREATE TRIGGER `trg_standard_construct_step_bi` BEFORE INSERT ON `standard_construct_step` FOR EACH ROW SET NEW.createUser = COALESCE(NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(NEW.updateUser, @current_user_id, NEW.createUser, 1);
DROP TRIGGER IF EXISTS `trg_standard_construct_step_bu`;
CREATE TRIGGER `trg_standard_construct_step_bu` BEFORE UPDATE ON `standard_construct_step` FOR EACH ROW SET NEW.createUser = COALESCE(OLD.createUser, NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(@current_user_id, NEW.updateUser, OLD.updateUser, NEW.createUser, 1);

DROP TRIGGER IF EXISTS `trg_standard_milestone_bi`;
CREATE TRIGGER `trg_standard_milestone_bi` BEFORE INSERT ON `standard_milestone` FOR EACH ROW SET NEW.createUser = COALESCE(NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(NEW.updateUser, @current_user_id, NEW.createUser, 1);
DROP TRIGGER IF EXISTS `trg_standard_milestone_bu`;
CREATE TRIGGER `trg_standard_milestone_bu` BEFORE UPDATE ON `standard_milestone` FOR EACH ROW SET NEW.createUser = COALESCE(OLD.createUser, NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(@current_user_id, NEW.updateUser, OLD.updateUser, NEW.createUser, 1);

DROP TRIGGER IF EXISTS `trg_role_bi`;
CREATE TRIGGER `trg_role_bi` BEFORE INSERT ON `role` FOR EACH ROW SET NEW.createUser = COALESCE(NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(NEW.updateUser, @current_user_id, NEW.createUser, 1);
DROP TRIGGER IF EXISTS `trg_role_bu`;
CREATE TRIGGER `trg_role_bu` BEFORE UPDATE ON `role` FOR EACH ROW SET NEW.createUser = COALESCE(OLD.createUser, NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(@current_user_id, NEW.updateUser, OLD.updateUser, NEW.createUser, 1);

DROP TRIGGER IF EXISTS `trg_organ_bi`;
CREATE TRIGGER `trg_organ_bi` BEFORE INSERT ON `organ` FOR EACH ROW SET NEW.createUser = COALESCE(NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(NEW.updateUser, @current_user_id, NEW.createUser, 1);
DROP TRIGGER IF EXISTS `trg_organ_bu`;
CREATE TRIGGER `trg_organ_bu` BEFORE UPDATE ON `organ` FOR EACH ROW SET NEW.createUser = COALESCE(OLD.createUser, NEW.createUser, @current_user_id, 1), NEW.updateUser = COALESCE(@current_user_id, NEW.updateUser, OLD.updateUser, NEW.createUser, 1);

SELECT '095 migration completed: add createUser/updateUser to all business tables' AS message;
