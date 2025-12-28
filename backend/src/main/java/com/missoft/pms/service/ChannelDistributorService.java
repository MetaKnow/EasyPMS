package com.missoft.pms.service;

import com.missoft.pms.entity.ChannelDistributor;
import com.missoft.pms.repository.ChannelDistributorRepository;
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
 * 渠道商服务类
 * 提供渠道商相关的业务逻辑处理
 * 
 * @author MissoftPMS
 * @version 1.0.0
 */
@Service
@Transactional
public class ChannelDistributorService {

    @Autowired
    private ChannelDistributorRepository channelDistributorRepository;

    /**
     * 分页查询渠道商列表
     *
     * @param page        页码（从0开始）
     * @param size        每页大小
     * @param channelName 渠道名称（可选）
     * @param contactor   联系人（可选）
     * @param phoneNumber 联系方式（可选）
     * @param sortBy      排序字段
     * @param sortDir     排序方向（asc/desc）
     * @return 渠道商分页数据
     */
    @Transactional(readOnly = true)
    public Page<ChannelDistributor> getChannelDistributors(int page, int size, String channelName, 
                                                          String contactor, String phoneNumber, 
                                                          Long saleDirector,
                                                          String sortBy, String sortDir) {
        // 创建排序对象
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        // 处理空字符串为null
        channelName = StringUtils.hasText(channelName) ? channelName.trim() : null;
        contactor = StringUtils.hasText(contactor) ? contactor.trim() : null;
        phoneNumber = StringUtils.hasText(phoneNumber) ? phoneNumber.trim() : null;

        // 如果所有查询条件都为空，返回所有渠道商
        if (channelName == null && contactor == null && phoneNumber == null && saleDirector == null) {
            return channelDistributorRepository.findAll(pageable);
        }

        // 使用多条件查询
        return channelDistributorRepository.findByMultipleConditions(channelName, contactor, phoneNumber, saleDirector, pageable);
    }

    /**
     * 根据ID查询渠道商
     *
     * @param channelId 渠道商ID
     * @return 渠道商对象
     * @throws RuntimeException 如果渠道商不存在
     */
    @Transactional(readOnly = true)
    public ChannelDistributor getChannelDistributorById(Long channelId) {
        return channelDistributorRepository.findById(channelId)
                .orElseThrow(() -> new RuntimeException("渠道商不存在，ID: " + channelId));
    }

    /**
     * 创建新渠道商
     *
     * @param channelDistributor 渠道商对象
     * @return 保存后的渠道商对象
     * @throws RuntimeException 如果渠道名称已存在
     */
    public ChannelDistributor createChannelDistributor(ChannelDistributor channelDistributor) {
        // 验证渠道名称是否已存在
        if (channelDistributorRepository.existsByChannelName(channelDistributor.getChannelName())) {
            throw new RuntimeException("渠道名称已存在: " + channelDistributor.getChannelName());
        }

        // 保存渠道商
        return channelDistributorRepository.save(channelDistributor);
    }

    /**
     * 更新渠道商信息
     *
     * @param channelId          渠道商ID
     * @param channelDistributor 更新的渠道商信息
     * @return 更新后的渠道商对象
     * @throws RuntimeException 如果渠道商不存在或渠道名称已被其他渠道商使用
     */
    public ChannelDistributor updateChannelDistributor(Long channelId, ChannelDistributor channelDistributor) {
        // 检查渠道商是否存在
        ChannelDistributor existingChannelDistributor = getChannelDistributorById(channelId);

        // 验证渠道名称是否被其他渠道商使用
        if (channelDistributorRepository.existsByChannelNameAndChannelIdNot(
                channelDistributor.getChannelName(), channelId)) {
            throw new RuntimeException("渠道名称已被其他渠道商使用: " + channelDistributor.getChannelName());
        }

        // 更新渠道商信息
        existingChannelDistributor.setChannelName(channelDistributor.getChannelName());
        existingChannelDistributor.setContactor(channelDistributor.getContactor());
        existingChannelDistributor.setPhoneNumber(channelDistributor.getPhoneNumber());
        existingChannelDistributor.setSaleDirector(channelDistributor.getSaleDirector());

        return channelDistributorRepository.save(existingChannelDistributor);
    }

    /**
     * 删除渠道商
     *
     * @param channelId 渠道商ID
     * @throws RuntimeException 如果渠道商不存在
     */
    public void deleteChannelDistributor(Long channelId) {
        // 检查渠道商是否存在
        if (!channelDistributorRepository.existsById(channelId)) {
            throw new RuntimeException("渠道商不存在，ID: " + channelId);
        }

        // 删除渠道商
        channelDistributorRepository.deleteById(channelId);
    }

    /**
     * 批量删除渠道商
     *
     * @param channelIds 渠道商ID列表
     * @return 成功删除的渠道商数量
     */
    public int deleteChannelDistributors(List<Long> channelIds) {
        if (channelIds == null || channelIds.isEmpty()) {
            return 0;
        }

        // 查找存在的渠道商
        List<ChannelDistributor> existingChannelDistributors = channelDistributorRepository.findByChannelIdIn(channelIds);
        
        if (existingChannelDistributors.isEmpty()) {
            throw new RuntimeException("没有找到要删除的渠道商");
        }

        // 批量删除
        channelDistributorRepository.deleteAll(existingChannelDistributors);
        
        return existingChannelDistributors.size();
    }

    /**
     * 检查渠道名称是否可用
     *
     * @param channelName 渠道名称
     * @param channelId   要排除的渠道商ID（可为null）
     * @return 是否可用
     */
    @Transactional(readOnly = true)
    public boolean isChannelNameAvailable(String channelName, Long channelId) {
        if (channelId == null) {
            return !channelDistributorRepository.existsByChannelName(channelName);
        } else {
            return !channelDistributorRepository.existsByChannelNameAndChannelIdNot(channelName, channelId);
        }
    }

    /**
     * 获取渠道商总数
     *
     * @return 渠道商总数
     */
    @Transactional(readOnly = true)
    public long getTotalCount() {
        return channelDistributorRepository.countAllChannelDistributors();
    }

    /**
     * 根据渠道名称查找渠道商
     *
     * @param channelName 渠道名称
     * @return 渠道商对象（如果存在）
     */
    @Transactional(readOnly = true)
    public ChannelDistributor findByChannelName(String channelName) {
        return channelDistributorRepository.findByChannelName(channelName).orElse(null);
    }

    /**
     * 获取所有渠道商列表（不分页）
     *
     * @return 渠道商列表
     */
    @Transactional(readOnly = true)
    public List<ChannelDistributor> getAllChannelDistributors() {
        return channelDistributorRepository.findAll();
    }
}
