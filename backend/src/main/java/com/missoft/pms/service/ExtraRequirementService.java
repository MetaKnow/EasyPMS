package com.missoft.pms.service;

import com.missoft.pms.entity.ExtraRequirement;
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
    private com.missoft.pms.repository.ConstructingProjectRepository constructingProjectRepository;

    /**
     * 函数级注释：创建合同外需求条目
     * @param payload ExtraRequirement 数据
     * @return 保存后的实体
     */
    public ExtraRequirement create(ExtraRequirement payload) {
        if (payload == null || payload.getProjectId() == null) {
            throw new IllegalArgumentException("projectId 不能为空");
        }
        if (payload.getRequirementName() == null || payload.getRequirementName().trim().isEmpty()) {
            throw new IllegalArgumentException("需求名称不能为空");
        }
        validateRequirementType(payload.getRequirementType());
        // 处理 isPay/payAmount 的基本一致性：不付费时金额置空
        Boolean isPay = payload.getIsPay() != null ? payload.getIsPay() : Boolean.FALSE;
        if (!isPay) {
            payload.setPayAmount(null);
        } else {
            BigDecimal amt = payload.getPayAmount();
            if (amt != null && amt.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("付费金额不能为负数");
            }
        }
        ExtraRequirement saved = extraRequirementRepository.save(payload);
        Long modifyUser = resolveModifyUser(payload.getProjectId(), payload.getModifyUser(), payload.getDeveloper());
        if (modifyUser != null) {
            String beforeText = "无";
            String afterText = "需求ID=" + formatValue(saved.getRequirementId())
                    + ", 需求名称=" + formatValue(saved.getRequirementName())
                    + ", 需求类型=" + formatValue(saved.getRequirementType());
            modifyRecordService.createRecord(saved.getProjectId(), modifyUser, "新增合同外需求", beforeText, afterText);
        }
        return saved;
    }

    /**
     * 函数级注释：按项目查询合同外需求列表
     * @param projectId 项目ID
     * @return 列表
     */
    @Transactional(readOnly = true)
    public List<ExtraRequirement> listByProjectId(Long projectId) {
        if (projectId == null) return List.of();
        List<ExtraRequirement> list = extraRequirementRepository.findByProjectId(projectId);
        for (ExtraRequirement r : list) {
            if (r != null && r.getRequirementId() != null) {
                r.setHasFiles(extraRequirementDeliverableFileService.hasFiles(r.getRequirementId()));
            }
        }
        return list;
    }

    /**
     * 函数级注释：更新合同外需求条目
     * @param id 需求ID
     * @param payload 更新数据
     * @return 更新后的实体
     */
    public ExtraRequirement update(Long id, ExtraRequirement payload) {
        ExtraRequirement existing = extraRequirementRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("未找到指定的合同外需求"));

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
        
        ExtraRequirement saved = extraRequirementRepository.save(existing);
        Long modifyUser = resolveModifyUser(saved.getProjectId(), payload.getModifyUser(), payload.getDeveloper());
        if (modifyUser != null) {
            String beforeText = buildRequirementBeforeText(oldRequirementName, oldRequirementType, oldIsPay, oldPayAmount,
                    oldIsDeliver, oldIsComplete, oldIsProductization, oldWorkload, oldDeveloper);
            String afterText = buildRequirementAfterText(saved);
            modifyRecordService.createRecord(saved.getProjectId(), modifyUser, "修改合同外需求", beforeText, afterText);
        }
        return saved;
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
    public void delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("需求ID不能为空");
        }
        ExtraRequirement existing = extraRequirementRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("未找到指定的合同外需求"));
        extraRequirementDeliverableFileService.deleteFilesByRequirementId(id);
        extraRequirementRepository.delete(existing);
    }

    private Long resolveModifyUser(Long projectId, Long preferUserId, Long fallbackUserId) {
        if (preferUserId != null) return preferUserId;
        if (fallbackUserId != null) return fallbackUserId;
        if (projectId == null) return null;
        var project = constructingProjectRepository.findById(projectId).orElse(null);
        if (project != null && project.getProjectLeader() != null) return project.getProjectLeader();
        if (project != null) return project.getSaleLeader();
        return null;
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
