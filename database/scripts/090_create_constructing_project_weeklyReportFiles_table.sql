USE pms_db;

CREATE TABLE IF NOT EXISTS constructing_project_weeklyReportFiles (
    fileId BIGINT AUTO_INCREMENT PRIMARY KEY,
    filePath VARCHAR(500) NOT NULL,
    fileSize BIGINT,
    uploadUser BIGINT NOT NULL,
    projectId BIGINT NOT NULL,
    weeklyReportId BIGINT NOT NULL,
    createTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updateTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE constructing_project_weeklyReportFiles
ADD CONSTRAINT fk_constructing_project_weekly_report_files_project
FOREIGN KEY (projectId) REFERENCES constructing_project(projectId)
ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE constructing_project_weeklyReportFiles
ADD CONSTRAINT fk_constructing_project_weekly_report_files_weekly_report
FOREIGN KEY (weeklyReportId) REFERENCES constructing_project_weeklyReport(weeklyReportId)
ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE constructing_project_weeklyReportFiles
ADD CONSTRAINT fk_constructing_project_weekly_report_files_upload_user
FOREIGN KEY (uploadUser) REFERENCES user(userId)
ON DELETE RESTRICT ON UPDATE CASCADE;

CREATE INDEX idx_constructing_project_weekly_report_files_project ON constructing_project_weeklyReportFiles(projectId);
CREATE INDEX idx_constructing_project_weekly_report_files_weekly_report ON constructing_project_weeklyReportFiles(weeklyReportId);
CREATE INDEX idx_constructing_project_weekly_report_files_upload_user ON constructing_project_weeklyReportFiles(uploadUser);

SELECT 'Migration 090 executed: constructing_project_weeklyReportFiles table created' AS message;
