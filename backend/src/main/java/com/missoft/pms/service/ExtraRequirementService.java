package com.missoft.pms.service;

import com.missoft.pms.entity.ExtraRequirement;
import com.missoft.pms.entity.ConstructingProject;
import com.missoft.pms.repository.ConstructingProjectParticipantRepository;
import com.missoft.pms.repository.ConstructingProjectRepository;
import com.missoft.pms.repository.ExtraRequirementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 类级注释：
 * 合同外需求服务，负责创建与按项目查询。
 */
@Service
@Transactional
public class ExtraRequirementService {

    private static final List<String> REQUIREMENT_TYPES = List.of("个性化需求", "接口需求", "其他需求");

    @Autowired
    private ExtraRequirementRepository extraRequirementRepository;

    @Autowired
    private ExtraRequirementDeliverableFileService extraRequirementDeliverableFileService;

    @Autowired
    private ConstructingProjectModifyRecordService modifyRecordService;

    @Autowired
    private ConstructingProjectRepository constructingProjectRepository;

    @Autowired
    private ConstructingProjectParticipantRepository constructingProjectParticipantRepository;

    /**
     * 函数级注释：创建合同外需求条目
     * @param payload ExtraRequirement 数据
     * @return 保存后的实体
     */
    public ExtraRequirement create(ExtraRequirement payload, Long operatorUserId) {
        if (payload == null || payload.getProjectId() == null) {
            throw new IllegalArgumentException("projectId 不能为空");
        }
        if (payload.getRequirementName() == null || payload.getRequirementName().trim().isEmpty()) {
            throw new IllegalArgumentException("需求名称不能为空");
        }
        Long modifyUser = resolveOperatorUserId(operatorUserId, payload.getModifyUser());
        assertCanCreate(payload.getProjectId(), modifyUser);
        validateRequirementType(payload.getRequirementType());
        Boolean isPay = payload.getIsPay() != null ? payload.getIsPay() : Boolean.FALSE;
        if (!isPay) {
            payload.setPayAmount(null);
        } else {
            BigDecimal amt = payload.getPayAmount();
            if (amt != null && amt.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("付费金额不能为负数");
            }
        }
        if (modifyUser != null) {
            payload.setCreateUser(modifyUser);
            payload.setUpdateUser(modifyUser);
        }
        ExtraRequirement saved = extraRequirementRepository.save(payload);
        if (modifyUser != null) {
            String beforeText = "无";
            String afterText = "需求ID=" + formatValue(saved.getRequirementId())
                    + ", 需求名称=" + formatValue(saved.getRequirementName())
                    + ", 需求类型=" + formatValue(saved.getRequirementType());
            modifyRecordService.createRecord(saved.getProjectId(), modifyUser, "新增合同外需求", beforeText, afterText);
        }
        return saved;
    }

    public ExtraRequirement create(ExtraRequirement payload) {
        return create(payload, null);
    }

    /**
     * 函数级注释：按项目查询合同外需求列表
     * @param projectId 项目ID
     * @return 列表
     */
    @Transactional(readOnly = true)
    public List<ExtraRequirement> listByProjectId(Long projectId, Long operatorUserId) {
        if (projectId == null) return List.of();
        ConstructingProject project = constructingProjectRepository.findById(projectId).orElse(null);
        List<ExtraRequirement> list = extraRequirementRepository.findByProjectId(projectId);
        for (ExtraRequirement r : list) {
            if (r != null && r.getRequirementId() != null) {
                r.setHasFiles(extraRequirementDeliverableFileService.hasFiles(r.getRequirementId()));
                r.setCanEdit(canEditRequirement(project, r, operatorUserId));
                r.setCanDelete(canDeleteRequirement(project, r, operatorUserId));
            }
        }
        return list;
    }

    @Transactional(readOnly = true)
    public List<ExtraRequirement> listByProjectId(Long projectId) {
        return listByProjectId(projectId, null);
    }

    /**
     * 函数级注释：更新合同外需求条目
     * @param id 需求ID
     * @param payload 更新数据
     * @return 更新后的实体
     */
    public ExtraRequirement update(Long id, ExtraRequirement payload, Long operatorUserId) {
        ExtraRequirement existing = extraRequirementRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("未找到指定的合同外需求"));
        Long modifyUser = resolveOperatorUserId(operatorUserId, payload == null ? null : payload.getModifyUser());
        assertCanUpdate(existing, modifyUser);

