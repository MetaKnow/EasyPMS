package com.missoft.pms.service;

import com.missoft.pms.dto.ConstructingProjectDTO;
import com.missoft.pms.entity.ConstructingProject;
import com.missoft.pms.repository.ConstructingProjectRepository;
import com.missoft.pms.repository.ConstructDeliverableFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Comparator;
import java.util.Optional;

/**
 * 在建项目服务类
 * 提供在建项目相关的业务逻辑处理
 * 
 * @author MissoftPMS
 * @version 1.0.0
 */
@Service
@Transactional
public class ConstructingProjectService {

    @Autowired
    private ConstructingProjectRepository constructingProjectRepository;
    @Autowired
    private ConstructDeliverableFileRepository constructDeliverableFileRepository;
    @Autowired
    private ProjectSstepRelationService projectSstepRelationService;
    @Autowired
    private ConstructMilestoneService constructMilestoneService;
    @Autowired
    private InterfaceService interfaceService;
    @Autowired
    private PersonalDevelopeService personalDevelopeService;

    /**
     * 分页查询在建项目列表
     *
     * @param page          页码（从0开始）
     * @param size          每页大小
     * @param projectName   项目名称（可选）
     * @param year          年度（可选）
     * @param projectState  项目状态（可选）
     * @param projectLeader 项目负责人ID（可选）
     * @param customerId    客户ID（可选）
     * @param sortBy        排序字段
     * @param sortDir       排序方向（asc/desc）
     * @return 项目分页数据
     */
    @Transactional(readOnly = true)
    public Page<ConstructingProject> getConstructingProjects(int page, int size, String projectName,
                                                           Integer year, String projectState, Long projectLeader,
                                                           Long customerId, String sortBy, String sortDir) {
        // 创建排序对象
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        // 处理空字符串为null
        projectName = StringUtils.hasText(projectName) ? projectName.trim() : null;
        projectState = StringUtils.hasText(projectState) ? projectState.trim() : null;

        // 如果所有查询条件都为空，返回所有项目
        if (projectName == null && year == null && projectState == null && 
            projectLeader == null && customerId == null) {
            return constructingProjectRepository.findAll(pageable);
        }

        // 使用多条件查询
        return constructingProjectRepository.findByMultipleConditions(
                projectName, year, projectState, projectLeader, customerId, pageable);
    }

    /**
     * 分页查询在建项目列表（包含客户名称）
     *
     * @param page          页码（从0开始）
     * @param size          每页大小
     * @param projectName   项目名称（可选）
     * @param year          年度（可选）
     * @param projectState  项目状态（可选）
     * @param projectLeader 项目负责人ID（可选）
     * @param customerId    客户ID（可选）
     * @param sortBy        排序字段
     * @param sortDir       排序方向（asc/desc）
     * @return 项目分页数据（包含客户名称）
     */
    @Transactional(readOnly = true)
    public Page<ConstructingProjectDTO> getConstructingProjectsWithCustomerName(int page, int size, String projectName,
                                                                               Integer year, String projectState, Long projectLeader,
                                                                               Long customerId, String sortBy, String sortDir) {
        // 创建排序对象
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        // 处理空字符串为null
        projectName = StringUtils.hasText(projectName) ? projectName.trim() : null;
        projectState = StringUtils.hasText(projectState) ? projectState.trim() : null;

        // 使用多条件查询（包含客户名称）
        return constructingProjectRepository.findByMultipleConditionsWithCustomerName(
                projectName, year, projectState, projectLeader, customerId, pageable);
    }

