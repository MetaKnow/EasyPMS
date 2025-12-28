package com.missoft.pms.service;

import com.missoft.pms.entity.Customer;
import com.missoft.pms.repository.CustomerRepository;
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
 * 客户服务类
 * 提供客户相关的业务逻辑处理
 */
@Service
@Transactional
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    /**
     * 分页查询客户列表
     *
     * @param page          页码（从0开始）
     * @param size          每页大小
     * @param customerName  客户名称（可选）
     * @param province      省份（可选）
     * @param customerRank  客户等级（可选）
     * @param sortBy        排序字段
     * @param sortDir       排序方向（asc/desc）
     * @param saleLeader    销售负责人（可选）
     * @param ifDeal        是否成交（可选）
     * @param customerOwner 客户归属（可选）
     * @param channelId     渠道ID（可选）
     * @return 客户分页数据
     */
    @Transactional(readOnly = true)
    public Page<Customer> getCustomers(int page, int size, String customerName,
                                       String province, String customerRank, String sortBy, String sortDir,
                                       Long saleLeader, Boolean ifDeal, String customerOwner, Long channelId) {
        // 创建排序对象
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        // 处理空字符串为null
        customerName = StringUtils.hasText(customerName) ? customerName.trim() : null;
        province = StringUtils.hasText(province) ? province.trim() : null;
        customerRank = StringUtils.hasText(customerRank) ? customerRank.trim() : null;
        customerOwner = StringUtils.hasText(customerOwner) ? customerOwner.trim() : null;

        // 如果所有查询条件都为空，返回所有客户
        if (customerName == null && province == null && customerRank == null && saleLeader == null && ifDeal == null && customerOwner == null && channelId == null) {
            return customerRepository.findAll(pageable);
        }

        // 使用多条件查询
        return customerRepository.findByMultipleConditions(customerName, province, customerRank, saleLeader, ifDeal, customerOwner, channelId, pageable);
    }

    /**
     * 根据ID查询客户
     *
     * @param customerId 客户ID
     * @return 客户对象
     * @throws RuntimeException 如果客户不存在
     */
    @Transactional(readOnly = true)
    public Customer getCustomerById(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("客户不存在，ID: " + customerId));
    }

    /**
     * 创建新客户
     *
     * @param customer 客户对象
     * @return 保存后的客户对象
     * @throws RuntimeException 如果客户名称或联系方式已存在
     */
    public Customer createCustomer(Customer customer) {
        // 验证客户名称是否已存在
        if (customerRepository.findByCustomerName(customer.getCustomerName()).isPresent()) {
            throw new RuntimeException("客户名称已存在: " + customer.getCustomerName());
        }

        // 验证联系方式是否已存在
        if (customerRepository.findByPhoneNumber(customer.getPhoneNumber()).isPresent()) {
            throw new RuntimeException("联系方式已存在: " + customer.getPhoneNumber());
        }

        // 保存客户
        return customerRepository.save(customer);
    }

    /**
     * 更新客户信息
     *
     * @param customerId 客户ID
     * @param customer   更新的客户信息
     * @return 更新后的客户对象
     * @throws RuntimeException 如果客户不存在或客户名称/联系方式已被其他客户使用
     */
    public Customer updateCustomer(Long customerId, Customer customer) {
        // 检查客户是否存在
        Customer existingCustomer = getCustomerById(customerId);

        // 验证客户名称是否被其他客户使用
        if (customerRepository.existsByCustomerNameAndCustomerIdNot(customer.getCustomerName(), customerId)) {
            throw new RuntimeException("客户名称已被其他客户使用: " + customer.getCustomerName());
        }

        // 验证联系方式是否被其他客户使用
        if (customerRepository.existsByPhoneNumberAndCustomerIdNot(customer.getPhoneNumber(), customerId)) {
            throw new RuntimeException("联系方式已被其他客户使用: " + customer.getPhoneNumber());
        }

        // 更新客户信息
        existingCustomer.setCustomerName(customer.getCustomerName());
        existingCustomer.setContact(customer.getContact());
        existingCustomer.setPhoneNumber(customer.getPhoneNumber());
        existingCustomer.setProvince(customer.getProvince());
        existingCustomer.setCustomerRank(customer.getCustomerRank());
        existingCustomer.setSaleLeader(customer.getSaleLeader());
        existingCustomer.setIfDeal(customer.getIfDeal());
        existingCustomer.setCustomerOwner(customer.getCustomerOwner());
        existingCustomer.setChannelId(customer.getChannelId());

        return customerRepository.save(existingCustomer);
    }

    /**
     * 删除客户
     *
     * @param customerId 客户ID
     * @throws RuntimeException 如果客户不存在
     */
    public void deleteCustomer(Long customerId) {
        // 检查客户是否存在
        if (!customerRepository.existsById(customerId)) {
            throw new RuntimeException("客户不存在，ID: " + customerId);
        }

        // 删除客户
        customerRepository.deleteById(customerId);
    }

    /**
     * 批量删除客户
     *
     * @param customerIds 客户ID列表
     * @return 成功删除的客户数量
     */
    public int deleteCustomers(List<Long> customerIds) {
        if (customerIds == null || customerIds.isEmpty()) {
            return 0;
        }

        // 查找存在的客户
        List<Customer> existingCustomers = customerRepository.findByCustomerIdIn(customerIds);
        
        if (existingCustomers.isEmpty()) {
            throw new RuntimeException("没有找到要删除的客户");
        }

        // 批量删除
        customerRepository.deleteAll(existingCustomers);
        
        return existingCustomers.size();
    }

    /**
     * 检查客户名称是否可用
     *
     * @param customerName 客户名称
     * @param customerId   要排除的客户ID（可为null）
     * @return 是否可用
     */
    @Transactional(readOnly = true)
    public boolean isCustomerNameAvailable(String customerName, Long customerId) {
        if (customerId == null) {
            return !customerRepository.findByCustomerName(customerName).isPresent();
        } else {
            return !customerRepository.existsByCustomerNameAndCustomerIdNot(customerName, customerId);
        }
    }

    /**
     * 检查联系方式是否可用
     *
     * @param phoneNumber 联系方式
     * @param customerId  要排除的客户ID（可为null）
     * @return 是否可用
     */
    @Transactional(readOnly = true)
    public boolean isPhoneNumberAvailable(String phoneNumber, Long customerId) {
        if (customerId == null) {
            return !customerRepository.findByPhoneNumber(phoneNumber).isPresent();
        } else {
            return !customerRepository.existsByPhoneNumberAndCustomerIdNot(phoneNumber, customerId);
        }
    }

    /**
     * 获取所有客户列表
     *
     * @return 客户列表
     */
    @Transactional(readOnly = true)
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    /**
     * 获取所有省份列表
     *
     * @return 省份列表
     */
    @Transactional(readOnly = true)
    public List<String> getAllProvinces() {
        return customerRepository.findDistinctProvinces();
    }

    /**
     * 获取所有客户等级列表
     *
     * @return 客户等级列表
     */
    @Transactional(readOnly = true)
    public List<String> getAllCustomerRanks() {
        return customerRepository.findDistinctCustomerRanks();
    }

    /**
     * 统计客户总数
     *
     * @return 客户总数
     */
    @Transactional(readOnly = true)
    public long getTotalCustomerCount() {
        return customerRepository.count();
    }

    /**
     * 根据省份统计客户数量
     *
     * @param province 省份
     * @return 客户数量
     */
    @Transactional(readOnly = true)
    public long getCustomerCountByProvince(String province) {
        return customerRepository.countByProvince(province);
    }

    /**
     * 根据客户等级统计客户数量
     *
     * @param customerRank 客户等级
     * @return 客户数量
     */
    @Transactional(readOnly = true)
    public long getCustomerCountByRank(String customerRank) {
        return customerRepository.countByCustomerRank(customerRank);
    }
}
