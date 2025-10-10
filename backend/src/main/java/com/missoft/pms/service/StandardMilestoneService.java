package com.missoft.pms.service;

import com.missoft.pms.entity.StandardMilestone;
import com.missoft.pms.repository.StandardMilestoneRepository;
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
 * 标准里程碑服务类
 * 提供标准里程碑相关的业务逻辑处理
 */
@Service
@Transactional
public class StandardMilestoneService {

    @Autowired
    private StandardMilestoneRepository standardMilestoneRepository;

    /**
     * 分页查询里程碑列表
     *
     * @param page          页码（从0开始）
     * @param size          每页大小
     * @param milestoneName 里程碑名称（可选）
     * @param sortBy        排序字段
     * @param sortDir       排序方向（asc/desc）
     * @return 里程碑分页数据
     */
    @Transactional(readOnly = true)
    public Page<StandardMilestone> getStandardMilestones(int page, int size, String milestoneName, 
                                                         String sortBy, String sortDir) {
        // 创建排序对象
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        // 处理空字符串为null
        milestoneName = StringUtils.hasText(milestoneName) ? milestoneName.trim() : null;

        // 如果查询条件为空，返回所有里程碑
        if (milestoneName == null) {
            return standardMilestoneRepository.findAll(pageable);
        }

        // 使用条件查询
        return standardMilestoneRepository.findByMultipleConditions(milestoneName, pageable);
    }

    /**
     * 根据ID查询里程碑详情
     *
     * @param milestoneId 里程碑ID
     * @return 里程碑详情
     */
    @Transactional(readOnly = true)
    public StandardMilestone getStandardMilestoneById(Long milestoneId) {
        return standardMilestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new RuntimeException("里程碑不存在，ID: " + milestoneId));
    }

    /**
     * 创建新里程碑
     *
     * @param standardMilestone 里程碑信息
     * @return 创建后的里程碑
     */
    public StandardMilestone createStandardMilestone(StandardMilestone standardMilestone) {
        // 检查名称是否已存在
        if (standardMilestoneRepository.existsByMilestoneNameIgnoreCase(
                standardMilestone.getMilestoneName())) {
            throw new RuntimeException("里程碑名称已存在: " + standardMilestone.getMilestoneName());
        }

        // 保存里程碑
        return standardMilestoneRepository.save(standardMilestone);
    }

    /**
     * 更新里程碑信息
     *
     * @param milestoneId       里程碑ID
     * @param standardMilestone 更新的里程碑信息
     * @return 更新后的里程碑
     */
    public StandardMilestone updateStandardMilestone(Long milestoneId, StandardMilestone standardMilestone) {
        // 检查里程碑是否存在
        StandardMilestone existingMilestone = getStandardMilestoneById(milestoneId);

        // 检查名称是否已被其他里程碑使用
        if (standardMilestoneRepository.existsByMilestoneNameIgnoreCaseAndMilestoneIdNot(
                standardMilestone.getMilestoneName(), milestoneId)) {
            throw new RuntimeException("里程碑名称已存在: " + standardMilestone.getMilestoneName());
        }

        // 更新里程碑信息
        existingMilestone.setMilestoneName(standardMilestone.getMilestoneName());

        // 保存更新
        return standardMilestoneRepository.save(existingMilestone);
    }

    /**
     * 删除里程碑
     *
     * @param milestoneId 里程碑ID
     */
    public void deleteStandardMilestone(Long milestoneId) {
        // 检查里程碑是否存在
        StandardMilestone milestone = getStandardMilestoneById(milestoneId);
        
        // 删除里程碑
        standardMilestoneRepository.delete(milestone);
    }

    /**
     * 批量删除里程碑
     *
     * @param milestoneIds 里程碑ID列表
     */
    public void batchDeleteStandardMilestones(List<Long> milestoneIds) {
        if (milestoneIds == null || milestoneIds.isEmpty()) {
            throw new RuntimeException("删除的里程碑ID列表不能为空");
        }

        // 检查所有里程碑是否存在
        List<StandardMilestone> milestones = standardMilestoneRepository.findAllById(milestoneIds);
        if (milestones.size() != milestoneIds.size()) {
            throw new RuntimeException("部分里程碑不存在，无法批量删除");
        }

        // 批量删除
        standardMilestoneRepository.deleteAllById(milestoneIds);
    }

    /**
     * 获取所有里程碑列表
     *
     * @return 里程碑列表
     */
    @Transactional(readOnly = true)
    public List<StandardMilestone> getAllStandardMilestones() {
        return standardMilestoneRepository.findAllByOrderByCreateTimeAsc();
    }

    /**
     * 获取里程碑总数
     *
     * @return 里程碑总数
     */
    @Transactional(readOnly = true)
    public long getTotalStandardMilestoneCount() {
        return standardMilestoneRepository.count();
    }

    /**
     * 检查里程碑名称是否存在（不区分大小写）
     *
     * @param milestoneName 里程碑名称
     * @return 是否存在
     */
    @Transactional(readOnly = true)
    public boolean existsByMilestoneName(String milestoneName) {
        if (!StringUtils.hasText(milestoneName)) {
            return false;
        }
        return standardMilestoneRepository.existsByMilestoneNameIgnoreCase(milestoneName.trim());
    }

    /**
     * 检查里程碑名称是否存在（排除指定ID，不区分大小写）
     *
     * @param milestoneName 里程碑名称
     * @param excludeId     需要排除的里程碑ID
     * @return 是否存在
     */
    @Transactional(readOnly = true)
    public boolean existsByMilestoneNameExcludingId(String milestoneName, Long excludeId) {
        if (!StringUtils.hasText(milestoneName)) {
            return false;
        }
        return standardMilestoneRepository.existsByMilestoneNameIgnoreCaseAndMilestoneIdNot(
                milestoneName.trim(), excludeId);
    }

    /**
     * 检查里程碑名称是否可用（用于前端异步验证）
     *
     * @param milestoneName 里程碑名称
     * @param milestoneId   可选，编辑场景下用于排除当前里程碑ID
     * @return 名称是否可用
     */
    @Transactional(readOnly = true)
    public boolean isMilestoneNameAvailable(String milestoneName, Long milestoneId) {
        if (!StringUtils.hasText(milestoneName)) {
            return false;
        }
        
        if (milestoneId != null) {
            // 编辑场景：检查除当前ID外是否有重复
            return !existsByMilestoneNameExcludingId(milestoneName, milestoneId);
        } else {
            // 新增场景：检查是否有重复
            return !existsByMilestoneName(milestoneName);
        }
    }
}