USE pms_db;

CREATE TABLE IF NOT EXISTS constructing_project_risk (
    riskId BIGINT AUTO_INCREMENT PRIMARY KEY,
    riskType ENUM('需求控制','需求敲定','协调配合','数据迁移','商务关系','其他') NOT NULL,
    riskLevel ENUM('高','中','低') NOT NULL,
    isRelieve TINYINT(1) NOT NULL DEFAULT 0,
    relieveWay VARCHAR(200) NULL,
    riskDescription VARCHAR(2000) NULL,
    riskEvaluate VARCHAR(2000) NULL,
    creator BIGINT NOT NULL,
    projectId BIGINT NOT NULL,
    createTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updateTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE constructing_project_risk
ADD CONSTRAINT fk_constructing_project_risk_creator
FOREIGN KEY (creator) REFERENCES user(userId)
ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE constructing_project_risk
ADD CONSTRAINT fk_constructing_project_risk_project
FOREIGN KEY (projectId) REFERENCES constructing_project(projectId)
ON DELETE CASCADE ON UPDATE CASCADE;

CREATE INDEX idx_constructing_project_risk_creator ON constructing_project_risk(creator);
CREATE INDEX idx_constructing_project_risk_project ON constructing_project_risk(projectId);

SELECT 'Migration 085 executed: constructing_project_risk table created' AS message;
