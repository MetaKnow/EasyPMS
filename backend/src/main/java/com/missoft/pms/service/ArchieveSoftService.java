package com.missoft.pms.service;

import com.missoft.pms.entity.ArchieveSoft;
import com.missoft.pms.repository.ArchieveSoftRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

/**
 * 软件产品服务类
 * 提供软件产品相关的业务逻辑处理
 */
@Service
@Transactional
public class ArchieveSoftService {

    @Autowired
    private ArchieveSoftRepository archieveSoftRepository;

    /**
     * 分页查询产品列表
     *
     * @param page        页码（从0开始）
     * @param size        每页大小
     * @param softName    软件名称（可选）
     * @param softVersion 软件版本（可选）
     * @param sortBy      排序字段
     * @param sortDir     排序方向（asc/desc）
     * @return 产品分页数据
     */
    @Transactional(readOnly = true)
    public Page<ArchieveSoft> getArchieveSofts(int page, int size, String softName, String softVersion, 
                                               String sortBy, String sortDir) {
        // 创建排序对象
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        // 处理空字符串为null
        softName = StringUtils.hasText(softName) ? softName.trim() : null;
        softVersion = StringUtils.hasText(softVersion) ? softVersion.trim() : null;

        // 如果所有查询条件都为空，返回所有产品
        if (softName == null && softVersion == null) {
            return archieveSoftRepository.findAll(pageable);
        }

        // 使用多条件查询
        return archieveSoftRepository.findByMultipleConditions(softName, softVersion, pageable);
    }

    /**
     * 根据ID查询产品详情
     *
     * @param softId 产品ID
     * @return 产品详情
     */
    @Transactional(readOnly = true)
    public ArchieveSoft getArchieveSoftById(Long softId) {
        return archieveSoftRepository.findById(softId)
                .orElseThrow(() -> new RuntimeException("产品不存在，ID: " + softId));
    }

    /**
     * 创建新产品
     *
     * @param archieveSoft 产品信息
     * @return 创建后的产品
     */
    public ArchieveSoft createArchieveSoft(ArchieveSoft archieveSoft) {
        // 检查名称+版本是否已存在
        if (archieveSoftRepository.existsBySoftNameIgnoreCaseAndSoftVersionIgnoreCase(
                archieveSoft.getSoftName(), archieveSoft.getSoftVersion())) {
            throw new RuntimeException("产品名称与版本组合已存在: " + archieveSoft.getSoftName() + " - " + archieveSoft.getSoftVersion());
        }

        // 保存产品
        return archieveSoftRepository.save(archieveSoft);
    }

    /**
     * 更新产品信息
     *
     * @param softId      产品ID
     * @param archieveSoft 更新的产品信息
     * @return 更新后的产品
     */
    public ArchieveSoft updateArchieveSoft(Long softId, ArchieveSoft archieveSoft) {
        // 检查产品是否存在
        ArchieveSoft existingArchieveSoft = getArchieveSoftById(softId);

        // 检查名称+版本组合是否已被其他产品使用
        if (archieveSoftRepository.existsBySoftNameIgnoreCaseAndSoftVersionIgnoreCaseAndSoftIdNot(
                archieveSoft.getSoftName(), archieveSoft.getSoftVersion(), softId)) {
            throw new RuntimeException("产品名称与版本组合已存在: " + archieveSoft.getSoftName() + " - " + archieveSoft.getSoftVersion());
        }

        // 更新产品信息
        existingArchieveSoft.setSoftName(archieveSoft.getSoftName());
        existingArchieveSoft.setSoftVersion(archieveSoft.getSoftVersion());

        // 保存更新
        return archieveSoftRepository.save(existingArchieveSoft);
    }

    /**
     * 删除产品
     *
     * @param softId 产品ID
     */
    public void deleteArchieveSoft(Long softId) {
        // 检查产品是否存在
        if (!archieveSoftRepository.existsById(softId)) {
            throw new RuntimeException("产品不存在，ID: " + softId);
        }

        // 删除产品
        archieveSoftRepository.deleteById(softId);
    }

    /**
     * 批量删除产品
     *
     * @param softIds 产品ID列表
     * @return 删除的数量
     */
    public int deleteArchieveSofts(List<Long> softIds) {
        int deletedCount = 0;
        for (Long softId : softIds) {
            try {
                deleteArchieveSoft(softId);
                deletedCount++;
            } catch (Exception e) {
                // 记录错误，继续删除其他产品
                System.err.println("删除产品失败，ID: " + softId + ", 错误: " + e.getMessage());
            }
        }
        return deletedCount;
    }

    /**
     * 获取所有产品列表
     *
     * @return 产品列表
     */
    @Transactional(readOnly = true)
    public List<ArchieveSoft> getAllArchieveSofts() {
        return archieveSoftRepository.findAllByOrderBySoftNameAsc();
    }

    /**
     * 获取产品总数
     *
     * @return 产品总数
     */
    @Transactional(readOnly = true)
    public long getTotalArchieveSoftCount() {
        return archieveSoftRepository.count();
    }

    /**
     * 检查产品名称是否存在（不区分大小写）
     *
     * @param softName 产品名称
     * @return 是否存在
     */
    @Transactional(readOnly = true)
    public boolean existsBySoftName(String softName) {
        if (!org.springframework.util.StringUtils.hasText(softName)) {
            return false;
        }
        return archieveSoftRepository.existsBySoftNameIgnoreCase(softName.trim());
    }

    /**
     * 检查产品名称是否存在（排除指定ID，不区分大小写）
     *
     * @param softName  产品名称
     * @param excludeId 需要排除的产品ID
     * @return 是否存在
     */
    @Transactional(readOnly = true)
    public boolean existsBySoftNameAndVersion(String softName, String softVersion) {
        if (!org.springframework.util.StringUtils.hasText(softName) || !org.springframework.util.StringUtils.hasText(softVersion)) {
            return false;
        }
        return archieveSoftRepository.existsBySoftNameIgnoreCaseAndSoftVersionIgnoreCase(softName.trim(), softVersion.trim());
    }

    @Transactional(readOnly = true)
    public boolean existsBySoftNameAndVersionExcludingId(String softName, String softVersion, Long excludeId) {
        if (!org.springframework.util.StringUtils.hasText(softName) || !org.springframework.util.StringUtils.hasText(softVersion)) {
            return false;
        }
        if (excludeId == null) {
            return existsBySoftNameAndVersion(softName, softVersion);
        }
        return archieveSoftRepository.existsBySoftNameIgnoreCaseAndSoftVersionIgnoreCaseAndSoftIdNot(softName.trim(), softVersion.trim(), excludeId);
    }

    /**
     * 获取所有去重的产品名称
     *
     * @return 去重的产品名称列表
     */
    @Transactional(readOnly = true)
    public List<String> getDistinctSoftNames() {
        return archieveSoftRepository.findDistinctSoftNames();
    }
}