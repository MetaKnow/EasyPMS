package com.missoft.pms.service;

import com.missoft.pms.entity.ArchieveSoft;
import com.missoft.pms.entity.ConstructMilestone;
import com.missoft.pms.entity.ConstructingProject;
import com.missoft.pms.entity.StandardConstructStep;
import com.missoft.pms.entity.StandardMilestone;
import com.missoft.pms.entity.ProjectSstepRelation;
import com.missoft.pms.repository.ArchieveSoftRepository;
import com.missoft.pms.repository.ConstructMilestoneRepository;
import com.missoft.pms.repository.StandardConstructStepRepository;
import com.missoft.pms.repository.StandardMilestoneRepository;
import com.missoft.pms.repository.ProjectSstepRelationRepository;
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

    @Autowired
    private ProjectSstepRelationRepository projectSstepRelationRepository;

    /**
     * 函数级注释：根据“已生成的项目步骤关系”同步项目里程碑集合。
     * 目标：保证 construct_milestone 表仅保留与当前项目存在步骤的里程碑；
     * 同时在勾选了“接口开发/个性化功能开发”时，保留对应入口里程碑（05/06），即使暂未生成关联步骤。
     *
     * 规则：
     * - 从 project_sstep_relations 读取当前项目的关系列表；
     *   · 对于标准步骤（sstepId 非空），通过 standard_construct_step -> standard_milestone 映射得到里程碑名称；
     *   · 对于非标准步骤或关系中已回填 milestoneId 的情况，优先使用关系中的 milestoneId -> construct_milestone 名称；
     * - 根据项目建设内容 constructContent，若包含“接口开发”，强制保留“05完成接口开发集成”；
     *   若包含“个性化功能开发”，强制保留“06完成个性化功能开发”。
     * - 删除项目下其余不在保留集合中的里程碑；对缺失但应保留的名称进行补充创建（初始 iscomplete=false, period=null）。
     *
     * @param project 当前项目实体（需要 projectId 与 constructContent）
     * @return 变更数量（新增+删除）
     */
    public int syncMilestonesWithProjectSteps(ConstructingProject project) {
        if (project == null || project.getProjectId() == null) {
            return 0;
        }

        final Long projectId = project.getProjectId();
        final String constructContent = project.getConstructContent();

        // 1) 读取项目已有的步骤关系
        List<ProjectSstepRelation> relations = projectSstepRelationRepository.findByProjectId(projectId);

        // 2) 计算应保留的里程碑名称集合
        Set<String> keepNames = new LinkedHashSet<>();

        if (relations != null) {
            for (ProjectSstepRelation rel : relations) {
                if (rel == null) continue;
                String name = null;
                // 优先使用关系中的 milestoneId（如果存在）：通过 construct_milestone 获取名称
                if (rel.getMilestoneId() != null) {
                    ConstructMilestone cm = constructMilestoneRepository.findById(rel.getMilestoneId()).orElse(null);
                    if (cm != null && cm.getMilestoneName() != null) {
                        name = cm.getMilestoneName().trim();
                    }
                }
                // 其次使用标准步骤的 smilestoneId -> 名称映射
                if (name == null && rel.getSstepId() != null) {
                    StandardConstructStep step = standardConstructStepRepository.findById(rel.getSstepId()).orElse(null);
                    if (step != null && step.getSmilestoneId() != null) {
                        StandardMilestone sm = standardMilestoneRepository.findById(step.getSmilestoneId()).orElse(null);
                        if (sm != null && sm.getMilestoneName() != null) {
                            name = sm.getMilestoneName().trim();
                        }
                    }
                }
                if (name != null && !name.isEmpty()) {
                    keepNames.add(name);
                }
            }
        }

        // 入口里程碑保留：接口开发/个性化功能开发
        boolean includeInterfaceDev = constructContent != null && constructContent.contains("接口开发");
        boolean includePersonalDev = constructContent != null && constructContent.contains("个性化功能开发");
        if (includeInterfaceDev) {
            keepNames.add("05完成接口开发集成");
        }
        if (includePersonalDev) {
            keepNames.add("06完成个性化功能开发");
        }

        // 3) 读取项目现有里程碑并对比，确定增删集
        List<ConstructMilestone> existing = constructMilestoneRepository.findByProjectId(projectId);
        Set<String> existingNames = existing.stream()
                .filter(m -> m != null && m.getMilestoneName() != null)
                .map(m -> m.getMilestoneName().trim())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        int changes = 0;

        // 删除多余的里程碑
        for (String name : existingNames) {
            if (!keepNames.contains(name)) {
                constructMilestoneRepository.deleteByProjectIdAndMilestoneName(projectId, name);
                changes++;
            }
        }

        // 补充缺失的里程碑
        for (String name : keepNames) {
            if (!existingNames.contains(name)) {
                ConstructMilestone cm = new ConstructMilestone();
                cm.setProjectId(projectId);
                cm.setMilestoneName(name);
                cm.setIscomplete(false);
                cm.setMilestonePeriod(null);
                constructMilestoneRepository.save(cm);
                changes++;
            }
        }

        return changes;
    }

    /**
     * 根据项目的产品（systemName）生成项目里程碑数据。
     * 规则：从 standard_construct_step 表中取该产品对应的所有步骤的 smilestoneId，
     * 映射到 standard_milestone 的名称，按名称去重后在 construct_milestone 中创建记录。
     * 同时，当项目建设内容未勾选“接口开发”时，过滤掉名称为“05完成接口开发集成”的里程碑。
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

        // 当未勾选“接口开发”建设内容时，过滤掉接口相关里程碑
        boolean includeInterfaceDev = project.getConstructContent() != null && project.getConstructContent().contains("接口开发");
        if (!includeInterfaceDev) {
            milestoneNames = milestoneNames.stream()
                    .filter(name -> !"05完成接口开发集成".equals(name))
                    .collect(Collectors.toList());
            if (milestoneNames.isEmpty()) {
                return 0;
            }
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

    /**
     * 在项目编辑时根据“接口开发”建设内容的勾选状态，增删接口开发里程碑。
     * 当未勾选“接口开发”时，删除项目下名称为“05完成接口开发集成”的里程碑；
     * 当勾选“接口开发”且该里程碑不存在时，补充创建（初始未完成、工期为空）。
     *
     * @param projectId        项目ID
     * @param constructContent 项目建设内容（按/分隔）
     * @return 变更的里程碑数量（删除或新增的数量）
     */
    public int adjustMilestonesForInterfaceDev(Long projectId, String constructContent) {
        if (projectId == null) return 0;
        boolean includeInterfaceDev = constructContent != null && constructContent.contains("接口开发");
        String targetName = "05完成接口开发集成";
        int changes = 0;

        if (!includeInterfaceDev) {
            // 未勾选接口开发：删除对应里程碑
            if (constructMilestoneRepository.existsByProjectIdAndMilestoneName(projectId, targetName)) {
                constructMilestoneRepository.deleteByProjectIdAndMilestoneName(projectId, targetName);
                changes++;
            }
        } else {
            // 勾选接口开发：若不存在则补充创建
            if (!constructMilestoneRepository.existsByProjectIdAndMilestoneName(projectId, targetName)) {
                ConstructMilestone cm = new ConstructMilestone();
                cm.setProjectId(projectId);
                cm.setMilestoneName(targetName);
                cm.setIscomplete(false);
                cm.setMilestonePeriod(null);
                constructMilestoneRepository.save(cm);
                changes++;
            }
        }

        return changes;
    }

    /**
     * 函数级注释：在项目编辑时根据“个性化功能开发”勾选状态，增删对应里程碑。
     * 当未勾选“个性化功能开发”时，删除项目下名称为“06完成个性化功能开发”的里程碑；
     * 当勾选且该里程碑不存在时，补充创建（初始未完成、工期为空）。
     *
     * @param projectId        项目ID
     * @param constructContent 项目建设内容（按/分隔）
     * @return 变更的里程碑数量（删除或新增的数量）
     */
    public int adjustMilestonesForPersonalDev(Long projectId, String constructContent) {
        if (projectId == null) return 0;
        boolean includePersonalDev = constructContent != null && constructContent.contains("个性化功能开发");
        String targetName = "06完成个性化功能开发";
        int changes = 0;

        if (!includePersonalDev) {
            // 未勾选个性化功能开发：删除对应里程碑
            if (constructMilestoneRepository.existsByProjectIdAndMilestoneName(projectId, targetName)) {
                constructMilestoneRepository.deleteByProjectIdAndMilestoneName(projectId, targetName);
                changes++;
            }
        } else {
            // 勾选个性化功能开发：若不存在则补充创建
            if (!constructMilestoneRepository.existsByProjectIdAndMilestoneName(projectId, targetName)) {
                ConstructMilestone cm = new ConstructMilestone();
                cm.setProjectId(projectId);
                cm.setMilestoneName(targetName);
                cm.setIscomplete(false);
                cm.setMilestonePeriod(null);
                constructMilestoneRepository.save(cm);
                changes++;
            }
        }
        return changes;
    }
    @Transactional(readOnly = true)
    public List<ConstructMilestone> getMilestonesByProjectId(Long projectId) {
        if (projectId == null) return List.of();
        return constructMilestoneRepository.findByProjectId(projectId);
    }

    /**
     * 根据项目的步骤实际工期汇总，写回项目里程碑的 milestonePeriod。
     * 规则：按标准步骤的 smilestoneId 找到标准里程碑名称，按名称分组求和 actualPeriod。
     * 对于没有有效实际工期的里程碑写回 null，避免保留过期值。
     *
     * @param projectId 项目ID
     * @return 被更新的里程碑数量
     */
    public int updateMilestonePeriodsForProject(Long projectId) {
        if (projectId == null) return 0;

        // 读取项目步骤关系
        List<ProjectSstepRelation> relations = projectSstepRelationRepository.findByProjectId(projectId);
        if (relations == null || relations.isEmpty()) {
            // 没有关系时，将所有项目里程碑的工期置空
            List<ConstructMilestone> milestones = constructMilestoneRepository.findByProjectId(projectId);
            for (ConstructMilestone m : milestones) {
                m.setMilestonePeriod(null);
  }

  
            if (!milestones.isEmpty()) {
                constructMilestoneRepository.saveAll(milestones);
            }
            return milestones.size();
        }

        // 计算：标准里程碑名称 -> 实际工期求和
        Map<String, Integer> sumByMilestoneName = new HashMap<>();
        for (ProjectSstepRelation rel : relations) {
            if (rel == null || rel.getSstepId() == null) continue;
            Integer ap = rel.getActualPeriod();
            if (ap == null) continue; // 仅汇总有效的实际工期

            StandardConstructStep step = standardConstructStepRepository.findById(rel.getSstepId()).orElse(null);
            if (step == null || step.getSmilestoneId() == null) continue;

            StandardMilestone sm = standardMilestoneRepository.findById(step.getSmilestoneId()).orElse(null);
            if (sm == null || sm.getMilestoneName() == null) continue;
            String name = sm.getMilestoneName().trim();
            if (name.isEmpty()) continue;

            sumByMilestoneName.merge(name, ap, Integer::sum);
        }

        // 写回构建里程碑表
        List<ConstructMilestone> milestones = constructMilestoneRepository.findByProjectId(projectId);
        int updated = 0;
        for (ConstructMilestone m : milestones) {
            if (m == null || m.getMilestoneName() == null) continue;
            String name = m.getMilestoneName().trim();
            Integer sum = sumByMilestoneName.get(name);
            m.setMilestonePeriod(sum); // 若无汇总值则写回null
            updated++;
        }
        if (!milestones.isEmpty()) {
            constructMilestoneRepository.saveAll(milestones);
        }
        return updated;
    }

    /**
     * 函数级注释：根据项目步骤完成状态更新里程碑完成标记与完成日期。
     * @param projectId 项目ID
     * @return 被更新的里程碑数量
     */
    public int updateMilestoneCompletionForProject(Long projectId) {
        if (projectId == null) return 0;

        List<ProjectSstepRelation> relations = projectSstepRelationRepository.findByProjectId(projectId);
        Map<String, List<ProjectSstepRelation>> relByMilestoneName = new HashMap<>();
        if (relations != null) {
            for (ProjectSstepRelation rel : relations) {
                if (rel == null) continue;
                String name = null;
                if (rel.getMilestoneId() != null) {
                    ConstructMilestone cm = constructMilestoneRepository.findById(rel.getMilestoneId()).orElse(null);
                    if (cm != null && cm.getMilestoneName() != null) {
                        name = cm.getMilestoneName().trim();
                    }
                }
                if (name == null && rel.getSstepId() != null) {
                    StandardConstructStep step = standardConstructStepRepository.findById(rel.getSstepId()).orElse(null);
                    if (step != null && step.getSmilestoneId() != null) {
                        StandardMilestone sm = standardMilestoneRepository.findById(step.getSmilestoneId()).orElse(null);
                        if (sm != null && sm.getMilestoneName() != null) {
                            name = sm.getMilestoneName().trim();
                        }
                    }
                }
                if (name != null && !name.isEmpty()) {
                    relByMilestoneName.computeIfAbsent(name, k -> new ArrayList<>()).add(rel);
                }
            }
        }

        List<ConstructMilestone> milestones = constructMilestoneRepository.findByProjectId(projectId);
        int updated = 0;
        for (ConstructMilestone m : milestones) {
            if (m == null || m.getMilestoneName() == null) continue;
            String name = m.getMilestoneName().trim();
            List<ProjectSstepRelation> list = relByMilestoneName.get(name);
            boolean complete = list != null && !list.isEmpty() && list.stream().allMatch(r -> "已完成".equals(r.getStepStatus()));
            m.setIscomplete(complete);
            if (complete) {
                java.time.LocalDate maxEnd = list.stream()
                        .map(ProjectSstepRelation::getActualEndDate)
                        .filter(java.util.Objects::nonNull)
                        .max(java.util.Comparator.naturalOrder())
                        .orElse(null);
                m.setCompleteDate(maxEnd);
            } else {
                m.setCompleteDate(null);
            }
            updated++;
        }
        if (!milestones.isEmpty()) {
            constructMilestoneRepository.saveAll(milestones);
        }
        return updated;
    }
}