        String oldRequirementName = existing.getRequirementName();
        String oldRequirementType = existing.getRequirementType();
        Boolean oldIsPay = existing.getIsPay();
        BigDecimal oldPayAmount = existing.getPayAmount();
        Boolean oldIsDeliver = existing.getIsDeliver();
        Boolean oldIsComplete = existing.getIsComplete();
        Boolean oldIsProductization = existing.getIsProductization();
        BigDecimal oldWorkload = existing.getWorkload();
        Long oldDeveloper = existing.getDeveloper();

        if (payload.getRequirementName() != null && !payload.getRequirementName().trim().isEmpty()) {
            existing.setRequirementName(payload.getRequirementName().trim());
        }
        if (payload.getRequirementType() != null) {
            validateRequirementType(payload.getRequirementType());
            existing.setRequirementType(payload.getRequirementType());
        }
        
        if (payload.getIsPay() != null) {
            existing.setIsPay(payload.getIsPay());
            if (!payload.getIsPay()) {
                existing.setPayAmount(null);
            } else if (payload.getPayAmount() != null) {
                 if (payload.getPayAmount().compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException("付费金额不能为负数");
                }
                existing.setPayAmount(payload.getPayAmount());
            }
        } else if (existing.getIsPay() != null && existing.getIsPay() && payload.getPayAmount() != null) {
             // 如果 isPay 未变（或 payload 未传），但更新了金额
             if (payload.getPayAmount().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("付费金额不能为负数");
            }
            existing.setPayAmount(payload.getPayAmount());
        }

        if (payload.getIsDeliver() != null) existing.setIsDeliver(payload.getIsDeliver());
        if (payload.getIsComplete() != null) existing.setIsComplete(payload.getIsComplete());
        if (payload.getIsProductization() != null) existing.setIsProductization(payload.getIsProductization());
        if (payload.getWorkload() != null) existing.setWorkload(payload.getWorkload());
        if (payload.getDeveloper() != null) existing.setDeveloper(payload.getDeveloper());
        
