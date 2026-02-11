USE pms_db;

CREATE TABLE IF NOT EXISTS afterService_leads_deliverableFiles (
    fileId BIGINT AUTO_INCREMENT PRIMARY KEY,
    filePath VARCHAR(500) NOT NULL,
    fileSize BIGINT,
    uploadUser BIGINT NOT NULL,
    afterServiceProjectId BIGINT NOT NULL,
    leadsId BIGINT NOT NULL,
    createTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updateTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE afterService_leads_deliverableFiles
ADD CONSTRAINT fk_afterservice_leads_files_project
FOREIGN KEY (afterServiceProjectId) REFERENCES afterService_project(projectId)
ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE afterService_leads_deliverableFiles
ADD CONSTRAINT fk_afterservice_leads_files_leads
FOREIGN KEY (leadsId) REFERENCES afterService_leads(leadsId)
ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE afterService_leads_deliverableFiles
ADD CONSTRAINT fk_afterservice_leads_files_upload_user
FOREIGN KEY (uploadUser) REFERENCES user(userId)
ON DELETE RESTRICT ON UPDATE CASCADE;

CREATE INDEX idx_afterservice_leads_files_project ON afterService_leads_deliverableFiles(afterServiceProjectId);
CREATE INDEX idx_afterservice_leads_files_leads ON afterService_leads_deliverableFiles(leadsId);
CREATE INDEX idx_afterservice_leads_files_upload_user ON afterService_leads_deliverableFiles(uploadUser);
