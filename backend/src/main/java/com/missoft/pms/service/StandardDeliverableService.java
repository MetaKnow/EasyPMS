package com.missoft.pms.service;

import com.missoft.pms.entity.StandardDeliverable;
import com.missoft.pms.dto.StandardDeliverableWithNamesDTO;
import com.missoft.pms.repository.StandardDeliverableRepository;
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
 * 标准交付物服务类
 * 提供标准交付物相关的业务逻辑处理
 */
@Service
@Transactional
public class StandardDeliverableService {

    @Autowired
    private StandardDeliverableRepository standardDeliverableRepository;

    /**
     * 分页查询交付物列表
     *
     * @param page            页码（从0开始）
     * @param size            每页大小
     * @param deliverableName 交付物名称（可选）
     * @param systemName      系统名称（可选）
     * @param deliverableType 交付物类型（可选）
     * @param sstepId         标准步骤ID（可选）
     * @param milestoneId     里程碑ID（可选）
     * @param sortBy          排序字段
     * @param sortDir         排序方向（asc/desc）
     * @return 交付物分页数据
     */
    @Transactional(readOnly = true)
    public Page<StandardDeliverable> getStandardDeliverables(int page, int size, String deliverableName,
                                                            String systemName, String deliverableType,
                                                            Long sstepId, Long milestoneId,
                                                            String sortBy, String sortDir) {
        // 创建排序对象
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        // 处理空字符串为null
        deliverableName = StringUtils.hasText(deliverableName) ? deliverableName.trim() : null;
        systemName = StringUtils.hasText(systemName) ? systemName.trim() : null;
        deliverableType = StringUtils.hasText(deliverableType) ? deliverableType.trim() : null;

        // 使用多条件查询
        return standardDeliverableRepository.findByMultipleConditions(
                deliverableName, systemName, deliverableType, sstepId, milestoneId, pageable);
    }

    /**
     * 分页查询交付物列表（包含里程碑名称和步骤名称）
     *
     * @param page            页码（从0开始）
     * @param size            每页大小
     * @param deliverableName 交付物名称（可选）
     * @param systemName      系统名称（可选）
     * @param deliverableType 交付物类型（可选）
     * @param sstepId         标准步骤ID（可选）
     * @param milestoneId     里程碑ID（可选）
     * @param sortBy          排序字段
     * @param sortDir         排序方向（asc/desc）
     * @return 包含名称的交付物分页数据
     */
    @Transactional(readOnly = true)
    public Page<StandardDeliverableWithNamesDTO> getStandardDeliverablesWithNames(int page, int size, String deliverableName,
                                                                                 String systemName, String deliverableType,
                                                                                 Long sstepId, Long milestoneId,
                                                                                 String sortBy, String sortDir) {
        // 创建分页对象，不使用排序参数，因为排序已在SQL中固定实现
        Pageable pageable = PageRequest.of(page, size);

        // 处理空字符串为null
        deliverableName = StringUtils.hasText(deliverableName) ? deliverableName.trim() : null;
        systemName = StringUtils.hasText(systemName) ? systemName.trim() : null;
        deliverableType = StringUtils.hasText(deliverableType) ? deliverableType.trim() : null;

        // 使用JOIN查询获取名称，排序已在SQL中实现：先按里程碑创建时间，再按步骤名称
        return standardDeliverableRepository.findStandardDeliverablesWithNames(
                deliverableName, systemName, deliverableType, sstepId, milestoneId, pageable);
    }

    /**
     * 根据ID查询交付物详情
     *
     * @param deliverableId 交付物ID
     * @return 交付物详情
     */
    @Transactional(readOnly = true)
    public StandardDeliverable getStandardDeliverableById(Long deliverableId) {
        return standardDeliverableRepository.findById(deliverableId)
                .orElseThrow(() -> new RuntimeException("标准交付物不存在，ID: " + deliverableId));
    }