        if (modifyUser != null) {
            existing.setUpdateUser(modifyUser);
        }
        ExtraRequirement saved = extraRequirementRepository.save(existing);
        if (modifyUser != null) {
            String beforeText = buildRequirementBeforeText(oldRequirementName, oldRequirementType, oldIsPay, oldPayAmount,
                    oldIsDeliver, oldIsComplete, oldIsProductization, oldWorkload, oldDeveloper);
            String afterText = buildRequirementAfterText(saved);
            modifyRecordService.createRecord(saved.getProjectId(), modifyUser, "修改合同外需求", beforeText, afterText);
        }
        return saved;
    }

    public ExtraRequirement update(Long id, ExtraRequirement payload) {
        return update(id, payload, null);
    }

    private void validateRequirementType(String requirementType) {
        if (requirementType == null || requirementType.trim().isEmpty()) {
            throw new IllegalArgumentException("需求类型不能为空");
        }
        if (!REQUIREMENT_TYPES.contains(requirementType)) {
            throw new IllegalArgumentException("需求类型不合法");
        }
    }

    /**
     * 函数级注释：删除合同外需求条目并清理附件
     * @param id 需求ID
     */
    public void delete(Long id, Long operatorUserId) {
        if (id == null) {
            throw new IllegalArgumentException("需求ID不能为空");
        }
        ExtraRequirement existing = extraRequirementRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("未找到指定的合同外需求"));
        assertCanDelete(existing, operatorUserId);
        extraRequirementDeliverableFileService.deleteFilesByRequirementId(id);
        extraRequirementRepository.delete(existing);
    }

    public void delete(Long id) {
        delete(id, null);
    }

    private Long resolveOperatorUserId(Long operatorUserId, Long payloadModifyUser) {
        if (operatorUserId != null) return operatorUserId;
        return payloadModifyUser;
    }

    private void assertCanCreate(Long projectId, Long operatorUserId) {
        if (projectId == null || operatorUserId == null) return;
        ConstructingProject project = constructingProjectRepository.findById(projectId).orElse(null);
        if (project == null) return;
        if (isProjectManager(project, operatorUserId)) return;
        boolean isParticipant = constructingProjectParticipantRepository.existsByProjectIdAndUserId(projectId, operatorUserId);
        if (isParticipant) return;
        throw new IllegalArgumentException("您没有新增合同外需求的权限");
    }

    private void assertCanUpdate(ExtraRequirement requirement, Long operatorUserId) {
        if (requirement == null || requirement.getProjectId() == null || operatorUserId == null) return;
        ConstructingProject project = constructingProjectRepository.findById(requirement.getProjectId()).orElse(null);
        if (project != null && isProjectManager(project, operatorUserId)) return;
        boolean isParticipant = constructingProjectParticipantRepository.existsByProjectIdAndUserId(requirement.getProjectId(), operatorUserId);
        if (!isParticipant) {
            throw new IllegalArgumentException("您没有编辑合同外需求的权限");
        }
        if (isCreator(requirement, operatorUserId)) return;
        if (isDeveloper(requirement, operatorUserId)) return;
        throw new IllegalArgumentException("您没有编辑合同外需求的权限");
    }

    private void assertCanDelete(ExtraRequirement requirement, Long operatorUserId) {
        if (requirement == null || requirement.getProjectId() == null || operatorUserId == null) return;
        ConstructingProject project = constructingProjectRepository.findById(requirement.getProjectId()).orElse(null);
        if (project != null && isProjectManager(project, operatorUserId)) return;
        boolean isParticipant = constructingProjectParticipantRepository.existsByProjectIdAndUserId(requirement.getProjectId(), operatorUserId);
        if (!isParticipant) {
            throw new IllegalArgumentException("您没有删除合同外需求的权限");
        }
        if (isCreator(requirement, operatorUserId)) return;
        throw new IllegalArgumentException("您没有删除合同外需求的权限");
    }

    private boolean canEditRequirement(ConstructingProject project, ExtraRequirement requirement, Long operatorUserId) {
        if (operatorUserId == null) return true;
        if (project != null && isProjectManager(project, operatorUserId)) return true;
        boolean isParticipant = requirement != null
                && requirement.getProjectId() != null
                && constructingProjectParticipantRepository.existsByProjectIdAndUserId(requirement.getProjectId(), operatorUserId);
        if (!isParticipant) return false;
        return isCreator(requirement, operatorUserId) || isDeveloper(requirement, operatorUserId);
    }

    private boolean canDeleteRequirement(ConstructingProject project, ExtraRequirement requirement, Long operatorUserId) {
        if (operatorUserId == null) return true;
        if (project != null && isProjectManager(project, operatorUserId)) return true;
        boolean isParticipant = requirement != null
                && requirement.getProjectId() != null
                && constructingProjectParticipantRepository.existsByProjectIdAndUserId(requirement.getProjectId(), operatorUserId);
        if (!isParticipant) return false;
        return isCreator(requirement, operatorUserId);
    }

    private boolean isProjectManager(ConstructingProject project, Long userId) {
        if (project == null || userId == null) return false;
        return java.util.Objects.equals(project.getProjectLeader(), userId)
                || java.util.Objects.equals(project.getSaleLeader(), userId);
    }

    private boolean isCreator(ExtraRequirement requirement, Long userId) {
        if (requirement == null || userId == null) return false;
        return java.util.Objects.equals(requirement.getCreateUser(), userId);
    }

    private boolean isDeveloper(ExtraRequirement requirement, Long userId) {
        if (requirement == null || userId == null) return false;
        return java.util.Objects.equals(requirement.getDeveloper(), userId);
    }

    private String buildRequirementBeforeText(String oldRequirementName,
                                              String oldRequirementType,
                                              Boolean oldIsPay,
                                              BigDecimal oldPayAmount,
                                              Boolean oldIsDeliver,
                                              Boolean oldIsComplete,
                                              Boolean oldIsProductization,
                                              BigDecimal oldWorkload,
                                              Long oldDeveloper) {
        return "需求名称=" + formatValue(oldRequirementName)
                + ", 需求类型=" + formatValue(oldRequirementType)
                + ", 是否付费=" + formatBool(oldIsPay)
                + ", 付费金额=" + formatValue(oldPayAmount)
                + ", 是否交付=" + formatBool(oldIsDeliver)
                + ", 是否完成=" + formatBool(oldIsComplete)
                + ", 是否产品化=" + formatBool(oldIsProductization)
                + ", 工作量=" + formatValue(oldWorkload)
                + ", 开发负责人=" + formatValue(oldDeveloper);
    }

    private String buildRequirementAfterText(ExtraRequirement saved) {
        return "需求名称=" + formatValue(saved.getRequirementName())
                + ", 需求类型=" + formatValue(saved.getRequirementType())
                + ", 是否付费=" + formatBool(saved.getIsPay())
                + ", 付费金额=" + formatValue(saved.getPayAmount())
                + ", 是否交付=" + formatBool(saved.getIsDeliver())
                + ", 是否完成=" + formatBool(saved.getIsComplete())
                + ", 是否产品化=" + formatBool(saved.getIsProductization())
                + ", 工作量=" + formatValue(saved.getWorkload())
                + ", 开发负责人=" + formatValue(saved.getDeveloper());
    }

    private String formatBool(Boolean value) {
        if (value == null) return "无";
        return value ? "是" : "否";
    }

    private String formatValue(Object value) {
        if (value == null) return "无";
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? "无" : text;
    }
}
