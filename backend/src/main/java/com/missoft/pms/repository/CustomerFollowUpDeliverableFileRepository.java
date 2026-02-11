package com.missoft.pms.repository;

import com.missoft.pms.entity.CustomerFollowUpDeliverableFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerFollowUpDeliverableFileRepository extends JpaRepository<CustomerFollowUpDeliverableFile, Long> {
    List<CustomerFollowUpDeliverableFile> findByFollowUpRecordId(Long followUpRecordId);
    boolean existsByFollowUpRecordId(Long followUpRecordId);
}
