package com.missoft.pms.repository;

import com.missoft.pms.entity.ConstructingProjectWeeklyReportFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 类级注释：项目周报附件数据仓库
 */
@Repository
public interface ConstructingProjectWeeklyReportFileRepository extends JpaRepository<ConstructingProjectWeeklyReportFile, Long> {
    /**
     * 函数级注释：按周报ID查询附件列表
     */
    List<ConstructingProjectWeeklyReportFile> findByWeeklyReportId(Long weeklyReportId);
    /**
     * 函数级注释：判断周报是否存在附件
     */
    boolean existsByWeeklyReportId(Long weeklyReportId);
    /**
     * 函数级注释：按周报ID删除附件记录
     */
    void deleteByWeeklyReportId(Long weeklyReportId);
}
