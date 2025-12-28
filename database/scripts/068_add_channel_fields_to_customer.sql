-- 增加客户成交状态、客户归属及渠道ID字段
-- 版本: 068
-- 描述: 在customer表中增加ifDeal, customerOwner, channelId字段

ALTER TABLE customer
ADD COLUMN ifDeal BOOLEAN DEFAULT FALSE COMMENT '是否成交',
ADD COLUMN customerOwner ENUM('自有客户', '渠道客户') DEFAULT '自有客户' COMMENT '客户归属：自有客户、渠道客户',
ADD COLUMN channelId BIGINT COMMENT '渠道ID，关联channel_distributor表';

-- 添加外键约束
ALTER TABLE customer
ADD CONSTRAINT fk_customer_channel_distributor
FOREIGN KEY (channelId) REFERENCES channel_distributor(channel_id);
