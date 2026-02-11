package com.missoft.pms.repository;

import com.missoft.pms.entity.CustomerFollowUp;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerFollowUpRepository extends JpaRepository<CustomerFollowUp, Long> {
    Page<CustomerFollowUp> findByAfterServiceProjectId(Long afterServiceProjectId, Pageable pageable);
    List<CustomerFollowUp> findByAfterServiceProjectId(Long afterServiceProjectId);
}