    /**
     * 创建新交付物
     *
     * @param standardDeliverable 交付物信息
     * @return 创建后的交付物
     */
    public StandardDeliverable createStandardDeliverable(StandardDeliverable standardDeliverable) {
        // 验证交付物类型
        validateDeliverableType(standardDeliverable.getDeliverableType());

        // 验证业务逻辑
        validateDeliverableBusinessLogic(standardDeliverable);

        // 保存交付物
        return standardDeliverableRepository.save(standardDeliverable);
    }

    /**
     * 更新交付物信息
     *
     * @param deliverableId       交付物ID
     * @param standardDeliverable 更新的交付物信息
     * @return 更新后的交付物
     */
    public StandardDeliverable updateStandardDeliverable(Long deliverableId, StandardDeliverable standardDeliverable) {
        // 检查交付物是否存在
        StandardDeliverable existingDeliverable = getStandardDeliverableById(deliverableId);

        // 验证交付物类型
        validateDeliverableType(standardDeliverable.getDeliverableType());

        // 验证业务逻辑
        validateDeliverableBusinessLogic(standardDeliverable);

        // 更新交付物信息
        existingDeliverable.setDeliverableName(standardDeliverable.getDeliverableName());
        existingDeliverable.setSystemName(standardDeliverable.getSystemName());
        existingDeliverable.setDeliverableType(standardDeliverable.getDeliverableType());
        existingDeliverable.setIsMustLoad(standardDeliverable.getIsMustLoad());
        existingDeliverable.setSstepId(standardDeliverable.getSstepId());
        existingDeliverable.setMilestoneId(standardDeliverable.getMilestoneId());

        // 保存更新
        return standardDeliverableRepository.save(existingDeliverable);
    }

    /**
     * 删除交付物
     *
     * @param deliverableId 交付物ID
     */
    public void deleteStandardDeliverable(Long deliverableId) {
        // 检查交付物是否存在
        StandardDeliverable deliverable = getStandardDeliverableById(deliverableId);
        
        // 删除交付物
        standardDeliverableRepository.delete(deliverable);
    }

    /**
     * 批量删除交付物
     *
     * @param deliverableIds 交付物ID列表
     */
    public void batchDeleteStandardDeliverables(List<Long> deliverableIds) {
        if (deliverableIds == null || deliverableIds.isEmpty()) {
            throw new RuntimeException("删除的交付物ID列表不能为空");
        }

        // 检查所有交付物是否存在
        List<StandardDeliverable> deliverables = standardDeliverableRepository.findAllById(deliverableIds);
        if (deliverables.size() != deliverableIds.size()) {
            throw new RuntimeException("部分交付物不存在，无法批量删除");
        }

        // 批量删除
        standardDeliverableRepository.deleteAllById(deliverableIds);
    }

    /**
     * 根据系统名称获取交付物列表
     *
     * @param systemName 系统名称
     * @return 交付物列表
     */
    @Transactional(readOnly = true)
    public List<StandardDeliverable> getStandardDeliverablesBySystemName(String systemName) {
        if (!StringUtils.hasText(systemName)) {
            return List.of();
        }
        return standardDeliverableRepository.findBySystemNameOrderByDeliverableNameAsc(systemName.trim());
    }

    /**
     * 根据系统名称和交付物类型获取交付物列表
     *
     * @param systemName      系统名称
     * @param deliverableType 交付物类型
     * @return 交付物列表
     */
    @Transactional(readOnly = true)
    public List<StandardDeliverable> getStandardDeliverablesBySystemNameAndType(String systemName, String deliverableType) {
        if (!StringUtils.hasText(systemName) || !StringUtils.hasText(deliverableType)) {
            return List.of();
        }
        return standardDeliverableRepository.findBySystemNameAndDeliverableType(systemName.trim(), deliverableType.trim());
    }

