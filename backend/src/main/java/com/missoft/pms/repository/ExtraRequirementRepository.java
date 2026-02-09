package com.missoft.pms.repository;

import com.missoft.pms.entity.ExtraRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 类级注释：
 * 合同外需求仓库接口，提供 `extra_requirement` 表的基础CRUD操作。
 */
@Repository
public interface ExtraRequirementRepository extends JpaRepository<ExtraRequirement, Long> {

    /**
     * 函数级注释：按项目ID查询合同外需求列表
     * @param projectId 项目ID
     * @return 合同外需求列表
     */
    List<ExtraRequirement> findByProjectId(Long projectId);
}
