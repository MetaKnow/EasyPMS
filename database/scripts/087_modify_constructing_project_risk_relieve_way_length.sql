USE pms_db;

ALTER TABLE constructing_project_risk
MODIFY COLUMN relieveWay VARCHAR(2000) NULL;

SELECT 'Migration 087 executed: constructing_project_risk.relieveWay length updated' AS message;