    /**
     * 根据标准步骤ID获取交付物列表
     *
     * @param sstepId 标准步骤ID
     * @return 交付物列表
     */
    @Transactional(readOnly = true)
    public List<StandardDeliverable> getStandardDeliverablesBySstepId(Long sstepId) {
        if (sstepId == null) {
            return List.of();
        }
        return standardDeliverableRepository.findBySstepId(sstepId);
    }

    /**
     * 根据里程碑ID获取交付物列表
     *
     * @param milestoneId 里程碑ID
     * @return 交付物列表
     */
    @Transactional(readOnly = true)
    public List<StandardDeliverable> getStandardDeliverablesByMilestoneId(Long milestoneId) {
        if (milestoneId == null) {
            return List.of();
        }
        return standardDeliverableRepository.findByMilestoneId(milestoneId);
    }

    /**
     * 获取所有交付物列表
     *
     * @return 交付物列表
     */
    @Transactional(readOnly = true)
    public List<StandardDeliverable> getAllStandardDeliverables() {
        return standardDeliverableRepository.findAllByOrderByDeliverableNameAsc();
    }

    /**
     * 获取所有不重复的系统名称列表
     *
     * @return 系统名称列表
     */
    @Transactional(readOnly = true)
    public List<String> getDistinctSystemNames() {
        return standardDeliverableRepository.findDistinctSystemNames();
    }

    /**
     * 获取交付物总数
     *
     * @return 交付物总数
     */
    @Transactional(readOnly = true)
    public long getTotalStandardDeliverableCount() {
        return standardDeliverableRepository.count();
    }

    /**
     * 更新交付物的模板文件相对路径
     *
     * @param deliverableId 交付物ID
     * @param relativePath  相对路径，如 deliveryTemplete/<systemName>/ 或其子文件
     * @return 更新后的交付物
     */
    public StandardDeliverable updateDeliverableTemplatePath(Long deliverableId, String relativePath) {
        StandardDeliverable deliverable = getStandardDeliverableById(deliverableId);
        deliverable.setDeliveryTempletePath(relativePath);
        return standardDeliverableRepository.save(deliverable);
    }



    /**
     * 验证交付物类型
     *
     * @param deliverableType 交付物类型
     */
    private void validateDeliverableType(String deliverableType) {
        if (!StringUtils.hasText(deliverableType)) {
            throw new RuntimeException("交付物类型不能为空");
        }
        
        String type = deliverableType.trim();
        if (!"步骤交付物".equals(type) && !"里程碑交付物".equals(type)) {
            throw new RuntimeException("交付物类型只能是'步骤交付物'或'里程碑交付物'");
        }
    }

    /**
     * 验证交付物业务逻辑
     *
     * @param standardDeliverable 交付物信息
     */
    private void validateDeliverableBusinessLogic(StandardDeliverable standardDeliverable) {
        String deliverableType = standardDeliverable.getDeliverableType();
        
        if ("步骤交付物".equals(deliverableType)) {
            // 步骤交付物必须关联标准步骤
            if (standardDeliverable.getSstepId() == null) {
                throw new RuntimeException("步骤交付物必须关联标准步骤");
            }
            // 步骤交付物不应该关联里程碑
            if (standardDeliverable.getMilestoneId() != null) {
                throw new RuntimeException("步骤交付物不应该关联里程碑");
            }
        } else if ("里程碑交付物".equals(deliverableType)) {
            // 里程碑交付物必须关联里程碑
            if (standardDeliverable.getMilestoneId() == null) {
                throw new RuntimeException("里程碑交付物必须关联里程碑");
            }
            // 里程碑交付物不应该关联标准步骤
            if (standardDeliverable.getSstepId() != null) {
                throw new RuntimeException("里程碑交付物不应该关联标准步骤");
            }
        }
        
        // 验证必填字段
        if (!StringUtils.hasText(standardDeliverable.getDeliverableName())) {
            throw new RuntimeException("交付物名称不能为空");
        }
        
        if (!StringUtils.hasText(standardDeliverable.getSystemName())) {
            throw new RuntimeException("系统名称不能为空");
        }
    }
}