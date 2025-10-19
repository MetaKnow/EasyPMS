package com.missoft.pms.repository;

import com.missoft.pms.entity.StandardDeliverable;
import com.missoft.pms.dto.StandardDeliverableWithNamesDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 标准交付物仓库接口
 * 提供对standard_deliverable表的数据库操作
 */
@Repository
public interface StandardDeliverableRepository extends JpaRepository<StandardDeliverable, Long> {

    /**
     * 根据交付物名称查询交付物（分页）
     *
     * @param deliverableName 交付物名称
     * @param pageable        分页参数
     * @return 交付物分页数据
     */
    Page<StandardDeliverable> findByDeliverableNameContainingIgnoreCase(String deliverableName, Pageable pageable);

    /**
     * 根据系统名称查询交付物（分页）
     *
     * @param systemName 系统名称
     * @param pageable   分页参数
     * @return 交付物分页数据
     */
    Page<StandardDeliverable> findBySystemNameContainingIgnoreCase(String systemName, Pageable pageable);

    /**
     * 根据交付物类型查询交付物（分页）
     *
     * @param deliverableType 交付物类型
     * @param pageable        分页参数
     * @return 交付物分页数据
     */
    Page<StandardDeliverable> findByDeliverableType(String deliverableType, Pageable pageable);

    /**
     * 根据标准步骤ID查询交付物（分页）
     *
     * @param sstepId  标准步骤ID
     * @param pageable 分页参数
     * @return 交付物分页数据
     */
    Page<StandardDeliverable> findBySstepId(Long sstepId, Pageable pageable);

    /**
     * 根据里程碑ID查询交付物（分页）
     *
     * @param milestoneId 里程碑ID
     * @param pageable    分页参数
     * @return 交付物分页数据
     */
    Page<StandardDeliverable> findByMilestoneId(Long milestoneId, Pageable pageable);

    /**
     * 多条件查询交付物
     *
     * @param deliverableName 交付物名称（可选）
     * @param systemName      系统名称（可选）
     * @param deliverableType 交付物类型（可选）
     * @param sstepId         标准步骤ID（可选）
     * @param milestoneId     里程碑ID（可选）
     * @param pageable        分页参数
     * @return 交付物分页数据
     */
    @Query("SELECT d FROM StandardDeliverable d WHERE " +
           "(:deliverableName IS NULL OR LOWER(d.deliverableName) LIKE LOWER(CONCAT('%', :deliverableName, '%'))) AND " +
           "(:systemName IS NULL OR LOWER(d.systemName) LIKE LOWER(CONCAT('%', :systemName, '%'))) AND " +
           "(:deliverableType IS NULL OR d.deliverableType = :deliverableType) AND " +
           "(:sstepId IS NULL OR d.sstepId = :sstepId) AND " +
           "(:milestoneId IS NULL OR d.milestoneId = :milestoneId)")
    Page<StandardDeliverable> findByMultipleConditions(
            @Param("deliverableName") String deliverableName,
            @Param("systemName") String systemName,
            @Param("deliverableType") String deliverableType,
            @Param("sstepId") Long sstepId,
            @Param("milestoneId") Long milestoneId,
            Pageable pageable);

    /**
     * 根据系统名称查询所有交付物（按交付物名称排序）
     *
     * @param systemName 系统名称
     * @return 交付物列表
     */
    List<StandardDeliverable> findBySystemNameOrderByDeliverableNameAsc(String systemName);

    /**
     * 根据系统名称和交付物类型查询交付物
     *
     * @param systemName      系统名称
     * @param deliverableType 交付物类型
     * @return 交付物列表
     */
    List<StandardDeliverable> findBySystemNameAndDeliverableType(String systemName, String deliverableType);

    /**
     * 根据标准步骤ID查询所有交付物
     *
     * @param sstepId 标准步骤ID
     * @return 交付物列表
     */
    List<StandardDeliverable> findBySstepId(Long sstepId);

    /**
     * 根据里程碑ID查询所有交付物
     *
     * @param milestoneId 里程碑ID
     * @return 交付物列表
     */
    List<StandardDeliverable> findByMilestoneId(Long milestoneId);

    /**
     * 获取所有交付物列表（按交付物名称排序）
     *
     * @return 交付物列表
     */
    List<StandardDeliverable> findAllByOrderByDeliverableNameAsc();

