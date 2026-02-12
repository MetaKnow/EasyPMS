package com.missoft.pms.repository;

import com.missoft.pms.entity.ConstructingProjectComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConstructingProjectCommentRepository extends JpaRepository<ConstructingProjectComment, Long> {
    List<ConstructingProjectComment> findByProjectIdOrderByCreateTimeDesc(Long projectId);
}
