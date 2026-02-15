package com.missoft.pms.repository;

import com.missoft.pms.entity.ConstructingProjectRisk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 类级注释：项目风险数据仓库
 */
@Repository
public interface ConstructingProjectRiskRepository extends JpaRepository<ConstructingProjectRisk, Long> {
    /**
     * 函数级注释：按项目ID查询风险列表
     */
    List<ConstructingProjectRisk> findByProjectId(Long projectId);
}
