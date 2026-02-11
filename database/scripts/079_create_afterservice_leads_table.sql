USE pms_db;

CREATE TABLE IF NOT EXISTS afterService_leads (
    leadsId BIGINT AUTO_INCREMENT PRIMARY KEY,
    afterServiceProjectId BIGINT NOT NULL,
    isTransform TINYINT(1) NOT NULL DEFAULT 0,
    leadsFinder BIGINT NOT NULL,
    leadsSource ENUM('用户主动寻求', '销售回访', '售后维护') NOT NULL,
    leadsDescript TEXT,
    createTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updateTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE afterService_leads
ADD CONSTRAINT fk_afterservice_leads_project
FOREIGN KEY (afterServiceProjectId) REFERENCES afterService_project(projectId)
ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE afterService_leads
ADD CONSTRAINT fk_afterservice_leads_finder
FOREIGN KEY (leadsFinder) REFERENCES user(userId)
ON DELETE RESTRICT ON UPDATE CASCADE;

CREATE INDEX idx_afterservice_leads_project ON afterService_leads(afterServiceProjectId);
CREATE INDEX idx_afterservice_leads_finder ON afterService_leads(leadsFinder);
CREATE INDEX idx_afterservice_leads_source ON afterService_leads(leadsSource);
CREATE INDEX idx_afterservice_leads_transform ON afterService_leads(isTransform);
