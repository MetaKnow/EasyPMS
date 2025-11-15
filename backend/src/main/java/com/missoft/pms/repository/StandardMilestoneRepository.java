package com.missoft.pms.repository;

import com.missoft.pms.entity.StandardMilestone;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 标准里程碑仓库接口
 * 提供对standard_milestone表的数据库操作
 */
@Repository
public interface StandardMilestoneRepository extends JpaRepository<StandardMilestone, Long> {

    /**
     * 根据里程碑名称查询里程碑（分页）
     *
     * @param milestoneName 里程碑名称
     * @param pageable      分页参数
     * @return 里程碑分页数据
     */
    Page<StandardMilestone> findByMilestoneNameContainingIgnoreCase(String milestoneName, Pageable pageable);

    /**
     * 多条件查询里程碑
     *
     * @param milestoneName 里程碑名称（可选）
     * @param pageable      分页参数
     * @return 里程碑分页数据
     */
    @Query("SELECT s FROM StandardMilestone s WHERE " +
           "(:milestoneName IS NULL OR LOWER(s.milestoneName) LIKE LOWER(CONCAT('%', :milestoneName, '%')))")
    Page<StandardMilestone> findByMultipleConditions(
            @Param("milestoneName") String milestoneName,
            Pageable pageable);

    /**
     * 检查里程碑名称是否已存在（不区分大小写）
     *
     * @param milestoneName 里程碑名称
     * @return 是否存在
     */
    boolean existsByMilestoneNameIgnoreCase(String milestoneName);

    /**
     * 检查里程碑名称是否已存在（排除指定ID，不区分大小写）
     *
     * @param milestoneName 里程碑名称
     * @param excludeId     排除的ID
     * @return 是否存在
     */
    boolean existsByMilestoneNameIgnoreCaseAndMilestoneIdNot(String milestoneName, Long excludeId);

    /**
     * 获取所有里程碑列表（按创建时间排序）
     *
     * @return 里程碑列表
     */
    List<StandardMilestone> findAllByOrderByCreateTimeAsc();

    /**
     * 根据里程碑名称（不区分大小写）精确查询单个里程碑
     * @param milestoneName 里程碑名称
     * @return 里程碑（可能为空）
     */
    java.util.Optional<StandardMilestone> findByMilestoneNameIgnoreCase(String milestoneName);
}