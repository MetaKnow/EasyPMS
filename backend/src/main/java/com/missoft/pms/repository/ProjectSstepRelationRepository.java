package com.missoft.pms.repository;

import com.missoft.pms.entity.ProjectSstepRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 项目-标准步骤关系仓库接口
 */
@Repository
public interface ProjectSstepRelationRepository extends JpaRepository<ProjectSstepRelation, Long> {

    List<ProjectSstepRelation> findByProjectId(Long projectId);

    List<ProjectSstepRelation> findByProjectIdAndSstepId(Long projectId, Long sstepId);

    /**
     * 按项目与接口ID查询已生成的关系，用于避免重复生成接口相关步骤。
     * @param projectId 项目ID
     * @param interfaceId 接口ID
     * @return 该接口对应的关系列表
     */
    List<ProjectSstepRelation> findByProjectIdAndInterfaceId(Long projectId, Long interfaceId);

    /**
     * 按项目与个性化开发ID查询已生成的关系，用于避免重复生成个性化开发相关步骤。
     * @param projectId 项目ID
     * @param personalDevId 个性化开发ID
     * @return 该个性化开发对应的关系列表
     */
    List<ProjectSstepRelation> findByProjectIdAndPersonalDevId(Long projectId, Long personalDevId);
}