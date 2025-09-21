-- 创建交付物表
-- 版本: 015
-- 描述: 创建交付物表，包含交付物文件信息和上传人信息

CREATE TABLE construct_deliverable (
    deliverableId BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '交付物ID，主键',
    deliverableName VARCHAR(200) COMMENT '交付物名称',
    filePath VARCHAR(500) COMMENT '文件路径',
    fileSize BIGINT COMMENT '文件大小（字节）',
    uploadUser BIGINT NOT NULL COMMENT '上传人，user表外键，必填',
    createTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updateTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='交付物表';

-- 添加外键约束
ALTER TABLE construct_deliverable 
ADD CONSTRAINT fk_construct_deliverable_upload_user 
FOREIGN KEY (uploadUser) REFERENCES user(userId) 
ON DELETE RESTRICT ON UPDATE CASCADE;

-- 创建索引
CREATE INDEX idx_construct_deliverable_name ON construct_deliverable(deliverableName);
CREATE INDEX idx_construct_deliverable_upload_user ON construct_deliverable(uploadUser);
CREATE INDEX idx_construct_deliverable_file_path ON construct_deliverable(filePath);