package com.missoft.pms.repository;

import com.missoft.pms.entity.ConstructingProjectWeeklyReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 类级注释：项目周报数据仓库
 */
@Repository
public interface ConstructingProjectWeeklyReportRepository extends JpaRepository<ConstructingProjectWeeklyReport, Long> {
    /**
     * 函数级注释：按项目ID查询周报列表
     */
    List<ConstructingProjectWeeklyReport> findByProjectId(Long projectId);
}
