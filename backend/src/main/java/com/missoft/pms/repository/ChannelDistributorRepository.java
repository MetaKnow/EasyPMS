package com.missoft.pms.repository;

import com.missoft.pms.entity.ChannelDistributor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 渠道商数据访问层接口
 * 提供渠道商相关的数据库操作方法
 * 
 * @author MissoftPMS
 * @version 1.0.0
 */
@Repository
public interface ChannelDistributorRepository extends JpaRepository<ChannelDistributor, Long> {

    /**
     * 根据渠道名称查找渠道商（模糊查询）
     *
     * @param channelName 渠道名称
     * @param pageable    分页参数
     * @return 渠道商分页列表
     */
    Page<ChannelDistributor> findByChannelNameContainingIgnoreCase(String channelName, Pageable pageable);

    /**
     * 根据联系人查找渠道商（模糊查询）
     *
     * @param contactor 联系人
     * @param pageable  分页参数
     * @return 渠道商分页列表
     */
    Page<ChannelDistributor> findByContactorContainingIgnoreCase(String contactor, Pageable pageable);

    /**
     * 根据联系方式查找渠道商（模糊查询）
     *
     * @param phoneNumber 联系方式
     * @param pageable    分页参数
     * @return 渠道商分页列表
     */
    Page<ChannelDistributor> findByPhoneNumberContainingIgnoreCase(String phoneNumber, Pageable pageable);

    /**
     * 根据渠道名称查找渠道商（精确查询）
     *
     * @param channelName 渠道名称
     * @return 渠道商信息
     */
    Optional<ChannelDistributor> findByChannelName(String channelName);

    /**
     * 检查渠道名称是否已存在（排除指定ID）
     *
     * @param channelName 渠道名称
     * @param channelId   要排除的渠道ID
     * @return 是否存在
     */
    boolean existsByChannelNameAndChannelIdNot(String channelName, Long channelId);

    /**
     * 检查渠道名称是否已存在
     *
     * @param channelName 渠道名称
     * @return 是否存在
     */
    boolean existsByChannelName(String channelName);

    /**
     * 多条件查询渠道商
     *
     * @param channelName 渠道名称（可选）
     * @param contactor   联系人（可选）
     * @param phoneNumber 联系方式（可选）
     * @param pageable    分页参数
     * @return 渠道商分页列表
     */
    @Query("SELECT cd FROM ChannelDistributor cd WHERE " +
           "(:channelName IS NULL OR LOWER(cd.channelName) LIKE LOWER(CONCAT('%', :channelName, '%'))) AND " +
           "(:contactor IS NULL OR LOWER(cd.contactor) LIKE LOWER(CONCAT('%', :contactor, '%'))) AND " +
           "(:phoneNumber IS NULL OR LOWER(cd.phoneNumber) LIKE LOWER(CONCAT('%', :phoneNumber, '%'))) AND " +
           "(:saleDirector IS NULL OR cd.saleDirector = :saleDirector)")
    Page<ChannelDistributor> findByMultipleConditions(
            @Param("channelName") String channelName,
            @Param("contactor") String contactor,
            @Param("phoneNumber") String phoneNumber,
            @Param("saleDirector") Long saleDirector,
            Pageable pageable);

    /**
     * 根据ID列表批量查询渠道商
     *
     * @param channelIds 渠道商ID列表
     * @return 渠道商列表
     */
    List<ChannelDistributor> findByChannelIdIn(List<Long> channelIds);

    /**
     * 根据ID列表批量删除渠道商
     *
     * @param channelIds 渠道商ID列表
     */
    void deleteByChannelIdIn(List<Long> channelIds);

    /**
     * 统计渠道商总数
     *
     * @return 渠道商总数
     */
    @Query("SELECT COUNT(cd) FROM ChannelDistributor cd")
    long countAllChannelDistributors();

    /**
     * 根据创建时间范围查询渠道商
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param pageable  分页参数
     * @return 渠道商分页列表
     */
    @Query("SELECT cd FROM ChannelDistributor cd WHERE cd.createdAt BETWEEN :startDate AND :endDate")
    Page<ChannelDistributor> findByCreatedAtBetween(
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate,
            Pageable pageable);
}
