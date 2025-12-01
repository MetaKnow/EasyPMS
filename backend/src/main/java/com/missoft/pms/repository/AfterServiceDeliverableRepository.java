package com.missoft.pms.repository;

import com.missoft.pms.entity.AfterServiceDeliverable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AfterServiceDeliverableRepository extends JpaRepository<AfterServiceDeliverable, Long> {
    List<AfterServiceDeliverable> findByProjectIdAndEventId(Long projectId, Long eventId);
    boolean existsByEventId(Long eventId);
}

