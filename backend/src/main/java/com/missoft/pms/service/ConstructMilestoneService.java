package com.missoft.pms.service;

import com.missoft.pms.entity.ArchieveSoft;
import com.missoft.pms.entity.ConstructMilestone;
import com.missoft.pms.entity.ConstructingProject;
import com.missoft.pms.entity.StandardConstructStep;
import com.missoft.pms.entity.StandardMilestone;
import com.missoft.pms.repository.ArchieveSoftRepository;
import com.missoft.pms.repository.ConstructMilestoneRepository;
import com.missoft.pms.repository.StandardConstructStepRepository;
import com.missoft.pms.repository.StandardMilestoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 项目里程碑服务
 */
@Service
@Transactional
public class ConstructMilestoneService {

    @Autowired
    private ConstructMilestoneRepository constructMilestoneRepository;

    @Autowired
    private StandardConstructStepRepository standardConstructStepRepository;

    @Autowired
    private StandardMilestoneRepository standardMilestoneRepository;

    @Autowired
    private ArchieveSoftRepository archieveSoftRepository;

    /**
     * 根据项目的产品（systemName）生成项目里程碑数据。
     * 规则：从 standard_construct_step 表中取该产品对应的所有步骤的 smilestoneId，
     * 映射到 standard_milestone 的名称，按名称去重后在 construct_milestone 中创建记录。
     *
     * @param project 在建项目
     * @return 生成的里程碑数量
     */
    public int generateMilestonesForProject(ConstructingProject project) {
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

        List<StandardConstructStep> steps;
        if (systemName != null && !systemName.isEmpty()) {
            steps = standardConstructStepRepository.findBySystemNameOrderBySstepNameAsc(systemName);
        } else {
            steps = standardConstructStepRepository.findAllByOrderBySstepNameAsc();
        }
        if (steps.isEmpty()) {
            return 0;
        }

        // 收集步骤对应的标准里程碑ID
        Set<Long> milestoneIds = steps.stream()
                .map(StandardConstructStep::getSmilestoneId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (milestoneIds.isEmpty()) {
            return 0;
        }

        // 查询标准里程碑名称
        List<String> milestoneNames = milestoneIds.stream()
                .map(id -> standardMilestoneRepository.findById(id).orElse(null))
                .filter(Objects::nonNull)
                .map(StandardMilestone::getMilestoneName)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .distinct()
                .collect(Collectors.toList());

        if (milestoneNames.isEmpty()) {
            return 0;
        }

        // 去除已存在的里程碑，避免重复生成
        List<ConstructMilestone> existing = constructMilestoneRepository.findByProjectIdAndMilestoneNames(project.getProjectId(), milestoneNames);
        Set<String> existingNames = existing.stream().map(ConstructMilestone::getMilestoneName).collect(Collectors.toSet());

        List<ConstructMilestone> toCreate = new ArrayList<>();
        for (String name : milestoneNames) {
            if (!existingNames.contains(name)) {
                ConstructMilestone cm = new ConstructMilestone();
                cm.setProjectId(project.getProjectId());
                cm.setMilestoneName(name);
                // 初始状态：未完成，工期未知
                cm.setIscomplete(false);
                cm.setMilestonePeriod(null);
                toCreate.add(cm);
            }
        }

        if (toCreate.isEmpty()) {
            return 0;
        }
        constructMilestoneRepository.saveAll(toCreate);
        return toCreate.size();
    }

    @Transactional(readOnly = true)
    public List<ConstructMilestone> getMilestonesByProjectId(Long projectId) {
        if (projectId == null) return List.of();
        return constructMilestoneRepository.findByProjectId(projectId);
    }
}