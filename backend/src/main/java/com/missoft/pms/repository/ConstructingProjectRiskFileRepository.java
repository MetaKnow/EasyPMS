package com.missoft.pms.repository;

import com.missoft.pms.entity.ConstructingProjectRiskFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 类级注释：项目风险附件数据仓库
 */
@Repository
public interface ConstructingProjectRiskFileRepository extends JpaRepository<ConstructingProjectRiskFile, Long> {
    /**
     * 函数级注释：按风险ID查询附件列表
     */
    List<ConstructingProjectRiskFile> findByRiskId(Long riskId);
    /**
     * 函数级注释：判断风险是否存在附件
     */
    boolean existsByRiskId(Long riskId);
    /**
     * 函数级注释：按风险ID删除附件记录
     */
    void deleteByRiskId(Long riskId);
}
