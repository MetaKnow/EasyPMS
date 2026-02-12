USE pms_db;

CREATE TABLE IF NOT EXISTS constructing_project_comment_reply (
    replyId BIGINT AUTO_INCREMENT PRIMARY KEY,
    projectId BIGINT NOT NULL,
    commentId BIGINT NOT NULL,
    userId BIGINT NOT NULL,
    content VARCHAR(2000) NOT NULL,
    createTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updateTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE constructing_project_comment_reply
ADD CONSTRAINT fk_constructing_project_comment_reply_project
FOREIGN KEY (projectId) REFERENCES constructing_project(projectId)
ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE constructing_project_comment_reply
ADD CONSTRAINT fk_constructing_project_comment_reply_comment
FOREIGN KEY (commentId) REFERENCES constructing_project_comment(commentId)
ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE constructing_project_comment_reply
ADD CONSTRAINT fk_constructing_project_comment_reply_user
FOREIGN KEY (userId) REFERENCES user(userId)
ON DELETE RESTRICT ON UPDATE CASCADE;

CREATE INDEX idx_constructing_project_comment_reply_project ON constructing_project_comment_reply(projectId);
CREATE INDEX idx_constructing_project_comment_reply_comment ON constructing_project_comment_reply(commentId);
CREATE INDEX idx_constructing_project_comment_reply_user ON constructing_project_comment_reply(userId);

CREATE TABLE IF NOT EXISTS constructing_project_comment_replyFiles (
    fileId BIGINT AUTO_INCREMENT PRIMARY KEY,
    filePath VARCHAR(500) NOT NULL,
    fileSize BIGINT,
    uploadUser BIGINT NOT NULL,
    projectId BIGINT NOT NULL,
    commentId BIGINT NOT NULL,
    replyId BIGINT NOT NULL,
    createTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updateTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE constructing_project_comment_replyFiles
ADD CONSTRAINT fk_constructing_project_comment_reply_files_project
FOREIGN KEY (projectId) REFERENCES constructing_project(projectId)
ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE constructing_project_comment_replyFiles
ADD CONSTRAINT fk_constructing_project_comment_reply_files_comment
FOREIGN KEY (commentId) REFERENCES constructing_project_comment(commentId)
ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE constructing_project_comment_replyFiles
ADD CONSTRAINT fk_constructing_project_comment_reply_files_reply
FOREIGN KEY (replyId) REFERENCES constructing_project_comment_reply(replyId)
ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE constructing_project_comment_replyFiles
ADD CONSTRAINT fk_constructing_project_comment_reply_files_upload_user
FOREIGN KEY (uploadUser) REFERENCES user(userId)
ON DELETE RESTRICT ON UPDATE CASCADE;

CREATE INDEX idx_constructing_project_comment_reply_files_project ON constructing_project_comment_replyFiles(projectId);
CREATE INDEX idx_constructing_project_comment_reply_files_comment ON constructing_project_comment_replyFiles(commentId);
CREATE INDEX idx_constructing_project_comment_reply_files_reply ON constructing_project_comment_replyFiles(replyId);
CREATE INDEX idx_constructing_project_comment_reply_files_upload_user ON constructing_project_comment_replyFiles(uploadUser);
