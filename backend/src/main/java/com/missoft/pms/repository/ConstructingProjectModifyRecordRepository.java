package com.missoft.pms.repository;

import com.missoft.pms.entity.ConstructingProjectModifyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ConstructingProjectModifyRecordRepository extends JpaRepository<ConstructingProjectModifyRecord, Long> {
    List<ConstructingProjectModifyRecord> findByProjectIdOrderByRecordIdDesc(Long projectId);
}
