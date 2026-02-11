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

    @Autowired
    private ExtraRequirementRepository extraRequirementRepository;

    @Autowired
    private ExtraRequirementDeliverableFileService extraRequirementDeliverableFileService;

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
        return extraRequirementRepository.save(payload);
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
        
        if (payload.getRequirementName() != null && !payload.getRequirementName().trim().isEmpty()) {
            existing.setRequirementName(payload.getRequirementName().trim());
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
        
        return extraRequirementRepository.save(existing);
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
}
