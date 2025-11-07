-- 删除interface表sstepId字段
-- 版本: 053
-- 描述: 删除interface表的sstepId字段及其外键与索引

-- 先删除外键约束
ALTER TABLE interface DROP FOREIGN KEY fk_interface_sstep;

-- 删除与sstepId相关的索引
DROP INDEX idx_interface_sstep ON interface;

-- 删除字段
ALTER TABLE interface 
DROP COLUMN sstepId;