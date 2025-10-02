-- 创建渠道商表
-- 文件: 037_create_channel_distributor_table.sql
-- 描述: 创建渠道商维护模块的数据表

CREATE TABLE IF NOT EXISTS channel_distributor (
    channel_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '渠道ID（主键）',
    channel_name VARCHAR(100) NOT NULL COMMENT '渠道名称',
    contactor VARCHAR(50) NOT NULL COMMENT '联系人',
    phone_number VARCHAR(20) NOT NULL COMMENT '联系方式',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='渠道商信息表';

-- 创建索引
CREATE INDEX idx_channel_name ON channel_distributor(channel_name);
CREATE INDEX idx_contactor ON channel_distributor(contactor);