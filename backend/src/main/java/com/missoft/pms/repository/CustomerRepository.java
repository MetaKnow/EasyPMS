package com.missoft.pms.repository;

import com.missoft.pms.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 客户数据访问层接口
 * 提供客户相关的数据库操作方法
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /**
     * 根据客户名称查找客户（模糊查询）
     *
     * @param customerName 客户名称
     * @param pageable     分页参数
     * @return 客户分页列表
     */
    Page<Customer> findByCustomerNameContainingIgnoreCase(String customerName, Pageable pageable);

    /**
     * 根据联系人查找客户（模糊查询）
     *
     * @param contact  联系人
     * @param pageable 分页参数
     * @return 客户分页列表
     */
    Page<Customer> findByContactContainingIgnoreCase(String contact, Pageable pageable);

    /**
     * 根据省份查找客户
     *
     * @param province 省份
     * @param pageable 分页参数
     * @return 客户分页列表
     */
    Page<Customer> findByProvince(String province, Pageable pageable);

    /**
     * 根据客户等级查找客户
     *
     * @param customerRank 客户等级
     * @param pageable     分页参数
     * @return 客户分页列表
     */
    Page<Customer> findByCustomerRank(String customerRank, Pageable pageable);

    /**
     * 根据客户名称查找客户（精确匹配）
     *
     * @param customerName 客户名称
     * @return 客户对象
     */
    Optional<Customer> findByCustomerName(String customerName);

    /**
     * 根据联系方式查找客户
     *
     * @param phoneNumber 联系方式
     * @return 客户对象
     */
    Optional<Customer> findByPhoneNumber(String phoneNumber);

    /**
     * 检查客户名称是否已存在（排除指定ID）
     *
     * @param customerName 客户名称
     * @param customerId   要排除的客户ID
     * @return 是否存在
     */
    boolean existsByCustomerNameAndCustomerIdNot(String customerName, Long customerId);

    /**
     * 检查联系方式是否已存在（排除指定ID）
     *
     * @param phoneNumber 联系方式
     * @param customerId  要排除的客户ID
     * @return 是否存在
     */
    boolean existsByPhoneNumberAndCustomerIdNot(String phoneNumber, Long customerId);

    /**
     * 多条件查询客户（支持客户名称、联系人、省份、客户等级的组合查询）
     *
     * @param customerName 客户名称（可为空）
     * @param contact      联系人（可为空）
     * @param province     省份（可为空）
     * @param customerRank 客户等级（可为空）
     * @param pageable     分页参数
     * @return 客户分页列表
     */
    /**
     * 函数级注释：多条件查询客户（支持销售负责人过滤）
     * - 当 saleLeader 不为 null 时，仅返回销售负责人为该用户的客户数据
     */
    @Query("SELECT c FROM Customer c WHERE " +
            "(:customerName IS NULL OR LOWER(c.customerName) LIKE LOWER(CONCAT('%', :customerName, '%'))) AND " +
            "(:contact IS NULL OR LOWER(c.contact) LIKE LOWER(CONCAT('%', :contact, '%'))) AND " +
            "(:province IS NULL OR c.province = :province) AND " +
            "(:customerRank IS NULL OR c.customerRank = :customerRank) AND " +
            "(:saleLeader IS NULL OR c.saleLeader = :saleLeader)")
    Page<Customer> findByMultipleConditions(
            @Param("customerName") String customerName,
            @Param("contact") String contact,
            @Param("province") String province,
            @Param("customerRank") String customerRank,
            @Param("saleLeader") Long saleLeader,
            Pageable pageable
    );

    /**
     * 获取所有省份列表（去重）
     *
     * @return 省份列表
     */
    @Query("SELECT DISTINCT c.province FROM Customer c ORDER BY c.province")
    List<String> findDistinctProvinces();

    /**
     * 获取所有客户等级列表（去重）
     *
     * @return 客户等级列表
     */
    @Query("SELECT DISTINCT c.customerRank FROM Customer c ORDER BY c.customerRank")
    List<String> findDistinctCustomerRanks();

    /**
     * 统计指定省份的客户数量
     *
     * @param province 省份
     * @return 客户数量
     */
    long countByProvince(String province);

    /**
     * 统计指定客户等级的客户数量
     *
     * @param customerRank 客户等级
     * @return 客户数量
     */
    long countByCustomerRank(String customerRank);

    /**
     * 根据客户ID列表批量查询客户
     *
     * @param customerIds 客户ID列表
     * @return 客户列表
     */
    List<Customer> findByCustomerIdIn(List<Long> customerIds);
}