    /**
     * 根据ID查询在建项目
     *
     * @param projectId 项目ID
     * @return 项目对象
     * @throws RuntimeException 如果项目不存在
     */
    @Transactional(readOnly = true)
    public ConstructingProject getConstructingProjectById(Long projectId) {
        return constructingProjectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("项目不存在，ID: " + projectId));
    }

    /**
     * 创建新项目
     *
     * @param constructingProject 项目对象
     * @return 保存后的项目对象
     * @throws RuntimeException 如果项目编号已存在或数据验证失败
     */
    public ConstructingProject createConstructingProject(ConstructingProject constructingProject) {
        // 验证必填字段
        validateConstructingProject(constructingProject);

        // 检查项目编号是否已存在
        if (StringUtils.hasText(constructingProject.getProjectNum()) && 
            constructingProjectRepository.existsByProjectNum(constructingProject.getProjectNum())) {
            throw new RuntimeException("项目编号已存在: " + constructingProject.getProjectNum());
        }

        // 如果没有提供项目编号，自动生成
        if (!StringUtils.hasText(constructingProject.getProjectNum())) {
            constructingProject.setProjectNum(generateProjectNumber());
        }

        // 设置默认值
        if (constructingProject.getProjectState() == null) {
            constructingProject.setProjectState("待开始");
        }
        if (constructingProject.getIsAgent() == null) {
            constructingProject.setIsAgent(0);
        }
        if (constructingProject.getReceivedMoney() == null) {
            constructingProject.setReceivedMoney(BigDecimal.ZERO);
        }
        if (constructingProject.getUnreceiveMoney() == null) {
            constructingProject.setUnreceiveMoney(BigDecimal.ZERO);
        }

        // 计算未回款金额
        calculateUnreceiveMoney(constructingProject);

        // 保存项目后优先生成项目里程碑（函数级注释：确保随后生成的步骤关系可正确回填 milestoneId）
        ConstructingProject saved = constructingProjectRepository.save(constructingProject);
        try {
            constructMilestoneService.generateMilestonesForProject(saved);
        } catch (Exception ignore) {
            // 里程碑生成失败不影响项目创建；可根据需要添加日志
        }
        try {
            projectSstepRelationService.generateRelationsForProject(saved);
        } catch (Exception ignore) {
            // 关系生成失败不影响项目创建；可根据需要添加日志
        }
        // 当新建项目勾选了“接口开发”时，补充生成“05业务系统接口需求调研”步骤（函数级注释：按产品+类型精准匹配步骤并避免重复）
        try {
            projectSstepRelationService.generateInterfaceDemandResearchForProject(saved.getProjectId());
        } catch (Exception ignore) {
            // 生成失败不影响项目创建；可根据需要添加日志
        }
        // 当新建项目勾选了“个性化功能开发”时，补充生成“06个性化功能需求调研”步骤（函数级注释：按产品+类型精准匹配步骤并避免重复）
        try {
            projectSstepRelationService.generatePersonalDemandResearchForProject(saved.getProjectId());
        } catch (Exception ignore) {
            // 生成失败不影响项目创建；可根据需要添加日志
        }
        // 在所有关系生成完成后，统一回填项目里程碑ID（函数级注释：提升里程碑与步骤的一致性）
        try {
            projectSstepRelationService.backfillMilestoneIdsForProject(saved.getProjectId());
        } catch (Exception ignore) {
            // 回填失败不影响项目创建；可根据需要添加日志
        }
        return saved;
    }

    /**
     * 更新项目
     *
     * @param projectId           项目ID
     * @param constructingProject 项目对象
     * @return 更新后的项目对象
     * @throws RuntimeException 如果项目不存在或数据验证失败
     */
    public ConstructingProject updateConstructingProject(Long projectId, ConstructingProject constructingProject) {
        // 检查项目是否存在
        ConstructingProject existingProject = getConstructingProjectById(projectId);
        // 记录编辑前的建设内容与产品ID
        String oldConstructContent = existingProject.getConstructContent();
        Long oldSoftId = existingProject.getSoftId();

        // 验证必填字段
        validateConstructingProject(constructingProject);

        // 检查项目编号是否与其他项目冲突
        if (StringUtils.hasText(constructingProject.getProjectNum()) && 
            !constructingProject.getProjectNum().equals(existingProject.getProjectNum()) &&
            constructingProjectRepository.existsByProjectNum(constructingProject.getProjectNum())) {
            throw new RuntimeException("项目编号已存在: " + constructingProject.getProjectNum());
        }

        // 更新字段
        if (StringUtils.hasText(constructingProject.getProjectNum())) {
            existingProject.setProjectNum(constructingProject.getProjectNum());
        }
        existingProject.setYear(constructingProject.getYear());
        existingProject.setProjectName(constructingProject.getProjectName());
        existingProject.setProjectType(constructingProject.getProjectType());
        existingProject.setProjectLeader(constructingProject.getProjectLeader());
        existingProject.setSaleLeader(constructingProject.getSaleLeader());
        existingProject.setCustomerId(constructingProject.getCustomerId());
        existingProject.setProjectState(constructingProject.getProjectState());
        existingProject.setSoftId(constructingProject.getSoftId());
        existingProject.setStartDate(constructingProject.getStartDate());
        existingProject.setPlanEndDate(constructingProject.getPlanEndDate());
        existingProject.setActualEndDate(constructingProject.getActualEndDate());
        existingProject.setPlanPeriod(constructingProject.getPlanPeriod());
        existingProject.setActualPeriod(constructingProject.getActualPeriod());
        existingProject.setIsAgent(constructingProject.getIsAgent());
        existingProject.setChannelId(constructingProject.getChannelId());
        existingProject.setValue(constructingProject.getValue());
        existingProject.setReceivedMoney(constructingProject.getReceivedMoney());
        existingProject.setAcceptanceDate(constructingProject.getAcceptanceDate());
        existingProject.setConstructContent(constructingProject.getConstructContent());

        // 计算未回款金额
        calculateUnreceiveMoney(existingProject);

        // 保存更新
        ConstructingProject saved = constructingProjectRepository.save(existingProject);

        // 先调整里程碑，再调整步骤关系，避免新增步骤无法正确回填 milestoneId
        // 编辑时根据“接口开发”建设内容的勾选状态，增删对应里程碑（函数级注释：确保未勾选时不显示接口开发里程碑）
        try {
            constructMilestoneService.adjustMilestonesForInterfaceDev(saved.getProjectId(), saved.getConstructContent());
        } catch (Exception ignore) {
            // 里程碑调整失败不影响项目更新；可按需记录日志
        }

        // 编辑时根据“个性化功能开发”建设内容的勾选状态，增删对应里程碑（函数级注释：与接口开发一致的规则）
        try {
            constructMilestoneService.adjustMilestonesForPersonalDev(saved.getProjectId(), saved.getConstructContent());
        } catch (Exception ignore) {
            // 里程碑调整失败不影响项目更新；可按需记录日志
        }

        // 基于建设内容的增减，按产品过滤增删对应关系，不影响其他步骤
        try {
            projectSstepRelationService.adjustRelationsForProjectOnEdit(
                    saved.getProjectId(),
                    oldConstructContent,
                    saved.getConstructContent(),
                    saved.getSoftId() != null ? saved.getSoftId() : oldSoftId,
                    saved.getProjectLeader()
            );
        } catch (Exception ignore) {
            // 关系调整失败不影响项目更新；可按需记录日志
        }

        // 根据当前实际生成的步骤同步项目里程碑集合（函数级注释：删除无步骤的里程碑，保留入口里程碑）
        try {
            constructMilestoneService.syncMilestonesWithProjectSteps(saved);
            // 步骤变更后，刷新里程碑工期汇总
            constructMilestoneService.updateMilestonePeriodsForProject(saved.getProjectId());
        } catch (Exception ignore) {
            // 同步失败不影响项目更新；可按需记录日志
        }

        // 当编辑取消勾选“接口开发”时，级联删除该项目下的接口数据（函数级注释：保持前后端一致性）
        try {
            Set<String> oldTypes = parseTypes(oldConstructContent);
            Set<String> newTypes = parseTypes(saved.getConstructContent());
            boolean removedInterfaceDev = oldTypes.contains("接口开发") && !newTypes.contains("接口开发");
            if (removedInterfaceDev) {
                interfaceService.deleteInterfacesByProject(saved.getProjectId());
            }
            // 当编辑新增勾选“接口开发”时，补充生成“05业务系统接口需求调研”步骤（函数级注释：仅在新增时生成一次，避免重复）
            boolean addedInterfaceDev = !oldTypes.contains("接口开发") && newTypes.contains("接口开发");
            if (addedInterfaceDev) {
                projectSstepRelationService.generateInterfaceDemandResearchForProject(saved.getProjectId());
            }

            // 当编辑取消勾选“个性化功能开发”时，删除该项目下的个性化开发记录（函数级注释：保持 personal_develope 与前端展示一致）
            boolean removedPersonalDev = oldTypes.contains("个性化功能开发") && !newTypes.contains("个性化功能开发");
            if (removedPersonalDev) {
                personalDevelopeService.deleteByProjectId(saved.getProjectId());
            }
            // 当编辑新增勾选“个性化功能开发”时，补充生成“06个性化功能需求调研”步骤（函数级注释：仅在新增时生成一次，避免重复）
            boolean addedPersonalDev = !oldTypes.contains("个性化功能开发") && newTypes.contains("个性化功能开发");
            if (addedPersonalDev) {
                projectSstepRelationService.generatePersonalDemandResearchForProject(saved.getProjectId());
            }
        } catch (Exception ignore) {
            // 接口数据清理失败不影响项目更新；可按需记录日志
        }

        // 编辑新增或调整关系后，统一回填项目里程碑ID（函数级注释：确保新增关系也具备里程碑归属）
        try {
            projectSstepRelationService.backfillMilestoneIdsForProject(saved.getProjectId());
        } catch (Exception ignore) {
            // 回填失败不影响项目更新；可按需记录日志
        }

        return saved;
    }

    /**
     * 删除项目
     *
     * @param projectId 项目ID
     * @throws RuntimeException 如果项目不存在
     */
    public void deleteConstructingProject(Long projectId) {
        // 函数级注释：删除在建项目，同时最佳努力清理该项目的交付物文件与记录。
        // 处理顺序：
        // 1. 获取项目信息以构建 deliverableFiles/<项目编号-项目名称>/ 路径前缀；
        // 2. 删除该项目的交付物文件数据库记录（不阻塞主流程，异常忽略）；
        // 3. 递归删除文件系统目录 deliverableFiles/<项目编号-项目名称>/（不阻塞主流程，异常忽略）；
        // 4. 删除项目本身记录。

        ConstructingProject project = constructingProjectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("项目不存在，ID: " + projectId));

        // 删除交付物文件数据库记录
        try {
            var records = constructDeliverableFileRepository.findByProjectId(projectId);
            if (records != null && !records.isEmpty()) {
                constructDeliverableFileRepository.deleteAll(records);
            }
        } catch (Exception ignore) {
            // 文件记录删除失败不阻塞项目删除
        }

        // 删除物理目录 deliverableFiles/<项目编号-项目名称>/
        try {
            String projectKey = buildProjectKey(project);
            Path repoRoot = Paths.get("").toAbsolutePath().getParent();
            Path projectDir = repoRoot.resolve("deliverableFiles").resolve(projectKey);
            deleteDirectoryRecursively(projectDir);
        } catch (Exception ignore) {
            // 物理文件删除失败不阻塞项目删除
        }

        // 删除项目记录
        constructingProjectRepository.deleteById(projectId);
    }

    /**
     * 批量删除项目
     *
     * @param projectIds 项目ID列表
     */
    public void batchDeleteConstructingProjects(List<Long> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            throw new RuntimeException("项目ID列表不能为空");
        }
        
        for (Long projectId : projectIds) {
            if (!constructingProjectRepository.existsById(projectId)) {
                throw new RuntimeException("项目不存在，ID: " + projectId);
            }
        }
        
        // 函数级注释：批量删除项目时，同时清理每个项目的交付物文件与记录（最佳努力）。
        for (Long projectId : projectIds) {
            try {
                ConstructingProject project = constructingProjectRepository.findById(projectId).orElse(null);
                // 删除交付物文件数据库记录
                try {
                    var records = constructDeliverableFileRepository.findByProjectId(projectId);
                    if (records != null && !records.isEmpty()) {
                        constructDeliverableFileRepository.deleteAll(records);
                    }
                } catch (Exception ignore) {}

                // 删除物理目录 deliverableFiles/<项目编号-项目名称>/
                if (project != null) {
                    try {
                        String projectKey = buildProjectKey(project);
                        Path repoRoot = Paths.get("").toAbsolutePath().getParent();
                        Path projectDir = repoRoot.resolve("deliverableFiles").resolve(projectKey);
                        deleteDirectoryRecursively(projectDir);
                    } catch (Exception ignore) {}
                }
            } catch (Exception ignore) {
                // 单个项目清理失败不阻塞整体批量删除
            }
        }

        // 最后删除项目记录
        constructingProjectRepository.deleteAllById(projectIds);
    }

    /**
     * 验证项目数据
     *
     * @param constructingProject 项目对象
     * @throws RuntimeException 如果验证失败
     */
    private void validateConstructingProject(ConstructingProject constructingProject) {
        if (constructingProject == null) {
            throw new RuntimeException("项目信息不能为空");
        }

        if (!StringUtils.hasText(constructingProject.getProjectName())) {
            throw new RuntimeException("项目名称不能为空");
        }

        if (!StringUtils.hasText(constructingProject.getProjectType())) {
            throw new RuntimeException("项目类型不能为空");
        }

        if (constructingProject.getYear() == null) {
            throw new RuntimeException("年度不能为空");
        }

        // 验证年度范围
        int currentYear = LocalDate.now().getYear();
        if (constructingProject.getYear() < 2000 || constructingProject.getYear() > currentYear + 10) {
            throw new RuntimeException("年度必须在2000年到" + (currentYear + 10) + "年之间");
        }

        // 验证金额字段
        if (constructingProject.getValue() != null && constructingProject.getValue().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("项目金额不能为负数");
        }

        if (constructingProject.getReceivedMoney() != null && constructingProject.getReceivedMoney().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("已回款金额不能为负数");
        }

        // 验证渠道项目信息
        if (constructingProject.getIsAgent() != null && constructingProject.getIsAgent() == 1) {
            if (constructingProject.getChannelId() == null) {
                throw new RuntimeException("渠道项目必须选择渠道名称");
            }
        }
    }

    /**
     * 生成项目编号
     * 格式：MS-YYYYMMDDHHMMSS
     *
     * @return 项目编号
     */
    private String generateProjectNumber() {
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String dateStr = now.format(formatter);
        
        // 获取当天已有项目数量，用于生成序号
        long count = constructingProjectRepository.countByYear(now.getYear());
        String sequence = String.format("%04d", count + 1);
        
        return "MS-" + dateStr + sequence;
    }

    /**
     * 计算未回款金额
     *
     * @param constructingProject 项目对象
     */
    private void calculateUnreceiveMoney(ConstructingProject constructingProject) {
        if (constructingProject.getValue() != null && constructingProject.getReceivedMoney() != null) {
            BigDecimal unreceiveMoney = constructingProject.getValue().subtract(constructingProject.getReceivedMoney());
            constructingProject.setUnreceiveMoney(unreceiveMoney.max(BigDecimal.ZERO));
        } else if (constructingProject.getValue() != null) {
            constructingProject.setUnreceiveMoney(constructingProject.getValue());
        } else {
            constructingProject.setUnreceiveMoney(BigDecimal.ZERO);
        }
    }

    /**
     * 获取所有项目（不分页）
     *
     * @return 项目列表
     */
    @Transactional(readOnly = true)
    public List<ConstructingProject> getAllConstructingProjects() {
        return constructingProjectRepository.findAll();
    }

    /**
     * 根据年度统计项目数量
     *
     * @param year 年度
     * @return 项目数量
     */
    @Transactional(readOnly = true)
    public long countByYear(Integer year) {
        return constructingProjectRepository.countByYear(year);
    }

    /**
     * 根据项目状态统计项目数量
     *
     * @param projectState 项目状态
     * @return 项目数量
     */
    @Transactional(readOnly = true)
    public long countByProjectState(String projectState) {
        return constructingProjectRepository.countByProjectState(projectState);
    }

    /**
     * 函数级注释：构建项目目录键 `<项目编号-项目名称>`，用于拼接 deliverableFiles 子目录。
     * 规则与交付物上传路径保持一致，替换非法文件名字符为下划线。
     *
     * @param project 在建项目实体
     * @return 目录键，如 `MS-20250101_0001-智慧政务平台`
     */
    private String buildProjectKey(ConstructingProject project) {
        String projNum = Optional.ofNullable(project.getProjectNum()).orElse("unknown").trim();
        String projName = Optional.ofNullable(project.getProjectName()).orElse("未命名项目").trim();
        String safeProjNum = projNum.replaceAll("[\\\\/:*?\"<>|]", "_");
        String safeProjName = projName.replaceAll("[\\\\/:*?\"<>|]", "_");
        return safeProjNum + "-" + safeProjName;
    }

    /**
     * 函数级注释：递归删除指定目录及其所有子文件/子目录。
     * 若目录不存在则直接返回；删除过程中的异常被忽略以保证主流程。
     *
     * @param dir 需要删除的目录路径
     */
    private void deleteDirectoryRecursively(Path dir) {
        try {
            if (dir == null || !Files.exists(dir)) {
                return;
            }
            // 先删文件，再删子目录，最后删根目录
            Files.walk(dir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (Exception ignore) {}
                    });
        } catch (Exception ignore) {
            // 忽略清理异常
        }
    }

    /**
     * 解析项目建设内容类型集合（函数级注释：按“/”分隔并去除空白）
     * @param constructContent 项目建设内容，按“/”分隔
     * @return 去重后的类型集合
     */
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
}