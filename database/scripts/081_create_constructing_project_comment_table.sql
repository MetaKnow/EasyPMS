USE pms_db;

CREATE TABLE IF NOT EXISTS constructing_project_comment (
    commentId BIGINT AUTO_INCREMENT PRIMARY KEY,
    projectId BIGINT NOT NULL,
    userId BIGINT NOT NULL,
    content VARCHAR(1000) NOT NULL,
    createTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updateTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE constructing_project_comment
ADD CONSTRAINT fk_constructing_project_comment_project
FOREIGN KEY (projectId) REFERENCES constructing_project(projectId)
ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE constructing_project_comment
ADD CONSTRAINT fk_constructing_project_comment_user
FOREIGN KEY (userId) REFERENCES user(userId)
ON DELETE RESTRICT ON UPDATE CASCADE;

CREATE INDEX idx_constructing_project_comment_project ON constructing_project_comment(projectId);
CREATE INDEX idx_constructing_project_comment_user ON constructing_project_comment(userId);
