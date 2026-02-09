USE pms_db;

CREATE TABLE IF NOT EXISTS extra_requirement (
    requirementId BIGINT AUTO_INCREMENT PRIMARY KEY,
    requirementName VARCHAR(200) NOT NULL,
    isPay TINYINT(1) NOT NULL DEFAULT 0,
    payAmount DECIMAL(12,2) NULL,
    isDeliver TINYINT(1) NOT NULL DEFAULT 0,
    isComplete TINYINT(1) NOT NULL DEFAULT 0,
    isProductization TINYINT(1) NOT NULL DEFAULT 0,
    workload DECIMAL(10,2) NULL,
    developer BIGINT NULL,
    INDEX idx_extra_requirement_name (requirementName),
    INDEX idx_extra_requirement_developer (developer),
    FOREIGN KEY (developer) REFERENCES user(userId) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SELECT 'Migration 071 executed: extra_requirement table ensured' AS message;
