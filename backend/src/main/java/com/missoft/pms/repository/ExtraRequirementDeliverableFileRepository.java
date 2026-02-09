package com.missoft.pms.repository;

import com.missoft.pms.entity.ExtraRequirementDeliverableFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 类级注释：
 * 合同外需求附件仓库接口，提供附件查询能力。
 */
@Repository
public interface ExtraRequirementDeliverableFileRepository extends JpaRepository<ExtraRequirementDeliverableFile, Long> {

    /**
     * 函数级注释：按合同外需求ID查询附件列表
     * @param requirementId 需求ID
     * @return 附件列表
     */
    List<ExtraRequirementDeliverableFile> findByRequirementId(Long requirementId);

    /**
     * 函数级注释：删除某个需求下的全部附件
     * @param requirementId 需求ID
     */
    void deleteByRequirementId(Long requirementId);
}
