-- 创建接口表
-- 版本: 043
-- 描述: 创建interface表，包含接口相关信息及外键关系

-- 创建interface表
CREATE TABLE interface (
    interfaceId BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '接口ID（主键）',
    projectId BIGINT COMMENT '项目ID（外键）',
    interfaceType VARCHAR(100) NOT NULL COMMENT '接口类型（必填）',
    integrationSysName VARCHAR(200) NOT NULL COMMENT '集成系统名称（必填）',
    integrationSysManufacturer VARCHAR(200) NOT NULL COMMENT '集成系统厂商（必填）',
    archieveDataCategory VARCHAR(100) COMMENT '归档数据类别',
    archieveInterfaceImpl VARCHAR(200) COMMENT '归档接口实现方式',
    milestoneId BIGINT COMMENT '里程碑ID（外键）',
    sstepId BIGINT COMMENT '标准步骤ID（外键）',
    createTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updateTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='接口表';

-- 创建外键约束
ALTER TABLE interface 
ADD CONSTRAINT fk_interface_project 
FOREIGN KEY (projectId) REFERENCES constructing_project(projectId) 
ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE interface 
ADD CONSTRAINT fk_interface_milestone 
FOREIGN KEY (milestoneId) REFERENCES construct_milestone(milestoneId) 
ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE interface 
ADD CONSTRAINT fk_interface_sstep 
FOREIGN KEY (sstepId) REFERENCES standard_construct_step(sstepId) 
ON DELETE SET NULL ON UPDATE CASCADE;

-- 创建索引以提高查询性能
CREATE INDEX idx_interface_project ON interface(projectId);
CREATE INDEX idx_interface_type ON interface(interfaceType);
CREATE INDEX idx_interface_milestone ON interface(milestoneId);
CREATE INDEX idx_interface_sstep ON interface(sstepId);
CREATE INDEX idx_interface_integration_sys ON interface(integrationSysName);
CREATE INDEX idx_interface_manufacturer ON interface(integrationSysManufacturer);