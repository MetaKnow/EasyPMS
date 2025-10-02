# 数据库表结构修改说明

## 概述
本次修改涉及多个数据库表的结构调整，主要目的是优化项目管理系统的数据模型，提高数据的组织性和关联性。

## 修改内容

### 1. standard_construct_step表修改 (027_modify_standard_construct_step_table.sql)
**删除字段：**
- director（负责人）
- planStartDate（计划开始日期）
- planEndDate（计划结束日期）
- actualStartDate（实际开始日期）
- actualEndDate（实际结束日期）
- planPeriod（计划工期）
- actualPeriod（实际工期）
- afterServiceProjectId（运维项目ID）

**新增字段：**
- type（步骤类型）

### 2. project_relations表修改 (028_modify_project_relations_table.sql)
**表名变更：** project_relations → project_sstep_relations

**删除字段：**
- milestoneId（里程碑ID）
- nstepId（非标步骤ID）

**新增字段：**
- director（负责人，user表外键）
- planStartDate（计划开始日期）
- planEndDate（计划结束日期）
- actualStartDate（实际开始日期）
- actualEndDate（实际结束日期）
- planPeriod（计划工期）
- actualPeriod（实际工期）

### 3. construct_deliverable表修改 (029_modify_construct_deliverable_table.sql)
**表名变更：** construct_deliverable → construct_standard_deliverable

**删除字段：**
- filePath（文件路径）
- fileSize（文件大小）
- uploadUser（上传人）

**新增字段：**
- isMustLoad（是否必须上传）
- sstepId（标准步骤ID，standard_construct_step表外键）
- milestoneId（里程碑ID，construct_milestone表外键）

### 4. 新建construct_deliverableFiles表 (030_create_construct_deliverableFiles_table.sql)
**新表字段：**
- fileId（文件ID，主键）
- filePath（文件路径）
- fileSize（文件大小）
- uploadUser（上传人，user表外键）
- deliverableId（交付物ID，construct_standard_deliverable表外键）
- projectId（项目ID，constructing_project表外键）
- sstepId（标准步骤ID，standard_construct_step表外键）
- nstepId（非标步骤ID，nonstandard_construct_step表外键）

### 5. nonstandard_construct_step表修改 (031_modify_nonstandard_construct_step_table.sql)
**新增字段：**
- projectId（项目ID，constructing_project表外键）

### 6. construct_milestone表修改 (032_modify_construct_milestone_table.sql)
**新增字段：**
- projectId（项目ID，constructing_project表外键）

### 7. construct_milestone表新增字段 (034_add_milestoneName_to_construct_milestone.sql)
**新增字段：**
- milestoneName（里程碑名称，必填）

## 执行顺序
建议按以下顺序执行迁移脚本：

1. 027_modify_standard_construct_step_table.sql
2. 031_modify_nonstandard_construct_step_table.sql
3. 032_modify_construct_milestone_table.sql
4. 034_add_milestoneName_to_construct_milestone.sql
5. 029_modify_construct_deliverable_table.sql
6. 030_create_construct_deliverableFiles_table.sql
7. 028_modify_project_relations_table.sql

**注意：** 033_execute_all_table_modifications.sql 已被删除，请按上述顺序逐个执行迁移脚本。

## 注意事项
1. **数据备份**：执行任何修改前，请务必备份数据库
2. **数据迁移**：某些字段的删除可能导致数据丢失，请提前做好数据迁移计划
3. **应用程序更新**：表结构修改后，需要同步更新相关的Java实体类、DAO层和业务逻辑
4. **外键约束**：新增的外键约束可能影响数据的插入和删除操作
5. **索引优化**：新增的索引将提高查询性能，但可能影响插入和更新性能

## 影响分析
- **数据完整性**：通过新增外键约束，提高了数据的完整性
- **查询性能**：新增的索引将提高相关查询的性能
- **存储优化**：将文件信息分离到独立表中，优化了存储结构
- **业务逻辑**：项目、步骤、里程碑和交付物之间的关系更加清晰