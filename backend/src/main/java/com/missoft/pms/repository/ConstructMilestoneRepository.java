package com.missoft.pms.repository;

import com.missoft.pms.entity.ConstructMilestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 项目里程碑仓库
 */
@Repository
public interface ConstructMilestoneRepository extends JpaRepository<ConstructMilestone, Long> {

    List<ConstructMilestone> findByProjectId(Long projectId);

    boolean existsByProjectIdAndMilestoneName(Long projectId, String milestoneName);

    /**
     * 按项目与里程碑名称删除记录（函数级注释：用于编辑时移除接口开发里程碑）
     */
    void deleteByProjectIdAndMilestoneName(Long projectId, String milestoneName);

    @Query("SELECT cm FROM ConstructMilestone cm WHERE cm.projectId = :projectId AND cm.milestoneName IN :names")
    List<ConstructMilestone> findByProjectIdAndMilestoneNames(@Param("projectId") Long projectId, @Param("names") List<String> names);
}