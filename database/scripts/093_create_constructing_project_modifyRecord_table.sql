USE pms_db;

CREATE TABLE IF NOT EXISTS constructing_project_modifyRecord (
    recordId BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    modifyUser BIGINT NOT NULL COMMENT '修改人（user表外键）',
    modifyDate DATE NOT NULL COMMENT '修改日期',
    modifyAction ENUM(
        '修改项目开始日期',
        '修改项目计划结束日期',
        '修改项目实际结束日期',
        '修改项目验收日期',
        '修改项目建设内容',
        '修改步骤负责人',
        '修改步骤计划开始日期',
        '修改步骤计划结束日期',
        '修改步骤实际开始日期',
        '修改步骤实际结束日期',
        '修改步骤或里程碑交付物',
        '新增合同外需求',
        '修改合同外需求',
        '新增项目风险',
        '修改项目风险'
    ) NOT NULL COMMENT '修改行为',
    modifyDescription VARCHAR(2000) NULL COMMENT '修改描述',
    projectId BIGINT NOT NULL COMMENT '项目ID（constructing_project表外键）',
    createTime TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updateTime TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='在建项目修改记录';

ALTER TABLE constructing_project_modifyRecord
ADD CONSTRAINT fk_constructing_project_modifyRecord_modifyUser
FOREIGN KEY (modifyUser) REFERENCES user(userId)
ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE constructing_project_modifyRecord
ADD CONSTRAINT fk_constructing_project_modifyRecord_project
FOREIGN KEY (projectId) REFERENCES constructing_project(projectId)
ON DELETE CASCADE ON UPDATE CASCADE;

CREATE INDEX idx_constructing_project_modifyRecord_modifyUser ON constructing_project_modifyRecord(modifyUser);
CREATE INDEX idx_constructing_project_modifyRecord_project ON constructing_project_modifyRecord(projectId);

SELECT 'Migration 093 executed: constructing_project_modifyRecord table created' AS message;
