-- 创建交付物文件表
-- 版本: 030
-- 描述: 创建construct_deliverableFiles表，包含文件信息和关联关系

CREATE TABLE construct_deliverableFiles (
    fileId BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '文件ID，主键',
    filePath VARCHAR(500) NOT NULL COMMENT '文件路径，必填',
    fileSize BIGINT COMMENT '文件大小（字节）',
    uploadUser BIGINT NOT NULL COMMENT '上传人，user表外键，必填',
    deliverableId BIGINT COMMENT '交付物ID，construct_standard_deliverable表外键',
    projectId BIGINT COMMENT '项目ID，constructing_project表外键',
    sstepId BIGINT COMMENT '标准步骤ID，standard_construct_step表外键',
    nstepId BIGINT COMMENT '非标步骤ID，nonstandard_construct_step表外键',
    createTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updateTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='交付物文件表';

-- 添加外键约束
ALTER TABLE construct_deliverableFiles 
ADD CONSTRAINT fk_construct_deliverableFiles_upload_user 
FOREIGN KEY (uploadUser) REFERENCES user(userId) 
ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE construct_deliverableFiles 
ADD CONSTRAINT fk_construct_deliverableFiles_deliverable 
FOREIGN KEY (deliverableId) REFERENCES construct_standard_deliverable(deliverableId) 
ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE construct_deliverableFiles 
ADD CONSTRAINT fk_construct_deliverableFiles_project 
FOREIGN KEY (projectId) REFERENCES constructing_project(projectId) 
ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE construct_deliverableFiles 
ADD CONSTRAINT fk_construct_deliverableFiles_sstep 
FOREIGN KEY (sstepId) REFERENCES standard_construct_step(sstepId) 
ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE construct_deliverableFiles 
ADD CONSTRAINT fk_construct_deliverableFiles_nstep 
FOREIGN KEY (nstepId) REFERENCES nonstandard_construct_step(nstepId) 
ON DELETE SET NULL ON UPDATE CASCADE;

-- 创建索引
CREATE INDEX idx_construct_deliverableFiles_file_path ON construct_deliverableFiles(filePath);
CREATE INDEX idx_construct_deliverableFiles_upload_user ON construct_deliverableFiles(uploadUser);
CREATE INDEX idx_construct_deliverableFiles_deliverable ON construct_deliverableFiles(deliverableId);
CREATE INDEX idx_construct_deliverableFiles_project ON construct_deliverableFiles(projectId);
CREATE INDEX idx_construct_deliverableFiles_sstep ON construct_deliverableFiles(sstepId);
CREATE INDEX idx_construct_deliverableFiles_nstep ON construct_deliverableFiles(nstepId);