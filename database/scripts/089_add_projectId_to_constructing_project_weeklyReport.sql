USE pms_db;

ALTER TABLE constructing_project_weeklyReport
ADD COLUMN projectId BIGINT NOT NULL;

ALTER TABLE constructing_project_weeklyReport
ADD CONSTRAINT fk_constructing_project_weekly_report_project
FOREIGN KEY (projectId) REFERENCES constructing_project(projectId)
ON DELETE CASCADE ON UPDATE CASCADE;

CREATE INDEX idx_constructing_project_weekly_report_project ON constructing_project_weeklyReport(projectId);

SELECT 'Migration 089 executed: constructing_project_weeklyReport.projectId added' AS message;
