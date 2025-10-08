package com.missoft.pms.service;

import com.missoft.pms.entity.StandardConstructStep;
import com.missoft.pms.repository.StandardConstructStepRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 标准交付步骤服务类
 * 提供标准交付步骤相关的业务逻辑处理
 */
@Service
@Transactional
public class StandardConstructStepService {

    @Autowired
    private StandardConstructStepRepository standardConstructStepRepository;

    /**
     * 分页查询步骤列表
     *
     * @param page         页码（从0开始）
     * @param size         每页大小
     * @param sstepName    步骤名称（可选）
     * @param systemName   档案系统名称（可选）
     * @param type         步骤类型（可选）
     * @param smilestoneId 标准里程碑ID（可选）
     * @param sortBy       排序字段
     * @param sortDir      排序方向（asc/desc）
     * @return 步骤分页数据
     */
    @Transactional(readOnly = true)
    public Page<StandardConstructStep> getStandardConstructSteps(int page, int size, String sstepName,
                                                                String systemName, String type, Long smilestoneId,
                                                                String sortBy, String sortDir) {
        // 创建排序对象
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        // 处理空字符串为null
        sstepName = StringUtils.hasText(sstepName) ? sstepName.trim() : null;
        systemName = StringUtils.hasText(systemName) ? systemName.trim() : null;
        type = StringUtils.hasText(type) ? type.trim() : null;

        // 使用多条件查询
        return standardConstructStepRepository.findByMultipleConditions(
                sstepName, systemName, type, smilestoneId, pageable);
    }

    /**
     * 根据ID查询步骤详情
     *
     * @param sstepId 步骤ID
     * @return 步骤详情
     */
    @Transactional(readOnly = true)
    public StandardConstructStep getStandardConstructStepById(Long sstepId) {
        return standardConstructStepRepository.findById(sstepId)
                .orElseThrow(() -> new RuntimeException("标准交付步骤不存在，ID: " + sstepId));
    }

    /**
     * 创建新步骤
     *
     * @param standardConstructStep 步骤信息
     * @return 创建后的步骤
     */
    public StandardConstructStep createStandardConstructStep(StandardConstructStep standardConstructStep) {
        // 保存步骤
        return standardConstructStepRepository.save(standardConstructStep);
    }

    /**
     * 更新步骤信息
     *
     * @param sstepId               步骤ID
     * @param standardConstructStep 更新的步骤信息
     * @return 更新后的步骤
     */
    public StandardConstructStep updateStandardConstructStep(Long sstepId, StandardConstructStep standardConstructStep) {
        // 检查步骤是否存在
        StandardConstructStep existingStep = getStandardConstructStepById(sstepId);

        // 更新步骤信息
        existingStep.setSstepName(standardConstructStep.getSstepName());
        existingStep.setType(standardConstructStep.getType());
        existingStep.setSystemName(standardConstructStep.getSystemName());
        existingStep.setSmilestoneId(standardConstructStep.getSmilestoneId());

        // 保存更新
        return standardConstructStepRepository.save(existingStep);
    }

    /**
     * 删除步骤
     *
     * @param sstepId 步骤ID
     */
    public void deleteStandardConstructStep(Long sstepId) {
        // 检查步骤是否存在
        StandardConstructStep step = getStandardConstructStepById(sstepId);
        
        // 删除步骤
        standardConstructStepRepository.delete(step);
    }

    /**
     * 批量删除步骤
     *
     * @param sstepIds 步骤ID列表
     */
    public void batchDeleteStandardConstructSteps(List<Long> sstepIds) {
        if (sstepIds == null || sstepIds.isEmpty()) {
            throw new RuntimeException("删除的步骤ID列表不能为空");
        }

        // 检查所有步骤是否存在
        List<StandardConstructStep> steps = standardConstructStepRepository.findAllById(sstepIds);
        if (steps.size() != sstepIds.size()) {
            throw new RuntimeException("部分步骤不存在，无法批量删除");
        }

        // 批量删除
        standardConstructStepRepository.deleteAllById(sstepIds);
    }

    /**
     * 根据档案系统名称获取步骤列表
     *
     * @param systemName 档案系统名称
     * @return 步骤列表
     */
    @Transactional(readOnly = true)
    public List<StandardConstructStep> getStandardConstructStepsBySystemName(String systemName) {
        if (!StringUtils.hasText(systemName)) {
            return List.of();
        }
        return standardConstructStepRepository.findBySystemNameOrderBySstepNameAsc(systemName.trim());
    }

    /**
     * 获取所有步骤列表
     *
     * @return 步骤列表
     */
    @Transactional(readOnly = true)
    public List<StandardConstructStep> getAllStandardConstructSteps() {
        return standardConstructStepRepository.findAllByOrderBySstepNameAsc();
    }

    /**
     * 获取所有不重复的档案系统名称列表
     *
     * @return 档案系统名称列表
     */
    @Transactional(readOnly = true)
    public List<String> getDistinctSystemNames() {
        return standardConstructStepRepository.findDistinctSystemNames();
    }

    /**
     * 获取步骤总数
     *
     * @return 步骤总数
     */
    @Transactional(readOnly = true)
    public long getTotalStandardConstructStepCount() {
        return standardConstructStepRepository.count();
    }



    /**
     * 根据标准里程碑ID获取步骤列表
     *
     * @param smilestoneId 标准里程碑ID
     * @return 步骤列表
     */
    @Transactional(readOnly = true)
    public List<StandardConstructStep> getStandardConstructStepsByMilestoneId(Long smilestoneId) {
        if (smilestoneId == null) {
            return List.of();
        }
        return standardConstructStepRepository.findBySmilestoneId(smilestoneId);
    }

    /**
     * 根据步骤类型获取步骤列表
     *
     * @param type 步骤类型
     * @return 步骤列表
     */
    @Transactional(readOnly = true)
    public List<StandardConstructStep> getStandardConstructStepsByType(String type) {
        if (!StringUtils.hasText(type)) {
            return List.of();
        }
        return standardConstructStepRepository.findByType(type.trim());
    }
}