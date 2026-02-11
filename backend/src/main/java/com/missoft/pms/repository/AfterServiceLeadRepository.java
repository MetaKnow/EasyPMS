package com.missoft.pms.repository;

import com.missoft.pms.entity.AfterServiceLead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AfterServiceLeadRepository extends JpaRepository<AfterServiceLead, Long> {
    Page<AfterServiceLead> findByAfterServiceProjectId(Long afterServiceProjectId, Pageable pageable);
    List<AfterServiceLead> findByAfterServiceProjectId(Long afterServiceProjectId);
}
