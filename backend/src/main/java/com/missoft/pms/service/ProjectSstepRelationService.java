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
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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
        // 解析项目建设内容（多选，按/分隔），仅生成勾选类型对应的步骤
        String constructContent = project.getConstructContent();
        if (!StringUtils.hasText(systemName) || !StringUtils.hasText(constructContent)) {
            // 必须同时有产品(systemName)与建设内容类型，才生成步骤
            return 0;
        }

        List<String> rawTypes = Arrays.asList(constructContent.split("/"));
        List<String> types = new ArrayList<>();
        for (String t : rawTypes) {
            if (t != null) {
                String tt = t.trim();
                if (!tt.isEmpty()) {
                    types.add(tt);
                }
            }
        }
        if (types.isEmpty()) {
            return 0;
        }

        // 查询已存在的关系，避免重复插入
        List<ProjectSstepRelation> existing = projectSstepRelationRepository.findByProjectId(project.getProjectId());
        Set<Long> existingSstepIds = new HashSet<>();
        for (ProjectSstepRelation r : existing) {
            if (r.getSstepId() != null) {
                existingSstepIds.add(r.getSstepId());
            }
        }

        // 按 systemName + type 获取标准步骤合集
        Set<Long> toCreateSstepIds = new HashSet<>();
        List<ProjectSstepRelation> toCreate = new ArrayList<>();
        for (String type : types) {
            List<StandardConstructStep> stepsByType = standardConstructStepRepository
                    .findBySystemNameAndTypeOrderBySstepNameAsc(systemName, type);
            for (StandardConstructStep step : stepsByType) {
                Long sid = step.getSstepId();
                if (sid == null) continue;
                if (existingSstepIds.contains(sid)) continue; // 已有关系跳过
                if (toCreateSstepIds.contains(sid)) continue; // 避免同类型间重复
                ProjectSstepRelation rel = new ProjectSstepRelation();
                rel.setProjectId(project.getProjectId());
                rel.setSstepId(sid);
                // 初始设置负责人为项目负责人（如有）
                rel.setDirector(project.getProjectLeader());
                toCreate.add(rel);
                toCreateSstepIds.add(sid);
            }
        }

        if (toCreate.isEmpty()) {
            return 0;
        }

        projectSstepRelationRepository.saveAll(toCreate);
        return toCreate.size();
    }

    /**
     * 在项目编辑时，根据建设内容的增减，按产品(systemName)增删对应的步骤关系。
     * 仅对新增/减少的类型对应的步骤进行处理，不影响其他步骤。
     *
     * @param projectId            项目ID
     * @param oldConstructContent  编辑前的项目建设内容（按/分隔）
     * @param newConstructContent  编辑后的项目建设内容（按/分隔）
     * @param softId               当前绑定产品ID（用于获取systemName）
     * @param projectLeader        当前项目负责人（用于初始化新增关系的负责人）
     * @return 变更的关系数量（新增 + 删除）
     */
    public int adjustRelationsForProjectOnEdit(Long projectId,
                                               String oldConstructContent,
                                               String newConstructContent,
                                               Long softId,
                                               Long projectLeader) {
        if (projectId == null) {
            return 0;
        }

        String systemName = null;
        if (softId != null) {
            ArchieveSoft soft = archieveSoftRepository.findById(softId).orElse(null);
            if (soft != null && soft.getSoftName() != null) {
                systemName = soft.getSoftName().trim();
            }
        }
        if (!StringUtils.hasText(systemName)) {
            return 0;
        }

        // 解析类型集合
        Set<String> oldTypes = parseTypes(oldConstructContent);
        Set<String> newTypes = parseTypes(newConstructContent);
        Set<String> addedTypes = new HashSet<>(newTypes);
        addedTypes.removeAll(oldTypes);
        Set<String> removedTypes = new HashSet<>(oldTypes);
        removedTypes.removeAll(newTypes);

        // 查询现有关系，构建 sstepId -> relation 映射
        List<ProjectSstepRelation> existing = projectSstepRelationRepository.findByProjectId(projectId);
        Set<Long> existingSstepIds = existing.stream()
                .map(ProjectSstepRelation::getSstepId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        int changes = 0;

        // 处理新增类型：为该产品 + 类型的标准步骤补充缺失关系
        if (!addedTypes.isEmpty()) {
            List<ProjectSstepRelation> toCreate = new ArrayList<>();
            for (String type : addedTypes) {
                List<StandardConstructStep> steps = standardConstructStepRepository
                        .findBySystemNameAndTypeOrderBySstepNameAsc(systemName, type);
                for (StandardConstructStep step : steps) {
                    Long sid = step.getSstepId();
                    if (sid == null) continue;
                    if (existingSstepIds.contains(sid)) continue;
                    ProjectSstepRelation rel = new ProjectSstepRelation();
                    rel.setProjectId(projectId);
                    rel.setSstepId(sid);
                    rel.setDirector(projectLeader);
                    toCreate.add(rel);
                    existingSstepIds.add(sid); // 立即纳入集合，避免重复
                }
            }
            if (!toCreate.isEmpty()) {
                projectSstepRelationRepository.saveAll(toCreate);
                changes += toCreate.size();
            }
        }

        // 处理减少类型：删除该产品 + 类型对应标准步骤的关系（仅删除匹配的，不影响其他）
        if (!removedTypes.isEmpty()) {
            Set<Long> toRemoveSstepIds = new HashSet<>();
            for (String type : removedTypes) {
                List<StandardConstructStep> steps = standardConstructStepRepository
                        .findBySystemNameAndTypeOrderBySstepNameAsc(systemName, type);
                for (StandardConstructStep step : steps) {
                    if (step.getSstepId() != null) {
                        toRemoveSstepIds.add(step.getSstepId());
                    }
                }
            }
            if (!toRemoveSstepIds.isEmpty()) {
                // 找出需要删除的关系记录
                List<ProjectSstepRelation> toDelete = new ArrayList<>();
                for (ProjectSstepRelation rel : existing) {
                    Long sid = rel.getSstepId();
                    if (sid != null && toRemoveSstepIds.contains(sid)) {
                        toDelete.add(rel);
                    }
                }
                if (!toDelete.isEmpty()) {
                    projectSstepRelationRepository.deleteAll(toDelete);
                    changes += toDelete.size();
                }
            }
        }

        return changes;
    }

    private Set<String> parseTypes(String constructContent) {
        Set<String> set = new HashSet<>();
        if (!StringUtils.hasText(constructContent)) {
            return set;
        }
        List<String> rawTypes = Arrays.asList(constructContent.split("/"));
        for (String t : rawTypes) {
            if (t != null) {
                String tt = t.trim();
                if (!tt.isEmpty()) {
                    set.add(tt);
                }
            }
        }
        return set;
    }

    @Transactional(readOnly = true)
    public List<ProjectSstepRelation> getRelationsByProjectId(Long projectId) {
        if (projectId == null) {
            return List.of();
        }
        return projectSstepRelationRepository.findByProjectId(projectId);
    }
}