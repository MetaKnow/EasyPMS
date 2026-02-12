package com.missoft.pms.repository;

import com.missoft.pms.entity.ConstructingProjectCommentFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConstructingProjectCommentFileRepository extends JpaRepository<ConstructingProjectCommentFile, Long> {
    List<ConstructingProjectCommentFile> findByCommentIdOrderByCreateTimeDesc(Long commentId);
    List<ConstructingProjectCommentFile> findByProjectIdOrderByCreateTimeDesc(Long projectId);
}
