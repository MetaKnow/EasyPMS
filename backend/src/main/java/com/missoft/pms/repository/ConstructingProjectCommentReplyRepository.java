package com.missoft.pms.repository;

import com.missoft.pms.entity.ConstructingProjectCommentReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 类级注释：项目评论回复仓库
 */
@Repository
public interface ConstructingProjectCommentReplyRepository extends JpaRepository<ConstructingProjectCommentReply, Long> {
    List<ConstructingProjectCommentReply> findByCommentIdOrderByCreateTimeAsc(Long commentId);
}
