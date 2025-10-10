# 项目管理系统 (PMS) 数据库设计文档

## 1. 概述

本文档描述了项目管理系统（PMS）的数据库设计，包括数据表结构、关系、约束和索引等信息。

### 1.1 数据库信息
- **数据库名称**: pms_db
- **字符集**: utf8mb4
- **排序规则**: utf8mb4_unicode_ci
- **存储引擎**: InnoDB
- **版本**: 1.0

### 1.2 设计原则
- 遵循第三范式（3NF）
- 使用外键约束保证数据完整性
- 合理使用索引提高查询性能
- 统一字段命名规范
- 支持审计跟踪（创建时间、更新时间）

## 2. 数据库表结构

### 2.1 系统管理模块

#### 2.1.1 用户表 (user)

| 字段名 | 数据类型 | 长度 | 允许空 | 默认值 | 约束 | 描述 |
|--------|----------|------|--------|--------|------|------|
| userId | BIGINT | - | NO | | PRIMARY KEY, AUTO_INCREMENT | 用户ID |
| userName | VARCHAR | 50 | NO | | UNIQUE | 用户名 |
| password | VARCHAR | 255 | NO | | | 密码（加密存储） |
| organId | BIGINT | - | YES | | FOREIGN KEY -> organ(organId) | 所属组织ID |
| roleId | BIGINT | - | YES | | FOREIGN KEY -> role(roleId) | 角色ID |
| name | VARCHAR | 100 | YES | | | 真实姓名 |
| locked | TINYINT | - | NO | 0 | | 锁定状态（0-正常，1-锁定） |
| createTime | TIMESTAMP | - | NO | CURRENT_TIMESTAMP | | 创建时间 |
| updateTime | TIMESTAMP | - | NO | CURRENT_TIMESTAMP ON UPDATE | | 更新时间 |

**索引**:
- idx_user_name (userName)
- idx_organ_id (organId)
- idx_role_id (roleId)

**外键约束**:
- fk_user_organ (organId) REFERENCES organ(organId) ON DELETE SET NULL
- fk_user_role (roleId) REFERENCES role(roleId) ON DELETE SET NULL

#### 2.1.2 角色表 (role)

| 字段名 | 数据类型 | 长度 | 允许空 | 默认值 | 约束 | 描述 |
|--------|----------|------|--------|--------|------|------|
| roleId | BIGINT | - | NO | | PRIMARY KEY, AUTO_INCREMENT | 角色ID |
| roleName | VARCHAR | 100 | NO | | UNIQUE | 角色名称 |
| description | TEXT | - | YES | | | 角色描述 |

**索引**:
- idx_role_name (roleName)

#### 2.1.3 组织机构表 (organ)

| 字段名 | 数据类型 | 长度 | 允许空 | 默认值 | 约束 | 描述 |
|--------|----------|------|--------|--------|------|------|
| organId | BIGINT | - | NO | | PRIMARY KEY, AUTO_INCREMENT | 组织ID |
| organName | VARCHAR | 200 | NO | | | 组织名称 |
| parentOrganId | BIGINT | - | YES | | FOREIGN KEY -> organ(organId) | 父组织ID |
| path | VARCHAR | 500 | YES | | | 组织路径 |

**索引**:
- idx_organ_name (organName)
- idx_parent_organ (parentOrganId)

**外键约束**:
- fk_organ_parent (parentOrganId) REFERENCES organ(organId) ON DELETE SET NULL

### 2.2 基础数据模块

#### 2.2.1 客户表 (customer)

| 字段名 | 数据类型 | 长度 | 允许空 | 默认值 | 约束 | 描述 |
|--------|----------|------|--------|--------|------|------|
| customerId | BIGINT | - | NO | | PRIMARY KEY, AUTO_INCREMENT | 客户ID |
| customerName | VARCHAR | 100 | NO | | | 客户名称 |
| contact | VARCHAR | 50 | NO | | | 联系人 |
| phoneNumber | VARCHAR | 20 | NO | | | 联系方式 |
| province | VARCHAR | 20 | NO | | | 省份 |
| customerRank | VARCHAR | 20 | NO | | | 客户等级 |
| createTime | TIMESTAMP | - | NO | CURRENT_TIMESTAMP | | 创建时间 |
| updateTime | TIMESTAMP | - | NO | CURRENT_TIMESTAMP ON UPDATE | | 更新时间 |

**索引**:
- idx_customer_name (customerName)
- idx_province (province)
- idx_customer_rank (customerRank)

#### 2.2.2 渠道商表 (channel_distributor)

