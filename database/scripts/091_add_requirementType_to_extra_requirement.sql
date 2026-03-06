USE pms_db;

ALTER TABLE extra_requirement
    ADD COLUMN requirementType VARCHAR(20) NULL AFTER requirementName;

SELECT 'Migration 091 executed: extra_requirement.requirementType added' AS message;
