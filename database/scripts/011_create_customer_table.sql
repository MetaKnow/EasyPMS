-- 创建客户表
-- 版本: 011
-- 描述: 创建客户表，包含客户基本信息和等级分类

CREATE TABLE customer (
    customerId BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '客户ID，主键',
    customerName VARCHAR(100) NOT NULL COMMENT '客户名称，必填',
    contact VARCHAR(50) NOT NULL COMMENT '联系人，必填',
    phoneNumber VARCHAR(20) NOT NULL COMMENT '联系方式，必填',
    province VARCHAR(50) NOT NULL COMMENT '省份，必填',
    customerRank ENUM('战略客户', '重要客户', '一般客户') NOT NULL DEFAULT '一般客户' COMMENT '客户等级，值域为战略客户、重要客户、一般客户',
    createTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updateTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户表';

-- 创建索引
CREATE INDEX idx_customer_name ON customer(customerName);
CREATE INDEX idx_customer_rank ON customer(customerRank);
CREATE INDEX idx_customer_province ON customer(province);
CREATE INDEX idx_customer_contact ON customer(contact);