| 字段名 | 数据类型 | 长度 | 允许空 | 默认值 | 约束 | 描述 |
|--------|----------|------|--------|--------|------|------|
| channel_id | BIGINT | - | NO | | PRIMARY KEY, AUTO_INCREMENT | 渠道ID |
| channel_name | VARCHAR | 100 | NO | | | 渠道名称 |
| contactor | VARCHAR | 50 | NO | | | 联系人 |
| phone_number | VARCHAR | 20 | NO | | | 联系方式 |
| created_at | TIMESTAMP | - | NO | CURRENT_TIMESTAMP | | 创建时间 |
| updated_at | TIMESTAMP | - | NO | CURRENT_TIMESTAMP ON UPDATE | | 更新时间 |

**索引**:
- idx_channel_name (channel_name)

#### 2.2.3 软件产品表 (archieveSoft)

| 字段名 | 数据类型 | 长度 | 允许空 | 默认值 | 约束 | 描述 |
|--------|----------|------|--------|--------|------|------|
| softId | BIGINT | - | NO | | PRIMARY KEY, AUTO_INCREMENT | 软件ID |
| softName | VARCHAR | 200 | NO | | | 软件名称 |
| softVersion | VARCHAR | 50 | NO | | | 软件版本 |
| createTime | TIMESTAMP | - | NO | CURRENT_TIMESTAMP | | 创建时间 |
| updateTime | TIMESTAMP | - | NO | CURRENT_TIMESTAMP ON UPDATE | | 更新时间 |

**索引**:
- idx_soft_name (softName)

### 2.3 项目管理模块

#### 2.3.1 在建项目表 (constructing_project)

| 字段名 | 数据类型 | 长度 | 允许空 | 默认值 | 约束 | 描述 |
|--------|----------|------|--------|--------|------|------|
| projectId | BIGINT | - | NO | | PRIMARY KEY, AUTO_INCREMENT | 项目ID |
| projectNum | VARCHAR | 50 | NO | | UNIQUE | 项目编号 |
| year | INT | - | NO | | | 年度 |
| projectName | VARCHAR | 200 | NO | | | 项目名称 |
| projectType | VARCHAR | 50 | NO | | | 项目类型 |
| projectLeader | BIGINT | - | YES | | FOREIGN KEY -> user(userId) | 项目负责人ID |
| saleLeader | BIGINT | - | YES | | FOREIGN KEY -> user(userId) | 商务负责人ID |
| customerId | BIGINT | - | YES | | FOREIGN KEY -> customer(customerId) | 客户ID |
| projectState | VARCHAR | 50 | NO | '待开始' | | 项目状态 |
| softId | BIGINT | - | YES | | FOREIGN KEY -> archieveSoft(softId) | 档案软件ID |
| startDate | DATE | - | YES | | | 开始日期 |
| planEndDate | DATE | - | YES | | | 计划结束日期 |
| actualEndDate | DATE | - | YES | | | 实际结束日期 |
| planPeriod | INT | - | YES | | | 项目预计工期（天） |
| actualPeriod | INT | - | YES | | | 项目实际工期（天） |
| isAgent | INT | - | NO | 0 | | 是否渠道项目（0-否，1-是） |
| channelId | BIGINT | - | YES | | FOREIGN KEY -> channel_distributor(channel_id) | 渠道ID |
| value | DECIMAL | 15,2 | YES | | | 项目金额 |
| receivedMoney | DECIMAL | 15,2 | NO | 0.00 | | 已回款金额 |
| unreceiveMoney | DECIMAL | 15,2 | NO | 0.00 | | 未回款金额 |
| acceptanceDate | DATE | - | YES | | | 项目验收日期 |
| constructContent | VARCHAR | 100 | YES | | | 项目建设内容 |
| createTime | TIMESTAMP | - | YES | | | 创建时间 |
| updateTime | TIMESTAMP | - | YES | | | 更新时间 |

**索引**:
- idx_project_num (projectNum)
- idx_year (year)
- idx_project_state (projectState)
- idx_start_date (startDate)
- idx_project_leader (projectLeader)
- idx_sale_leader (saleLeader)
- idx_customer_id (customerId)

**外键约束**:
- fk_project_leader (projectLeader) REFERENCES user(userId) ON DELETE SET NULL
- fk_sale_leader (saleLeader) REFERENCES user(userId) ON DELETE SET NULL
- fk_project_customer (customerId) REFERENCES customer(customerId) ON DELETE SET NULL
- fk_project_soft (softId) REFERENCES archieveSoft(softId) ON DELETE SET NULL
- fk_project_channel (channelId) REFERENCES channel_distributor(channel_id) ON DELETE SET NULL

