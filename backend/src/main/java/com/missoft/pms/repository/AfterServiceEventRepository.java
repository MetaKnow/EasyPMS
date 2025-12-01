package com.missoft.pms.repository;

import com.missoft.pms.entity.AfterServiceEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AfterServiceEventRepository extends JpaRepository<AfterServiceEvent, Long> {
    Page<AfterServiceEvent> findByAfterServiceProjectId(Long afterServiceProjectId, Pageable pageable);
    List<AfterServiceEvent> findByAfterServiceProjectId(Long afterServiceProjectId);
}

