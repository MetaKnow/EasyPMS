package com.missoft.pms.service;

import com.missoft.pms.dto.AfterServiceProjectDTO;
import com.missoft.pms.dto.HandoverRequest;
import com.missoft.pms.entity.AfterServiceProject;
import com.missoft.pms.entity.AfterServiceEvent;
import com.missoft.pms.entity.ArchieveSoft;
import com.missoft.pms.entity.ConstructingProject;
import com.missoft.pms.entity.User;
import com.missoft.pms.entity.ConstructMilestone;
import com.missoft.pms.entity.AfterServiceProjectParticipant;
import com.missoft.pms.repository.*;
import com.missoft.pms.service.AfterServiceEventService;
import com.missoft.pms.config.UserContextHolder;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;

/**
 * 运维项目服务类
 */
@Service
@Transactional
public class AfterServiceProjectService {

    @Autowired
    private AfterServiceProjectRepository afterServiceProjectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AfterServiceEventRepository afterServiceEventRepository;

    @Autowired
    private ConstructingProjectRepository constructingProjectRepository;

    @Autowired
    private ArchieveSoftRepository archieveSoftRepository;

    @Autowired
    private AfterServiceEventService afterServiceEventService;

  @Autowired
  private com.missoft.pms.repository.ConstructMilestoneRepository constructMilestoneRepository;

  @Autowired
    private ConstructMilestoneService constructMilestoneService;

    @Autowired
    private com.missoft.pms.repository.AfterServiceProjectParticipantRepository afterServiceProjectParticipantRepository;

    /**
     * 分页查询项目列表
     */
    public Page<AfterServiceProjectDTO> getProjects(String projectName, String status, Long saleDirector, Long serviceDirector, Long participantUserId, Pageable pageable) {
        Specification<AfterServiceProject> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(projectName)) {
                predicates.add(cb.like(root.get("projectName"), "%" + projectName + "%"));
            }

            if (StringUtils.hasText(status)) {
                predicates.add(cb.equal(root.get("serviceState"), status));
            }

            if (saleDirector != null) {
                predicates.add(cb.equal(root.get("saleDirector"), saleDirector));
            }

            if (serviceDirector != null) {
                predicates.add(cb.equal(root.get("serviceDirector"), serviceDirector));
            }