### 2.4 标准化模块

#### 2.4.1 标准里程碑表 (standard_milestone)

| 字段名 | 数据类型 | 长度 | 允许空 | 默认值 | 约束 | 描述 |
|--------|----------|------|--------|--------|------|------|
| milestone_id | BIGINT | - | NO | | PRIMARY KEY, AUTO_INCREMENT | 里程碑ID |
| milestone_name | VARCHAR | 100 | NO | | | 里程碑名称 |
| create_time | TIMESTAMP | - | NO | CURRENT_TIMESTAMP | | 创建时间 |
| update_time | TIMESTAMP | - | NO | CURRENT_TIMESTAMP ON UPDATE | | 更新时间 |

**索引**:
- idx_milestone_name (milestone_name)

#### 2.4.2 标准交付步骤表 (standard_construct_step)

| 字段名 | 数据类型 | 长度 | 允许空 | 默认值 | 约束 | 描述 |
|--------|----------|------|--------|--------|------|------|
| sstepId | BIGINT | - | NO | | PRIMARY KEY, AUTO_INCREMENT | 标准步骤ID |
| sstepName | VARCHAR | 200 | NO | | | 标准步骤名称 |
| type | VARCHAR | 50 | YES | | | 步骤类型 |
| systemName | VARCHAR | 100 | YES | | | 档案系统名称 |
| smilestoneId | BIGINT | - | YES | | FOREIGN KEY -> standard_milestone(milestone_id) | 标准里程碑ID |
| createTime | TIMESTAMP | - | YES | | | 创建时间 |
| updateTime | TIMESTAMP | - | YES | | | 更新时间 |

**索引**:
- idx_sstep_name (sstepName)
- idx_step_type (type)
- idx_system_name (systemName)

**外键约束**:
- fk_step_milestone (smilestoneId) REFERENCES standard_milestone(milestone_id) ON DELETE SET NULL

#### 2.4.3 标准交付物表 (standard_deliverable)

| 字段名 | 数据类型 | 长度 | 允许空 | 默认值 | 约束 | 描述 |
|--------|----------|------|--------|--------|------|------|
| deliverableId | BIGINT | - | NO | | PRIMARY KEY, AUTO_INCREMENT | 交付物ID |
| deliverableName | VARCHAR | 200 | YES | | | 交付物名称 |
| systemName | VARCHAR | 100 | YES | | | 系统名称 |
| deliverableType | VARCHAR | 50 | YES | | | 交付物类型 |
| isMustLoad | BOOLEAN | - | YES | | | 是否必须上传 |
| sstepId | BIGINT | - | YES | | FOREIGN KEY -> standard_construct_step(sstepId) | 标准步骤ID |
| milestoneId | BIGINT | - | YES | | FOREIGN KEY -> standard_milestone(milestone_id) | 里程碑ID |
| createTime | TIMESTAMP | - | YES | | | 创建时间 |
| updateTime | TIMESTAMP | - | YES | | | 更新时间 |

**索引**:
- idx_deliverable_name (deliverableName)
- idx_deliverable_type (deliverableType)

**外键约束**:
- fk_deliverable_step (sstepId) REFERENCES standard_construct_step(sstepId) ON DELETE SET NULL
- fk_deliverable_milestone (milestoneId) REFERENCES standard_milestone(milestone_id) ON DELETE SET NULL

### 2.5 其他模块

#### 2.5.1 迁移管理表 (flyway_schema_history)

| 字段名 | 数据类型 | 长度 | 允许空 | 默认值 | 约束 | 描述 |
|--------|----------|------|--------|--------|------|------|
| installed_rank | INT | - | NO | | PRIMARY KEY | 安装序号 |
| version | VARCHAR | 50 | YES | | | 版本号 |
| description | VARCHAR | 200 | YES | | | 描述 |
| type | VARCHAR | 20 | NO | | | 类型 |
| script | VARCHAR | 1000 | NO | | | 脚本名称 |
| checksum | INT | - | YES | | | 校验和 |
| installed_by | VARCHAR | 100 | NO | | | 安装者 |
| installed_on | TIMESTAMP | - | NO | | | 安装时间 |
| execution_time | INT | - | YES | | | 执行时间 |
| success | BOOLEAN | - | NO | | | 是否成功 |

## 3. 数据库关系图

