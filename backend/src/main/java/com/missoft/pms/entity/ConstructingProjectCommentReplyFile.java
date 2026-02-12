package com.missoft.pms.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 类级注释：项目评论回复附件实体
 */
@Entity
@Table(name = "constructing_project_comment_replyFiles")
public class ConstructingProjectCommentReplyFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fileId")
    private Long fileId;

    @Column(name = "filePath", length = 500, nullable = false)
    private String filePath;

    @Column(name = "fileSize")
    private Long fileSize;

    @Column(name = "uploadUser", nullable = false)
    private Long uploadUser;

    @Column(name = "projectId", nullable = false)
    private Long projectId;

    @Column(name = "commentId", nullable = false)
    private Long commentId;

    @Column(name = "replyId", nullable = false)
    private Long replyId;

    @Column(name = "createTime", updatable = false)
    private LocalDateTime createTime;

    @Column(name = "updateTime")
    private LocalDateTime updateTime;

    public Long getFileId() { return fileId; }
    public void setFileId(Long fileId) { this.fileId = fileId; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public Long getUploadUser() { return uploadUser; }
    public void setUploadUser(Long uploadUser) { this.uploadUser = uploadUser; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public Long getCommentId() { return commentId; }
    public void setCommentId(Long commentId) { this.commentId = commentId; }

    public Long getReplyId() { return replyId; }
    public void setReplyId(Long replyId) { this.replyId = replyId; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createTime = now;
        this.updateTime = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updateTime = LocalDateTime.now();
    }
}