    /**
     * 获取所有不重复的系统名称列表（按名称排序）
     *
     * @return 系统名称列表
     */
    @Query("SELECT DISTINCT d.systemName FROM StandardDeliverable d WHERE d.systemName IS NOT NULL ORDER BY d.systemName ASC")
    List<String> findDistinctSystemNames();

    /**
     * 检查交付物名称是否存在（忽略大小写）
     *
     * @param deliverableName 交付物名称
     * @return 是否存在
     */
    boolean existsByDeliverableNameIgnoreCase(String deliverableName);

    /**
     * 检查交付物名称是否存在（忽略大小写，排除指定ID）
     *
     * @param deliverableName 交付物名称
     * @param deliverableId   要排除的交付物ID
     * @return 是否存在
     */
    boolean existsByDeliverableNameIgnoreCaseAndDeliverableIdNot(String deliverableName, Long deliverableId);

    /**
     * 根据系统名称和交付物名称查询交付物
     *
     * @param systemName      系统名称
     * @param deliverableName 交付物名称
     * @return 交付物列表
     */
    List<StandardDeliverable> findBySystemNameAndDeliverableNameContainingIgnoreCase(String systemName, String deliverableName);

    /**
     * 查询标准交付物及其关联的里程碑名称和步骤名称（分页）
     *
     * @param deliverableName 交付物名称（可选）
     * @param systemName      系统名称（可选）
     * @param deliverableType 交付物类型（可选）
     * @param sstepId         标准步骤ID（可选）
     * @param milestoneId     里程碑ID（可选）
     * @param pageable        分页参数
     * @return 包含名称的交付物分页数据
     */
    @Query("SELECT new com.missoft.pms.dto.StandardDeliverableWithNamesDTO(" +
           "d.deliverableId, d.deliverableName, d.systemName, d.deliverableType, " +
           "d.isMustLoad, d.sstepId, d.milestoneId, " +
           "COALESCE(s.sstepName, ''), " +
           "CASE WHEN d.deliverableType = '步骤交付物' THEN COALESCE(sm.milestoneName, '') " +
           "     WHEN d.deliverableType = '里程碑交付物' THEN COALESCE(m.milestoneName, '') " +
           "     ELSE '' END, " +
           "d.createTime, d.updateTime) " +
           "FROM StandardDeliverable d " +
           "LEFT JOIN StandardConstructStep s ON d.sstepId = s.sstepId " +
           "LEFT JOIN StandardMilestone sm ON s.smilestoneId = sm.milestoneId " +
           "LEFT JOIN StandardMilestone m ON d.milestoneId = m.milestoneId " +
           "WHERE (:deliverableName IS NULL OR LOWER(d.deliverableName) LIKE LOWER(CONCAT('%', :deliverableName, '%'))) AND " +
           "(:systemName IS NULL OR LOWER(d.systemName) LIKE LOWER(CONCAT('%', :systemName, '%'))) AND " +
           "(:deliverableType IS NULL OR d.deliverableType = :deliverableType) AND " +
           "(:sstepId IS NULL OR d.sstepId = :sstepId) AND " +
           "(:milestoneId IS NULL OR d.milestoneId = :milestoneId) " +
           "ORDER BY " +
           "CASE WHEN d.deliverableType = '步骤交付物' THEN COALESCE(sm.milestoneName, '') " +
           "     WHEN d.deliverableType = '里程碑交付物' THEN COALESCE(m.milestoneName, '') " +
           "     ELSE '' END ASC, " +
           "CASE WHEN d.deliverableType = '里程碑交付物' THEN 1 ELSE 0 END ASC, " +
           "CASE WHEN d.deliverableType = '步骤交付物' THEN COALESCE(s.sstepName, '') ELSE '' END ASC, " +
           "CASE WHEN d.deliverableType = '里程碑交付物' THEN COALESCE(d.deliverableName, '') ELSE '' END ASC")
    Page<StandardDeliverableWithNamesDTO> findStandardDeliverablesWithNames(
            @Param("deliverableName") String deliverableName,
            @Param("systemName") String systemName,
            @Param("deliverableType") String deliverableType,
            @Param("sstepId") Long sstepId,
            @Param("milestoneId") Long milestoneId,
            Pageable pageable);
}