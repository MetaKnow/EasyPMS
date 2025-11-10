package com.missoft.pms.service;

import com.missoft.pms.entity.ArchieveSoft;
import com.missoft.pms.entity.ConstructingProject;
import com.missoft.pms.entity.ProjectSstepRelation;
import com.missoft.pms.entity.StandardConstructStep;
import com.missoft.pms.entity.StandardMilestone;
import com.missoft.pms.entity.InterfaceEntity;
import com.missoft.pms.entity.PersonalDevelope;
import com.missoft.pms.entity.ConstructMilestone;
import com.missoft.pms.repository.ArchieveSoftRepository;
import com.missoft.pms.repository.ProjectSstepRelationRepository;
import com.missoft.pms.repository.StandardConstructStepRepository;
import com.missoft.pms.repository.StandardMilestoneRepository;
import com.missoft.pms.repository.ConstructingProjectRepository;
import com.missoft.pms.repository.ConstructMilestoneRepository;
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

    @Autowired
    private StandardMilestoneRepository standardMilestoneRepository;

    @Autowired
    private ConstructingProjectRepository constructingProjectRepository;

    @Autowired
    private ConstructMilestoneRepository constructMilestoneRepository;

    // 在自动生成关系时需要跳过的建设内容类型（type）
    private static final java.util.Set<String> SKIP_TYPES_FOR_AUTOGEN =
            new java.util.HashSet<>(java.util.Arrays.asList(
                    "接口开发",
                    "个性化功能开发"
            ));

    // 指定在新建项目时负责人需置空的步骤名称（去掉前缀编号后匹配）
    private static final java.util.Set<String> STEP_NAMES_WITH_EMPTY_DIRECTOR =
            new java.util.HashSet<>(java.util.Arrays.asList(
                    "接口开发",
                    "接口联调测试",
                    "个性化需求整体设计",
                    "个性化需求开发",
                    "个性化需求测试验证"
            ));

    /**
     * 规范化步骤名称用于匹配：去掉前缀的数字、空白与常见分隔符
     */
    private String normalizeStepName(String name) {
        if (name == null) return "";
        String result = name.trim();
        // 去掉前缀的数字、空白和常见分隔符（- _ . 、 / （ ） ： ;）
        result = result.replaceFirst("^[\\s\\d\\-_.、/（）()：:；;]+", "");
        return result;
    }

    private boolean shouldEmptyDirector(String sstepName) {
        String base = normalizeStepName(sstepName);
        return STEP_NAMES_WITH_EMPTY_DIRECTOR.contains(base);
    }

    /**
     * 根据标准步骤对应的标准里程碑名称，在项目里程碑表中定位并返回该项目里程碑ID。
     * 若项目里程碑尚未生成或无法匹配到名称，则返回 null。
     *
     * @param projectId 项目ID
     * @param step      标准步骤（需包含 smilestoneId）
     * @return 关联的项目里程碑ID，或 null
     */
    private Long resolveConstructMilestoneIdForStep(Long projectId, StandardConstructStep step) {
        if (projectId == null || step == null || step.getSmilestoneId() == null) {
            return null;
        }
        StandardMilestone sm = standardMilestoneRepository.findById(step.getSmilestoneId()).orElse(null);
        if (sm == null || sm.getMilestoneName() == null) {
            return null;
        }
        String name = sm.getMilestoneName().trim();
        if (name.isEmpty()) {
            return null;
        }
        List<ConstructMilestone> milestones = constructMilestoneRepository.findByProjectId(projectId);
        for (ConstructMilestone cm : milestones) {
            if (cm != null && cm.getMilestoneName() != null && name.equals(cm.getMilestoneName().trim())) {
                return cm.getMilestoneId();
            }
        }
        return null;
    }

    /**
     * 函数级注释：为指定项目的所有项目-步骤关系回填项目里程碑ID。
     * 适用场景：在首次生成关系或编辑后新增关系时，存在 milestoneId 为空但标准步骤已配置 smilestoneId 的情况。
     * 回填规则：
     * - 仅处理 milestoneId 为空且 sstepId 非空的关系；
     * - 通过 sstepId -> standard_construct_step -> standard_milestone 映射获取标准里程碑名称；
     * - 在 construct_milestone 表中按项目ID与名称查找对应记录，将其 milestoneId 写回关系；
     * 返回值为成功写回的数量。
     *
     * @param projectId 项目ID
     * @return 成功回填的关系数量
     */
    public int backfillMilestoneIdsForProject(Long projectId) {
        if (projectId == null) {
            return 0;
        }

        List<ProjectSstepRelation> relations = projectSstepRelationRepository.findByProjectId(projectId);
        if (relations == null || relations.isEmpty()) {
            return 0;
        }

        // 预加载项目下的里程碑并构建名称到ID的映射，避免重复查询
        List<ConstructMilestone> milestones = constructMilestoneRepository.findByProjectId(projectId);
        java.util.Map<String, Long> milestoneNameToId = new java.util.HashMap<>();
        for (ConstructMilestone cm : milestones) {
            if (cm != null && cm.getMilestoneName() != null && cm.getMilestoneId() != null) {
                milestoneNameToId.put(cm.getMilestoneName().trim(), cm.getMilestoneId());
            }
        }

        int updated = 0;
        List<ProjectSstepRelation> toSave = new java.util.ArrayList<>();
        for (ProjectSstepRelation rel : relations) {
            if (rel == null) continue;
            if (rel.getMilestoneId() != null) continue; // 已有里程碑ID跳过
            Long sstepId = rel.getSstepId();
            if (sstepId == null) continue; // 非标准步骤或缺失，跳过

            StandardConstructStep step = standardConstructStepRepository.findById(sstepId).orElse(null);
            if (step == null || step.getSmilestoneId() == null) continue; // 未配置标准里程碑，无法回填

            StandardMilestone sm = standardMilestoneRepository.findById(step.getSmilestoneId()).orElse(null);
            if (sm == null || sm.getMilestoneName() == null) continue;
            String name = sm.getMilestoneName().trim();
            if (name.isEmpty()) continue;

            Long cmId = milestoneNameToId.get(name);
            if (cmId != null) {
                rel.setMilestoneId(cmId);
                toSave.add(rel);
                updated++;
            }
        }

        if (!toSave.isEmpty()) {
            projectSstepRelationRepository.saveAll(toSave);
        }
        return updated;
    }

    /**
     * 当项目建设内容勾选了“接口开发”时，生成标准步骤“05业务系统接口需求调研”的项目-步骤关系。
     * 规则：
     * 1) 按项目绑定产品(systemName) + 类型("接口开发")过滤标准步骤；
     * 2) 以去编号后的名称匹配“业务系统接口需求调研”；
     * 3) 仅在该步骤关系不存在时创建，负责人默认设置为项目负责人；
     * 4) 若无法定位产品或未勾选“接口开发”，则不生成。
     *
     * @param projectId 项目ID
     * @return 生成的关系数量（0或1）
     */
    public int generateInterfaceDemandResearchForProject(Long projectId) {
        if (projectId == null) {
            return 0;
        }

        // 读取项目，校验勾选了“接口开发”并获取产品与负责人
        ConstructingProject project = constructingProjectRepository.findById(projectId).orElse(null);
        if (project == null) {
            return 0;
        }
        String constructContent = project.getConstructContent();
        boolean includeInterfaceDev = StringUtils.hasText(constructContent) && constructContent.contains("接口开发");
        if (!includeInterfaceDev) {
            return 0;
        }

        String systemName = null;
        if (project.getSoftId() != null) {
            ArchieveSoft soft = archieveSoftRepository.findById(project.getSoftId()).orElse(null);
            if (soft != null && StringUtils.hasText(soft.getSoftName())) {
                systemName = soft.getSoftName().trim();
            }
        }
        if (!StringUtils.hasText(systemName)) {
            return 0;
        }

        // 查询该产品下类型为“接口开发”的标准步骤，定位“业务系统接口需求调研”
        List<StandardConstructStep> steps = standardConstructStepRepository
                .findBySystemNameAndTypeOrderBySstepNameAsc(systemName, "接口开发");
        if (steps == null || steps.isEmpty()) {
            return 0;
        }
        StandardConstructStep target = null;
        for (StandardConstructStep s : steps) {
            String base = normalizeStepName(s.getSstepName());
            if ("业务系统接口需求调研".equals(base)) {
                target = s;
                break;
            }
        }
        if (target == null || target.getSstepId() == null) {
            return 0;
        }

        // 避免重复：若该项目已存在对应 sstepId 的关系，则跳过
        List<ProjectSstepRelation> existing = projectSstepRelationRepository.findByProjectId(projectId);
        for (ProjectSstepRelation r : existing) {
            if (r != null && r.getSstepId() != null && r.getSstepId().equals(target.getSstepId())) {
                return 0;
            }
        }

        // 创建关系，负责人遵循置空规则，其余默认项目负责人
        ProjectSstepRelation rel = new ProjectSstepRelation();
        rel.setProjectId(projectId);
        rel.setSstepId(target.getSstepId());
        // 回填所属项目里程碑ID
        rel.setMilestoneId(resolveConstructMilestoneIdForStep(projectId, target));
        if (shouldEmptyDirector(target.getSstepName())) {
            rel.setDirector(null);
        } else {
            rel.setDirector(project.getProjectLeader());
        }

        projectSstepRelationRepository.save(rel);
        return 1;
    }

    /**
     * 当项目建设内容勾选了“个性化功能开发”时，生成标准步骤“06个性化功能需求调研”的项目-步骤关系。
     * 规则：
     * 1) 按项目绑定产品(systemName) + 类型("个性化功能开发")过滤标准步骤；
     * 2) 以去编号后的名称匹配“个性化功能需求调研”；
     * 3) 仅在该步骤关系不存在时创建，负责人默认设置为项目负责人；
     * 4) 若无法定位产品或未勾选“个性化功能开发”，则不生成。
     *
     * @param projectId 项目ID
     * @return 生成的关系数量（0或1）
     */
    public int generatePersonalDemandResearchForProject(Long projectId) {
        if (projectId == null) {
            return 0;
        }

        // 读取项目，校验勾选了“个性化功能开发”并获取产品与负责人
        ConstructingProject project = constructingProjectRepository.findById(projectId).orElse(null);
        if (project == null) {
            return 0;
        }
        String constructContent = project.getConstructContent();
        boolean includePersonalDev = StringUtils.hasText(constructContent) && constructContent.contains("个性化功能开发");
        if (!includePersonalDev) {
            return 0;
        }

        String systemName = null;
        if (project.getSoftId() != null) {
            ArchieveSoft soft = archieveSoftRepository.findById(project.getSoftId()).orElse(null);
            if (soft != null && StringUtils.hasText(soft.getSoftName())) {
                systemName = soft.getSoftName().trim();
            }
        }
        if (!StringUtils.hasText(systemName)) {
            return 0;
        }

        // 查询该产品下类型为“个性化功能开发”的标准步骤，定位“个性化功能需求调研”
        List<StandardConstructStep> steps = standardConstructStepRepository
                .findBySystemNameAndTypeOrderBySstepNameAsc(systemName, "个性化功能开发");
        if (steps == null || steps.isEmpty()) {
            return 0;
        }
        StandardConstructStep target = null;
        for (StandardConstructStep s : steps) {
            String base = normalizeStepName(s.getSstepName());
            if ("个性化功能需求调研".equals(base)) {
                target = s;
                break;
            }
        }
        if (target == null || target.getSstepId() == null) {
            return 0;
        }

        // 避免重复：若该项目已存在对应 sstepId 的关系，则跳过
        List<ProjectSstepRelation> existing = projectSstepRelationRepository.findByProjectId(projectId);
        for (ProjectSstepRelation r : existing) {
            if (r != null && r.getSstepId() != null && r.getSstepId().equals(target.getSstepId())) {
                return 0;
            }
        }

        // 创建关系，负责人遵循置空规则，其余默认项目负责人
        ProjectSstepRelation rel = new ProjectSstepRelation();
        rel.setProjectId(projectId);
        rel.setSstepId(target.getSstepId());
        // 回填所属项目里程碑ID
        rel.setMilestoneId(resolveConstructMilestoneIdForStep(projectId, target));
        if (shouldEmptyDirector(target.getSstepName())) {
            rel.setDirector(null);
        } else {
            rel.setDirector(project.getProjectLeader());
        }

        projectSstepRelationRepository.save(rel);
        return 1;
    }

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
            // 跳过“接口开发”类型，后续在接口添加逻辑中生成相应步骤
            if (SKIP_TYPES_FOR_AUTOGEN.contains(type)) {
                continue;
            }
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
                // 回填所属项目里程碑ID
                rel.setMilestoneId(resolveConstructMilestoneIdForStep(project.getProjectId(), step));
                // 初始负责人：指定步骤置空，其余默认项目负责人
                if (shouldEmptyDirector(step.getSstepName())) {
                    rel.setDirector(null);
                } else {
                    rel.setDirector(project.getProjectLeader());
                }
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
                // 编辑时新增建设内容为“接口开发”，同样跳过自动生成
                if (SKIP_TYPES_FOR_AUTOGEN.contains(type)) {
                    continue;
                }
                List<StandardConstructStep> steps = standardConstructStepRepository
                        .findBySystemNameAndTypeOrderBySstepNameAsc(systemName, type);
                for (StandardConstructStep step : steps) {
                    Long sid = step.getSstepId();
                    if (sid == null) continue;
                    if (existingSstepIds.contains(sid)) continue;
                    ProjectSstepRelation rel = new ProjectSstepRelation();
                    rel.setProjectId(projectId);
                    rel.setSstepId(sid);
                    // 回填所属项目里程碑ID
                    rel.setMilestoneId(resolveConstructMilestoneIdForStep(projectId, step));
                    // 新增关系时，同步应用负责人置空规则
                    if (shouldEmptyDirector(step.getSstepName())) {
                        rel.setDirector(null);
                    } else {
                        rel.setDirector(projectLeader);
                    }
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

    /**
     * 为新增接口生成四个“接口开发”步骤关系（所属里程碑：05完成接口开发集成）。
     * 按项目绑定产品(systemName) + 类型("接口开发")筛选标准步骤，并仅保留该里程碑下的步骤。
     * 负责人规则与项目自动生成一致：部分步骤置空，其余默认项目负责人。
     *
     * @param iface 接口实体（需包含 projectId）
     * @return 生成的关系数量
     */
    public int generateRelationsForInterface(InterfaceEntity iface) {
        if (iface == null || iface.getProjectId() == null) {
            return 0;
        }

        // 获取项目与产品名称(systemName)以及项目负责人
        Long projectId = iface.getProjectId();
        String systemName = null;
        Long projectLeader = null;

        ConstructingProject project = constructingProjectRepository.findById(projectId).orElse(null);
        if (project != null) {
            projectLeader = project.getProjectLeader();
            if (project.getSoftId() != null) {
                ArchieveSoft soft = archieveSoftRepository.findById(project.getSoftId()).orElse(null);
                if (soft != null && StringUtils.hasText(soft.getSoftName())) {
                    systemName = soft.getSoftName().trim();
                }
            }
        }

        if (!StringUtils.hasText(systemName)) {
            // 无法确定产品名称时，无法准确筛选接口开发步骤，安全返回
            return 0;
        }

        // 找到目标里程碑“05完成接口开发集成”
        Long targetMilestoneId = null;
        List<StandardMilestone> milestones = standardMilestoneRepository.findAllByOrderByCreateTimeAsc();
        for (StandardMilestone m : milestones) {
            if (m != null && "05完成接口开发集成".equals(m.getMilestoneName())) {
                targetMilestoneId = m.getMilestoneId();
                break;
            }
        }

        // 获取系统名称 + 类型为“接口开发”的标准步骤
        List<StandardConstructStep> allInterfaceSteps = standardConstructStepRepository
                .findBySystemNameAndTypeOrderBySstepNameAsc(systemName, "接口开发");

        // 若能定位到目标里程碑，仅保留该里程碑下的步骤；否则全部使用类型为“接口开发”的步骤
        List<StandardConstructStep> targetSteps;
        if (targetMilestoneId != null) {
            final Long finalTargetMilestoneId = targetMilestoneId;
            targetSteps = allInterfaceSteps.stream()
                    .filter(s -> finalTargetMilestoneId.equals(s.getSmilestoneId()))
                    .collect(Collectors.toList());
        } else {
            targetSteps = allInterfaceSteps;
        }

        if (targetSteps.isEmpty()) {
            return 0;
        }

        // 检查是否已为该接口生成过步骤（按 projectId + interfaceId 判重）
        List<ProjectSstepRelation> existingForInterface = projectSstepRelationRepository
                .findByProjectIdAndInterfaceId(projectId, iface.getInterfaceId());

        if (existingForInterface != null && !existingForInterface.isEmpty()) {
            // 已生成过，避免重复
            return 0;
        }

        // 生成关系记录（每个标准步骤一条关系，带上 interfaceId 用于前端归属与编辑）
        List<ProjectSstepRelation> toCreate = new ArrayList<>();
        for (StandardConstructStep step : targetSteps) {
            if (step.getSstepId() == null) continue;
            ProjectSstepRelation rel = new ProjectSstepRelation();
            rel.setProjectId(projectId);
            rel.setSstepId(step.getSstepId());
            rel.setInterfaceId(iface.getInterfaceId());
            // 回填所属项目里程碑ID
            rel.setMilestoneId(resolveConstructMilestoneIdForStep(projectId, step));
            // 应用负责人置空规则
            if (shouldEmptyDirector(step.getSstepName())) {
                rel.setDirector(null);
            } else {
                rel.setDirector(projectLeader);
            }
            toCreate.add(rel);
        }

        if (toCreate.isEmpty()) {
            return 0;
        }

        projectSstepRelationRepository.saveAll(toCreate);
        return toCreate.size();
    }

    /**
     * 为新增的个性化开发项生成“个性化功能开发”类型且属于里程碑“06完成个性化功能开发”的项目-步骤关系。
     * 规则：
     * - 按项目绑定产品(systemName) + 类型("个性化功能开发")筛选标准步骤；
     * - 若能定位标准里程碑“06完成个性化功能开发”，仅保留该里程碑下的步骤；
     * - 每条关系关联 personalDevId，用于前端分组展示；
     * - 初始负责人遵循置空规则；其余默认项目负责人。
     *
     * @param pdev 个性化开发实体（需包含 projectId 和 personalDevId）
     * @return 生成的关系数量
     */
    public int generateRelationsForPersonalDevelope(PersonalDevelope pdev) {
        if (pdev == null || pdev.getProjectId() == null || pdev.getPersonalDevId() == null) {
            return 0;
        }

        Long projectId = pdev.getProjectId();
        String systemName = null;
        Long projectLeader = null;

        ConstructingProject project = constructingProjectRepository.findById(projectId).orElse(null);
        if (project != null) {
            projectLeader = project.getProjectLeader();
            if (project.getSoftId() != null) {
                ArchieveSoft soft = archieveSoftRepository.findById(project.getSoftId()).orElse(null);
                if (soft != null && StringUtils.hasText(soft.getSoftName())) {
                    systemName = soft.getSoftName().trim();
                }
            }
        }

        if (!StringUtils.hasText(systemName)) {
            return 0;
        }

        // 找到目标里程碑“06完成个性化功能开发”
        Long targetMilestoneId = null;
        List<StandardMilestone> milestones = standardMilestoneRepository.findAllByOrderByCreateTimeAsc();
        for (StandardMilestone m : milestones) {
            if (m != null && "06完成个性化功能开发".equals(m.getMilestoneName())) {
                targetMilestoneId = m.getMilestoneId();
                break;
            }
        }

        // 获取系统名称 + 类型为“个性化功能开发”的标准步骤
        List<StandardConstructStep> allPersonalSteps = standardConstructStepRepository
                .findBySystemNameAndTypeOrderBySstepNameAsc(systemName, "个性化功能开发");

        List<StandardConstructStep> targetSteps;
        if (targetMilestoneId != null) {
            final Long finalTargetMilestoneId = targetMilestoneId;
            targetSteps = allPersonalSteps.stream()
                    .filter(s -> finalTargetMilestoneId.equals(s.getSmilestoneId()))
                    .collect(Collectors.toList());
        } else {
            targetSteps = allPersonalSteps;
        }

        if (targetSteps.isEmpty()) {
            return 0;
        }

        // 避免重复：检查是否已为该个性化开发生成过步骤（按 projectId + personalDevId 判重）
        List<ProjectSstepRelation> existingForPersonal = projectSstepRelationRepository
                .findByProjectIdAndPersonalDevId(projectId, pdev.getPersonalDevId());
        if (existingForPersonal != null && !existingForPersonal.isEmpty()) {
            return 0;
        }

        // 生成关系记录（每个标准步骤一条关系，带上 personalDevId）
        List<ProjectSstepRelation> toCreate = new ArrayList<>();
        for (StandardConstructStep step : targetSteps) {
            if (step.getSstepId() == null) continue;
            ProjectSstepRelation rel = new ProjectSstepRelation();
            rel.setProjectId(projectId);
            rel.setSstepId(step.getSstepId());
            rel.setPersonalDevId(pdev.getPersonalDevId());
            // 回填所属项目里程碑ID
            rel.setMilestoneId(resolveConstructMilestoneIdForStep(projectId, step));
            if (shouldEmptyDirector(step.getSstepName())) {
                rel.setDirector(null);
            } else {
                rel.setDirector(projectLeader);
            }
            toCreate.add(rel);
        }

        if (toCreate.isEmpty()) {
            return 0;
        }

        projectSstepRelationRepository.saveAll(toCreate);
        return toCreate.size();
    }
}