package com.missoft.pms.service;

import com.missoft.pms.dto.AfterServiceProjectDTO;
import com.missoft.pms.dto.HandoverRequest;
import com.missoft.pms.entity.AfterServiceProject;
import com.missoft.pms.entity.AfterServiceEvent;
import com.missoft.pms.entity.ArchieveSoft;
import com.missoft.pms.entity.ConstructingProject;
import com.missoft.pms.entity.User;
import com.missoft.pms.entity.ConstructMilestone;
import com.missoft.pms.repository.AfterServiceProjectRepository;
import com.missoft.pms.repository.AfterServiceEventRepository;
import com.missoft.pms.repository.ArchieveSoftRepository;
import com.missoft.pms.repository.ConstructingProjectRepository;
import com.missoft.pms.repository.CustomerRepository;
import com.missoft.pms.repository.UserRepository;
import com.missoft.pms.service.AfterServiceEventService;
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

/**
 * 运维项目服务
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

    /**
     * 分页查询项目列表
     */
    public Page<AfterServiceProjectDTO> getProjects(String projectName, String status, Long saleDirector, Pageable pageable) {
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

        List<ConstructMilestone> milestones = constructMilestoneRepository.findByProjectId(cp.getProjectId());
        boolean allCompleted = milestones.stream().allMatch(m -> Boolean.TRUE.equals(m.getIscomplete()));
        if (!allCompleted) {
            throw new RuntimeException("里程碑未全部完成，不可移交");
        }

        AfterServiceProject asp = new AfterServiceProject();
        asp.setProjectName(cp.getProjectName());
        asp.setCustomerId(cp.getCustomerId());
        asp.setSaleDirector(cp.getSaleLeader());

        if (cp.getSoftId() != null) {
            archieveSoftRepository.findById(cp.getSoftId())
                    .ifPresent(soft -> asp.setArcSystem(soft.getSoftName()));
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
        AfterServiceProject project = new AfterServiceProject();
        BeanUtils.copyProperties(dto, project);
        project.setProjectId(null); // Ensure new
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
        return convertToDTO(saved);
    }

    /**
     * 更新项目
     */
    public AfterServiceProjectDTO updateProject(Long id, AfterServiceProjectDTO dto) {
        AfterServiceProject project = afterServiceProjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        
        // 保持项目编号只读，不允许更新
        BeanUtils.copyProperties(dto, project, "projectId", "createTime", "updateTime", "projectNum");
        project.setProjectId(id);
        
        AfterServiceProject saved = afterServiceProjectRepository.save(project);
        return convertToDTO(saved);
    }

    /**
     * 删除项目
     */
    public void deleteProject(Long id) {
        AfterServiceProject project = afterServiceProjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));

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

        // 最后删除项目记录
        afterServiceProjectRepository.deleteById(id);
    }

    /**
     * 批量删除项目
     */
    public void batchDeleteProjects(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        for (Long id : ids) {
            try { deleteProject(id); } catch (Exception ignore) {}
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

        return dto;
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

    private String safe(String s) {
        if (s == null) return "";
        return s.trim().replaceAll("[\\/:*?\"<>|]", "_");
    }
}
