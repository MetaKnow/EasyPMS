package com.missoft.pms.service;

import com.missoft.pms.entity.ConstructingProjectRisk;
import com.missoft.pms.repository.ConstructingProjectRiskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 类级注释：项目风险服务
 */
@Service
@Transactional
public class ConstructingProjectRiskService {

    @Autowired
    private ConstructingProjectRiskRepository riskRepository;

    @Autowired
    private ConstructingProjectRiskFileService riskFileService;

    @Autowired
    private ConstructingProjectModifyRecordService modifyRecordService;

    @Autowired
    private com.missoft.pms.repository.ConstructingProjectRepository constructingProjectRepository;

    /**
     * 函数级注释：创建项目风险
     */
    public ConstructingProjectRisk create(ConstructingProjectRisk payload) {
        if (payload == null || payload.getProjectId() == null) {
            throw new IllegalArgumentException("projectId 不能为空");
        }
        if (payload.getRiskType() == null || payload.getRiskType().trim().isEmpty()) {
            throw new IllegalArgumentException("风险类型不能为空");
        }
        if (payload.getRiskLevel() == null || payload.getRiskLevel().trim().isEmpty()) {
            throw new IllegalArgumentException("风险级别不能为空");
        }
        if (payload.getIsRelieve() == null) {
            payload.setIsRelieve(Boolean.FALSE);
        }
        ConstructingProjectRisk saved = riskRepository.save(payload);
        Long modifyUser = resolveModifyUser(payload.getProjectId(), payload.getModifyUser(), payload.getCreator());
        if (modifyUser != null) {
            String beforeText = "无";
            String afterText = "风险ID=" + formatValue(saved.getRiskId())
                    + ", 风险类型=" + formatValue(saved.getRiskType())
                    + ", 风险级别=" + formatValue(saved.getRiskLevel());
            modifyRecordService.createRecord(saved.getProjectId(), modifyUser, "新增项目风险", beforeText, afterText);
        }
        return saved;
    }

    /**
     * 函数级注释：按项目ID加载风险列表并标记附件状态
     */
    @Transactional(readOnly = true)
    public List<ConstructingProjectRisk> listByProjectId(Long projectId) {
        if (projectId == null) return List.of();
        List<ConstructingProjectRisk> list = riskRepository.findByProjectId(projectId);
        for (ConstructingProjectRisk r : list) {
            if (r != null && r.getRiskId() != null) {
                r.setHasFiles(riskFileService.hasFiles(r.getRiskId()));
            }
        }
        return list;
    }

    /**
     * 函数级注释：更新项目风险
     */
    public ConstructingProjectRisk update(Long id, ConstructingProjectRisk payload) {
        ConstructingProjectRisk existing = riskRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("未找到指定的项目风险"));

        String oldRiskType = existing.getRiskType();
        String oldRiskLevel = existing.getRiskLevel();
        Boolean oldIsRelieve = existing.getIsRelieve();
        String oldRelieveWay = existing.getRelieveWay();
        String oldRiskDescription = existing.getRiskDescription();
        String oldRiskEvaluate = existing.getRiskEvaluate();
        Long oldCreator = existing.getCreator();

        if (payload.getRiskType() != null && !payload.getRiskType().trim().isEmpty()) {
            existing.setRiskType(payload.getRiskType().trim());
        }
        if (payload.getRiskLevel() != null && !payload.getRiskLevel().trim().isEmpty()) {
            existing.setRiskLevel(payload.getRiskLevel().trim());
        }
        if (payload.getIsRelieve() != null) {
            existing.setIsRelieve(payload.getIsRelieve());
        }
        if (payload.getRelieveWay() != null) {
            existing.setRelieveWay(payload.getRelieveWay());
        }
        if (payload.getRiskDescription() != null) {
            existing.setRiskDescription(payload.getRiskDescription());
        }
        if (payload.getRiskEvaluate() != null) {
            existing.setRiskEvaluate(payload.getRiskEvaluate());
        }
        if (payload.getCreator() != null) {
            existing.setCreator(payload.getCreator());
        }
        ConstructingProjectRisk saved = riskRepository.save(existing);
        Long modifyUser = resolveModifyUser(saved.getProjectId(), payload.getModifyUser(), payload.getCreator());
        if (modifyUser != null) {
            String beforeText = buildRiskBeforeText(oldRiskType, oldRiskLevel, oldIsRelieve, oldRelieveWay,
                    oldRiskDescription, oldRiskEvaluate, oldCreator);
            String afterText = buildRiskAfterText(saved);
            modifyRecordService.createRecord(saved.getProjectId(), modifyUser, "修改项目风险", beforeText, afterText);
        }
        return saved;
    }

    /**
     * 函数级注释：删除项目风险并清理附件
     */
    public void delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("风险ID不能为空");
        }
        ConstructingProjectRisk existing = riskRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("未找到指定的项目风险"));
        riskFileService.deleteFilesByRiskId(id);
        riskRepository.delete(existing);
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

    private String buildRiskBeforeText(String oldRiskType,
                                       String oldRiskLevel,
                                       Boolean oldIsRelieve,
                                       String oldRelieveWay,
                                       String oldRiskDescription,
                                       String oldRiskEvaluate,
                                       Long oldCreator) {
        return "风险类型=" + formatValue(oldRiskType)
                + ", 风险级别=" + formatValue(oldRiskLevel)
                + ", 是否解除=" + formatBool(oldIsRelieve)
                + ", 解除方式=" + formatValue(oldRelieveWay)
                + ", 风险描述=" + formatValue(oldRiskDescription)
                + ", 风险评估=" + formatValue(oldRiskEvaluate)
                + ", 提出人=" + formatValue(oldCreator);
    }

    private String buildRiskAfterText(ConstructingProjectRisk saved) {
        return "风险类型=" + formatValue(saved.getRiskType())
                + ", 风险级别=" + formatValue(saved.getRiskLevel())
                + ", 是否解除=" + formatBool(saved.getIsRelieve())
                + ", 解除方式=" + formatValue(saved.getRelieveWay())
                + ", 风险描述=" + formatValue(saved.getRiskDescription())
                + ", 风险评估=" + formatValue(saved.getRiskEvaluate())
                + ", 提出人=" + formatValue(saved.getCreator());
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
