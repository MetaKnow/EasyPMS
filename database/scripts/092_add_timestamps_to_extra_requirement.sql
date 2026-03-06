USE pms_db;

ALTER TABLE extra_requirement
    ADD COLUMN createTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN updateTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

UPDATE extra_requirement
SET createTime = IFNULL(createTime, CURRENT_TIMESTAMP),
    updateTime = IFNULL(updateTime, CURRENT_TIMESTAMP);

SELECT 'Migration 092 executed: extra_requirement timestamps added' AS message;
