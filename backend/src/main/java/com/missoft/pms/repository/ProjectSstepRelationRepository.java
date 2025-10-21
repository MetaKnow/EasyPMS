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
}