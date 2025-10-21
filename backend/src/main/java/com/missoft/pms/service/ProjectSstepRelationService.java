package com.missoft.pms.service;

import com.missoft.pms.entity.ArchieveSoft;
import com.missoft.pms.entity.ConstructingProject;
import com.missoft.pms.entity.ProjectSstepRelation;
import com.missoft.pms.entity.StandardConstructStep;
import com.missoft.pms.repository.ArchieveSoftRepository;
import com.missoft.pms.repository.ProjectSstepRelationRepository;
import com.missoft.pms.repository.StandardConstructStepRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 项目-标准步骤关系服务
 */
@Service
@Transactional
public class ProjectSstepRelationService {

    @Autowired
    private ProjectSstepRelationRepository projectSstepRelationRepository;

    @Autowired
    private StandardConstructStepRepository standardConstructStepRepository;

    @Autowired
    private ArchieveSoftRepository archieveSoftRepository;

    /**
     * 为项目生成与产品相关的标准步骤关系（按产品名称匹配standard_construct_step.systemName）
     *
     * @param project 在建项目
     * @return 生成的关系数量
     */
    public int generateRelationsForProject(ConstructingProject project) {
        if (project == null || project.getProjectId() == null) {
            return 0;
        }

        String systemName = null;
        if (project.getSoftId() != null) {
            ArchieveSoft soft = archieveSoftRepository.findById(project.getSoftId()).orElse(null);
            if (soft != null && soft.getSoftName() != null) {
                systemName = soft.getSoftName().trim();
            }
        }

        List<StandardConstructStep> steps = new ArrayList<>();
        if (systemName != null && !systemName.isEmpty()) {
            steps = standardConstructStepRepository.findBySystemNameOrderBySstepNameAsc(systemName);
        } else {
            // 如果没有产品名称，则生成所有步骤的关系（可按需调整为不生成）
            steps = standardConstructStepRepository.findAllByOrderBySstepNameAsc();
        }

        if (steps.isEmpty()) {
            return 0;
        }

        List<ProjectSstepRelation> relations = new ArrayList<>();
        for (StandardConstructStep step : steps) {
            ProjectSstepRelation rel = new ProjectSstepRelation();
            rel.setProjectId(project.getProjectId());
            rel.setSstepId(step.getSstepId());
            // 初始设置负责人为项目负责人（如有）
            rel.setDirector(project.getProjectLeader());
            relations.add(rel);
        }

        projectSstepRelationRepository.saveAll(relations);
        return relations.size();
    }

    @Transactional(readOnly = true)
    public List<ProjectSstepRelation> getRelationsByProjectId(Long projectId) {
        if (projectId == null) {
            return List.of();
        }
        return projectSstepRelationRepository.findByProjectId(projectId);
    }
}