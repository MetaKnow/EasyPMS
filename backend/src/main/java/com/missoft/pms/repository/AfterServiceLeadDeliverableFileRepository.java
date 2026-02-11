package com.missoft.pms.repository;

import com.missoft.pms.entity.AfterServiceLeadDeliverableFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AfterServiceLeadDeliverableFileRepository extends JpaRepository<AfterServiceLeadDeliverableFile, Long> {
    List<AfterServiceLeadDeliverableFile> findByLeadsId(Long leadsId);
    boolean existsByLeadsId(Long leadsId);
}
