package com.missoft.pms.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 类级注释：项目评论回复实体
 */
@Entity
@Table(name = "constructing_project_comment_reply")
public class ConstructingProjectCommentReply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "replyId")
    private Long replyId;

    @Column(name = "projectId", nullable = false)
    private Long projectId;

    @Column(name = "commentId", nullable = false)
    private Long commentId;

    @Column(name = "userId", nullable = false)
    private Long userId;

    @Column(name = "content", nullable = false, length = 2000)
    private String content;

    @Column(name = "createTime", updatable = false)
    private LocalDateTime createTime;

    @Column(name = "updateTime")
    private LocalDateTime updateTime;

    public Long getReplyId() { return replyId; }
    public void setReplyId(Long replyId) { this.replyId = replyId; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public Long getCommentId() { return commentId; }
    public void setCommentId(Long commentId) { this.commentId = commentId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

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
