USE pms_db;

CREATE TABLE IF NOT EXISTS constructing_project_weeklyReport (
    weeklyReportId BIGINT AUTO_INCREMENT PRIMARY KEY,
    period VARCHAR(100) NOT NULL,
    submitUser BIGINT NOT NULL,
    submitDate DATE NOT NULL,
    weeklyWorkload DECIMAL(10,2),
    workDifficulties VARCHAR(2000),
    createTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updateTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE constructing_project_weeklyReport
ADD CONSTRAINT fk_constructing_project_weekly_report_submit_user
FOREIGN KEY (submitUser) REFERENCES user(userId)
ON DELETE RESTRICT ON UPDATE CASCADE;

CREATE INDEX idx_constructing_project_weekly_report_submit_user ON constructing_project_weeklyReport(submitUser);

SELECT 'Migration 088 executed: constructing_project_weeklyReport table created' AS message;
