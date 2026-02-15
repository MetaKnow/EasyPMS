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
        return riskRepository.save(payload);
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
        return riskRepository.save(existing);
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
}