            if (participantUserId != null) {
                var participantSubquery = query.subquery(Long.class);
                var participantRoot = participantSubquery.from(AfterServiceProjectParticipant.class);
                participantSubquery.select(participantRoot.get("id"));
                participantSubquery.where(
                        cb.equal(participantRoot.get("projectId"), root.get("projectId")),
                        cb.equal(participantRoot.get("userId"), participantUserId)
                );
                predicates.add(cb.or(
                        cb.equal(root.get("saleDirector"), participantUserId),
                        cb.equal(root.get("serviceDirector"), participantUserId),
                        cb.exists(participantSubquery)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<AfterServiceProject> page = afterServiceProjectRepository.findAll(spec, pageable);
        return page.map(this::convertToDTO);
    }

    /**
     * 获取单个项目
     */
    public AfterServiceProjectDTO getProjectById(Long id) {
        AfterServiceProject project = afterServiceProjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        return convertToDTO(project);
    }

    /**
     * 从在建项目移交（校验：仅允许“进行中”且里程碑全部完成的项目）
     */
  public void handoverProject(HandoverRequest request) {
        ConstructingProject cp = constructingProjectRepository.findById(request.getConstructingProjectId())
                .orElseThrow(() -> new RuntimeException("在建项目不存在"));

        if (!"进行中".equals(cp.getProjectState())) {
            throw new RuntimeException("项目状态不符合移交条件：仅允许进行中");
        }

        // 移交流程前刷新里程碑完成状态，确保校验准确
        constructMilestoneService.updateMilestoneCompletionForProject(cp.getProjectId());
        List<ConstructMilestone> milestones = constructMilestoneRepository.findByProjectId(cp.getProjectId());
        boolean allCompleted = milestones.stream().allMatch(m -> Boolean.TRUE.equals(m.getIscomplete()));
        if (!allCompleted) {
            throw new RuntimeException("里程碑未全部完成，不可移交");
        }

        if (request.getServiceDirector() == null) {
            throw new RuntimeException("运维负责人不能为空");
        }
        if (isSystemAdmin(cp.getSaleLeader())) {
            throw new RuntimeException("销售负责人不能选择系统管理员");
        }
        if (isSystemAdmin(request.getServiceDirector())) {
            throw new RuntimeException("运维负责人不能选择系统管理员");
        }

        AfterServiceProject asp = new AfterServiceProject();
        asp.setProjectName(cp.getProjectName());
        asp.setCustomerId(cp.getCustomerId());
        asp.setSaleDirector(cp.getSaleLeader());

        if (cp.getSoftId() != null) {
            archieveSoftRepository.findById(cp.getSoftId())
                    .ifPresent(soft -> {
                        String v = soft.getSoftVersion();
                        String display = soft.getSoftName() + (v != null && !v.isEmpty() ? " (" + v + ")" : "");
                        asp.setArcSystem(display);
                    });
        } else {
            asp.setArcSystem("未知系统");
        }

        asp.setConstructingProjectId(cp.getProjectId());
        asp.setServiceState(request.getServiceState());
        asp.setServiceType(request.getServiceType());
        asp.setServiceDirector(request.getServiceDirector());
        asp.setServiceYear(request.getServiceYear());
        asp.setStartDate(request.getStartDate());
        asp.setEndDate(request.getEndDate());

        asp.setProjectNum(generateUniqueProjectNum());
        asp.setTotalHours(null);
        
        Long currentUserId = UserContextHolder.getCurrentUserId();
        if (currentUserId != null) {
            asp.setCreateUser(currentUserId);
            asp.setUpdateUser(currentUserId);
        }

        afterServiceProjectRepository.save(asp);

        cp.setIsCommitAfterSale(true);
        if (!"已完成".equals(cp.getProjectState())) {
            cp.setProjectState("已完成");
        }
        constructingProjectRepository.save(cp);
    }

    /**
     * 创建项目
     */
    public AfterServiceProjectDTO createProject(AfterServiceProjectDTO dto) {
        validateNoAdminAssignments(dto);
        AfterServiceProject project = new AfterServiceProject();
        BeanUtils.copyProperties(dto, project);
        project.setProjectId(null); // Ensure new
        
        Long currentUserId = UserContextHolder.getCurrentUserId();
        if (currentUserId != null) {
            project.setCreateUser(currentUserId);
            project.setUpdateUser(currentUserId);
        }

        // 保持总工时为系统统计值，创建时不接受前端传入的 totalHours
        project.setTotalHours(null);
        // 项目编号：如果前端已传入编号则优先使用（需校验唯一），否则生成新的唯一编号
        if (StringUtils.hasText(dto.getProjectNum())) {
            String candidate = dto.getProjectNum().trim();
            if (afterServiceProjectRepository.existsByProjectNum(candidate)) {
                throw new RuntimeException("项目编号已存在: " + candidate);
            }
            project.setProjectNum(candidate);
        } else {
            project.setProjectNum(generateUniqueProjectNum());
        }
        AfterServiceProject saved = afterServiceProjectRepository.save(project);
        saveProjectParticipants(saved.getProjectId(), dto.getParticipantIds());
        return convertToDTO(saved);
    }

    /**
     * 更新项目
     */
    public AfterServiceProjectDTO updateProject(Long id, AfterServiceProjectDTO dto) {
        return updateProject(id, dto, null);
    }

    public AfterServiceProjectDTO updateProject(Long id, AfterServiceProjectDTO dto, Long operatorUserId) {
        AfterServiceProject project = afterServiceProjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        assertParticipantReadOnly(project, resolveOperatorUserId(operatorUserId));
        validateNoAdminAssignments(dto);
        
        // 保持项目编号只读，不允许更新
        // 总工时由系统统计，忽略前端传入的 totalHours
        // 忽略createUser字段，防止被覆盖
        BeanUtils.copyProperties(dto, project, "projectId", "createTime", "updateTime", "projectNum", "totalHours", "createUser");
        project.setProjectId(id);
        
        Long currentUserId = UserContextHolder.getCurrentUserId();
        if (currentUserId != null) {
            project.setUpdateUser(currentUserId);
        }
        
        AfterServiceProject saved = afterServiceProjectRepository.save(project);
        replaceProjectParticipants(saved.getProjectId(), dto.getParticipantIds());
        return convertToDTO(saved);
    }

    /**
     * 删除项目
     */
    public void deleteProject(Long id) {
        deleteProject(id, null);
    }

    public void deleteProject(Long id, Long operatorUserId) {
        AfterServiceProject project = afterServiceProjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        assertParticipantReadOnly(project, resolveOperatorUserId(operatorUserId));

        // 先删除该项目下的运维事件及其附件与数据库记录
        List<AfterServiceEvent> events = afterServiceEventRepository.findByAfterServiceProjectId(id);
        if (events == null) {
            events = java.util.Collections.emptyList();
        }
        for (AfterServiceEvent ev : events) {
            try {
                afterServiceEventService.deleteEvent(ev.getEventId());
            } catch (Exception ignore) {}
        }

        // 尝试删除项目级附件目录（若为空则删除，不为空则忽略）
        try {
            String projNum = safe(project.getProjectNum());
            String projName = safe(project.getProjectName());
            String projectKey = projNum + "-" + projName;
            Path repoRoot = Paths.get("").toAbsolutePath().getParent();
            Path dir = repoRoot.resolve("afterServiceDeliverableFiles").resolve(projectKey);
            try {
                Files.delete(dir); // 空目录可删除；非空会抛异常，忽略
            } catch (Exception ignore) {}
        } catch (Exception ignore) {}

        afterServiceProjectParticipantRepository.deleteByProjectId(id);

        // 最后删除项目记录
        afterServiceProjectRepository.deleteById(id);
    }

    /**
     * 批量删除项目
     */
    public void batchDeleteProjects(List<Long> ids) {
        batchDeleteProjects(ids, null);
    }

    public void batchDeleteProjects(List<Long> ids, Long operatorUserId) {
        if (ids == null || ids.isEmpty()) return;
        Long resolvedOperatorUserId = resolveOperatorUserId(operatorUserId);
        for (Long id : ids) {
            try { deleteProject(id, resolvedOperatorUserId); } catch (Exception ignore) {}
        }
    }

    /**
     * 实体转DTO
     */
    private AfterServiceProjectDTO convertToDTO(AfterServiceProject entity) {
        AfterServiceProjectDTO dto = new AfterServiceProjectDTO();
        BeanUtils.copyProperties(entity, dto);
        
        if (entity.getSaleDirector() != null) {
            userRepository.findById(entity.getSaleDirector())
                    .ifPresent(user -> dto.setSaleDirectorName(user.getName()));
        }

        if (entity.getCustomerId() != null) {
            customerRepository.findById(entity.getCustomerId())
                    .ifPresent(customer -> dto.setCustomerName(customer.getCustomerName()));
        }
        
        if (entity.getServiceDirector() != null) {
            userRepository.findById(entity.getServiceDirector())
                    .ifPresent(user -> dto.setServiceDirectorName(user.getName()));
        }

        List<AfterServiceProjectParticipant> participants = afterServiceProjectParticipantRepository.findByProjectId(entity.getProjectId());
        dto.setParticipantIds(participants.stream()
                .map(AfterServiceProjectParticipant::getUserId)
                .collect(Collectors.toList()));

        // 计算并填充总工时字段
        dto.setTotalHours(computeTotalHours(entity.getProjectId()));

        return dto;
    }

    /**
     * 函数级注释：统计指定运维项目的总工时
     * 规则：累加该项目下所有运维事件的 eventhours（忽略空值）
     * @param projectId 运维项目ID
     * @return 总工时（无事件时返回 BigDecimal.ZERO）
     */
    private BigDecimal computeTotalHours(Long projectId) {
        if (projectId == null) return BigDecimal.ZERO;
        List<AfterServiceEvent> events = afterServiceEventRepository.findByAfterServiceProjectId(projectId);
        if (events == null || events.isEmpty()) return BigDecimal.ZERO;
        BigDecimal sum = BigDecimal.ZERO;
        for (AfterServiceEvent ev : events) {
            BigDecimal h = (ev != null) ? ev.getEventhours() : null;
            if (h != null) {
                sum = sum.add(h);
            }
        }
        return sum;
    }

    /**
     * 生成唯一项目编号：MS-YYYYMMDDHHMMSS
     */
    private String generateUniqueProjectNum() {
        String num;
        do {
            num = generateProjectNum();
        } while (afterServiceProjectRepository.existsByProjectNum(num));
        return num;
    }

    private String generateProjectNum() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return "MS-" + LocalDateTime.now().format(fmt);
    }

    /**
     * 对外提供：生成一个新的唯一项目编号（预览用）
     */
    public String generateNewProjectNum() {
        return generateUniqueProjectNum();
    }

    private void saveProjectParticipants(Long projectId, List<Long> participantIds) {
        if (projectId == null || participantIds == null || participantIds.isEmpty()) return;
        
        Long currentUserId = UserContextHolder.getCurrentUserId();
        
        for (Long userId : participantIds) {
            if (userId == null) continue;
            AfterServiceProjectParticipant participant = new AfterServiceProjectParticipant();
            participant.setProjectId(projectId);
            participant.setUserId(userId);
            
            if (currentUserId != null) {
                participant.setCreateUser(currentUserId);
                participant.setUpdateUser(currentUserId);
            }
            
            afterServiceProjectParticipantRepository.save(participant);
        }
    }

    private void replaceProjectParticipants(Long projectId, List<Long> participantIds) {
        if (projectId == null) return;
        afterServiceProjectParticipantRepository.deleteByProjectId(projectId);
        saveProjectParticipants(projectId, participantIds);
    }

    /**
     * 函数级注释：
     * 校验运维项目中的销售负责人、运维负责人、项目参与人不能选择系统管理员 admin。
     * 该校验用于拦截绕过前端下拉的非法提交，保证人员分配规则统一生效。
     *
     * @param dto 运维项目DTO
     */
    private void validateNoAdminAssignments(AfterServiceProjectDTO dto) {
        if (dto == null) {
            return;
        }
        if (isSystemAdmin(dto.getSaleDirector())) {
            throw new RuntimeException("销售负责人不能选择系统管理员");
        }
        if (isSystemAdmin(dto.getServiceDirector())) {
            throw new RuntimeException("运维负责人不能选择系统管理员");
        }
        if (dto.getParticipantIds() == null || dto.getParticipantIds().isEmpty()) {
            return;
        }
        boolean hasAdminParticipant = dto.getParticipantIds().stream()
                .filter(java.util.Objects::nonNull)
                .anyMatch(this::isSystemAdmin);
        if (hasAdminParticipant) {
            throw new RuntimeException("项目参与人不能选择系统管理员");
        }
    }

    private Long resolveOperatorUserId(Long preferUserId) {
        if (preferUserId != null) {
            return preferUserId;
        }
        return UserContextHolder.getCurrentUserId();
    }

    private void assertParticipantReadOnly(AfterServiceProject project, Long operatorUserId) {
        if (project == null || operatorUserId == null) {
            return;
        }
        if (isSystemAdmin(operatorUserId)) {
            return;
        }
        boolean isSaleDirector = java.util.Objects.equals(operatorUserId, project.getSaleDirector());
        boolean isServiceDirector = java.util.Objects.equals(operatorUserId, project.getServiceDirector());
        if (isSaleDirector || isServiceDirector) {
            return;
        }
        boolean isParticipant = afterServiceProjectParticipantRepository.existsByProjectIdAndUserId(project.getProjectId(), operatorUserId);
        if (isParticipant) {
            throw new RuntimeException("项目参与人仅可查看项目，无编辑或删除权限");
        }
    }

    private boolean isSystemAdmin(Long operatorUserId) {
        if (operatorUserId == null) {
            return false;
        }
        User user = userRepository.findById(operatorUserId).orElse(null);
        if (user == null || user.getUserName() == null) {
            return false;
        }
        return "admin".equalsIgnoreCase(user.getUserName().trim());
    }

    private String safe(String s) {
        if (s == null) return "";
        return s.trim().replaceAll("[\\/:*?\"<>|]", "_");
    }
}