```
organ (组织机构)
├── organId (PK)
├── parentOrganId (FK -> organ.organId)
└── user (用户表)
    ├── userId (PK)
    ├── organId (FK -> organ.organId)
    ├── roleId (FK -> role.roleId)
    └── constructing_project (项目表)
        ├── projectId (PK)
        ├── projectLeader (FK -> user.userId)
        ├── saleLeader (FK -> user.userId)
        ├── customerId (FK -> customer.customerId)
        ├── softId (FK -> archieveSoft.softId)
        └── channelId (FK -> channel_distributor.channel_id)

role (角色表)
├── roleId (PK)
└── user.roleId (FK)

customer (客户表)
├── customerId (PK)
└── constructing_project.customerId (FK)

channel_distributor (渠道商表)
├── channel_id (PK)
└── constructing_project.channelId (FK)

archieveSoft (软件产品表)
├── softId (PK)
└── constructing_project.softId (FK)

standard_milestone (标准里程碑表)
├── milestone_id (PK)
├── standard_construct_step.smilestoneId (FK)
└── standard_deliverable.milestoneId (FK)

standard_construct_step (标准交付步骤表)
├── sstepId (PK)
├── smilestoneId (FK -> standard_milestone.milestone_id)
└── standard_deliverable.sstepId (FK)

standard_deliverable (标准交付物表)
├── deliverableId (PK)
├── sstepId (FK -> standard_construct_step.sstepId)
└── milestoneId (FK -> standard_milestone.milestone_id)
```

## 4. 数据字典

### 4.1 枚举值定义

#### 4.1.1 项目状态 (projectState)
- 待开始
- 进行中
- 已完成
- 已暂停
- 已取消

#### 4.1.2 项目类型 (projectType)
- 新建项目
- 扩展项目
- 维护项目
- 升级项目

#### 4.1.3 客户等级 (customerRank)
- 战略客户
- 重要客户
- 一般客户

#### 4.1.4 步骤类型 (type)
- 标准产品
- 接口开发
- 数据迁移
- 个性化功能开发
- 用户培训
- 系统上线试运行

#### 4.1.5 交付物类型 (deliverableType)
- 步骤交付物
- 里程碑交付物

### 4.2 业务规则

1. **用户权限规则**
   - 每个用户必须属于一个组织机构
   - 每个用户必须拥有一个角色
   - 用户被锁定后不能登录系统

2. **项目业务规则**
   - 项目编号必须唯一
   - 项目必须有项目负责人
   - 渠道项目必须关联渠道商

3. **组织机构规则**
   - 支持多级组织机构树形结构
   - 根节点的parentOrganId为NULL

4. **标准化管理规则**
   - 标准步骤必须关联标准里程碑
   - 标准交付物可以关联标准步骤或里程碑

## 5. 性能优化建议

### 5.1 索引优化
1. 为经常查询的字段创建索引
2. 为外键字段创建索引
3. 为经常用于排序的字段创建索引
4. 避免过多索引影响写入性能

### 5.2 查询优化
1. 使用EXPLAIN分析查询计划
2. 避免SELECT *，只查询需要的字段
3. 合理使用JOIN，避免笛卡尔积
4. 使用分页查询减少数据传输量

### 5.3 表结构优化
1. 定期分析表，优化表结构
2. 对于大表考虑分区
3. 合理设置字段长度，避免空间浪费

## 6. 数据安全

### 6.1 访问控制
1. 实施基于角色的访问控制（RBAC）
2. 限制数据库用户的权限
3. 定期审查数据库访问日志

### 6.2 数据加密
1. 用户密码使用加密存储
2. 敏感数据传输使用SSL加密
3. 定期备份数据库

### 6.3 审计跟踪
1. 记录数据变更日志
2. 追踪用户操作记录
3. 定期审计数据库操作

## 7. 备份与恢复

### 7.1 备份策略
1. 每日全量备份
2. 每小时增量备份
3. 备份文件异地存储

### 7.2 恢复策略
1. 制定详细的恢复流程
2. 定期测试恢复方案
3. 建立应急响应机制

## 8. 版本历史

| 版本 | 日期 | 修改人 | 修改内容 |
|------|------|--------|----------|
| 1.0.0 | 2024-01-01 | 开发团队 | 初始版本 |
| 1.1.0 | 2024-01-15 | 开发团队 | 添加渠道商管理模块 |
| 1.2.0 | 2024-01-20 | 开发团队 | 添加标准化管理模块 |

## 9. 附录

### 9.1 数据库连接配置
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/pms_db?useUnicode=true&characterEncoding=utf8mb4&useSSL=false&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

### 9.2 JPA配置
```properties
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
```

---

**文档版本**: 1.0.0
**最后更新**: 2024-01-20
**文档维护**: 开发团队