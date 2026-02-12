package com.missoft.pms.repository;

import com.missoft.pms.entity.ConstructingProjectCommentReplyFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 类级注释：项目评论回复附件仓库
 */
@Repository
public interface ConstructingProjectCommentReplyFileRepository extends JpaRepository<ConstructingProjectCommentReplyFile, Long> {
    List<ConstructingProjectCommentReplyFile> findByCommentIdOrderByCreateTimeAsc(Long commentId);
    List<ConstructingProjectCommentReplyFile> findByReplyIdOrderByCreateTimeAsc(Long replyId);
}
