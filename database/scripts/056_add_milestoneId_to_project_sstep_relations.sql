-- =============================================
-- 迁移：为 project_sstep_relations 表新增 milestoneId 外键列
-- 目的：使项目-标准步骤关系可关联到具体的项目里程碑（construct_milestone）
-- 注意：仅进行表结构变更，不插入或更新任何数据
-- =============================================

-- 添加里程碑外键列（允许为 NULL，以兼容现有数据）
ALTER TABLE `project_sstep_relations`
  ADD COLUMN `milestoneId` BIGINT NULL COMMENT '项目里程碑ID，construct_milestone表外键';

-- 为里程碑外键列创建索引（保障外键与查询性能）
CREATE INDEX `idx_psr_milestoneId` ON `project_sstep_relations`(`milestoneId`);

-- 建立外键约束：指向 construct_milestone(milestoneId)
ALTER TABLE `project_sstep_relations`
  ADD CONSTRAINT `fk_psr_milestone`
    FOREIGN KEY (`milestoneId`)
    REFERENCES `construct_milestone`(`milestoneId`)
    ON UPDATE CASCADE
    ON DELETE SET NULL;

-- 说明：
-- 1) milestoneId 设为可空，避免对历史数据造成破坏；
-- 2) ON DELETE SET NULL：删除里程碑后，仅清空关联列，保留关系行；
-- 3) ON UPDATE CASCADE：里程碑主键更新可同步到关系表；