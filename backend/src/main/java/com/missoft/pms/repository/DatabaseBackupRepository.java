package com.missoft.pms.repository;

import com.missoft.pms.entity.DatabaseBackup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DatabaseBackupRepository extends JpaRepository<DatabaseBackup, Long> {
    
    // 查询最近的成功备份记录，按时间倒序
    List<DatabaseBackup> findByBackupStateOrderByBackupDateDesc(String backupState);
    
    // 查询所有备份记录，按时间倒序
    List<DatabaseBackup> findAllByOrderByBackupDateDesc();
}
