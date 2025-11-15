package com.missoft.pms.repository;

import com.missoft.pms.entity.ConstructDeliverableFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 项目交付物文件仓库接口
 */
@Repository
public interface ConstructDeliverableFileRepository extends JpaRepository<ConstructDeliverableFile, Long> {

    /**
     * 根据项目ID和交付物ID查询文件列表
     */
    List<ConstructDeliverableFile> findByProjectIdAndDeliverableId(Long projectId, Long deliverableId);

    /**
     * 根据项目ID、交付物ID与项目步骤关系ID查询文件列表
     * 用于“上传交付物”界面按当前步骤（relationId）加载已上传文件。
     * @param projectId 项目ID
     * @param deliverableId 交付物ID
     * @param projectStepId 项目-标准步骤关系ID（project_sstep_relations.relationId）
     * @return 文件记录列表
     */
    List<ConstructDeliverableFile> findByProjectIdAndDeliverableIdAndProjectStepId(Long projectId, Long deliverableId, Long projectStepId);

    /**
     * 根据项目ID查询文件列表
     */
    List<ConstructDeliverableFile> findByProjectId(Long projectId);

    /**
     * 根据项目ID与项目步骤关系ID查询文件列表
     * @param projectId 项目ID
     * @param projectStepId 项目-标准步骤关系ID（project_sstep_relations.relationId）
     * @return 文件记录列表
     */
    List<ConstructDeliverableFile> findByProjectIdAndProjectStepId(Long projectId, Long projectStepId);

    /**
     * 根据项目ID与项目里程碑ID查询文件列表
     * 注意：此里程碑ID为项目里程碑（construct_milestone.milestoneId），非标准里程碑ID。
     * @param projectId 项目ID
     * @param milestoneId 项目里程碑ID
     * @return 文件记录列表
     */
    List<ConstructDeliverableFile> findByProjectIdAndMilestoneId(Long projectId, Long milestoneId);
}