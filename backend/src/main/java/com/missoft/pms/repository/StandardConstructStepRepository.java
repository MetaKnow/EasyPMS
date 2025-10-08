package com.missoft.pms.repository;

import com.missoft.pms.entity.StandardConstructStep;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 标准交付步骤仓库接口
 * 提供对standard_construct_step表的数据库操作
 */
@Repository
public interface StandardConstructStepRepository extends JpaRepository<StandardConstructStep, Long> {

    /**
     * 根据步骤名称查询步骤（分页）
     *
     * @param sstepName 步骤名称
     * @param pageable  分页参数
     * @return 步骤分页数据
     */
    Page<StandardConstructStep> findBySstepNameContainingIgnoreCase(String sstepName, Pageable pageable);

    /**
     * 根据档案系统名称查询步骤（分页）
     *
     * @param systemName 档案系统名称
     * @param pageable   分页参数
     * @return 步骤分页数据
     */
    Page<StandardConstructStep> findBySystemNameContainingIgnoreCase(String systemName, Pageable pageable);

    /**
     * 根据步骤类型查询步骤（分页）
     *
     * @param type     步骤类型
     * @param pageable 分页参数
     * @return 步骤分页数据
     */
    Page<StandardConstructStep> findByType(String type, Pageable pageable);

    /**
     * 根据标准里程碑ID查询步骤（分页）
     *
     * @param smilestoneId 标准里程碑ID
     * @param pageable     分页参数
     * @return 步骤分页数据
     */
    Page<StandardConstructStep> findBySmilestoneId(Long smilestoneId, Pageable pageable);

    /**
     * 多条件查询步骤
     *
     * @param sstepName    步骤名称（可选）
     * @param systemName   档案系统名称（可选）
     * @param type         步骤类型（可选）
     * @param smilestoneId 标准里程碑ID（可选）
     * @param pageable     分页参数
     * @return 步骤分页数据
     */
    @Query("SELECT s FROM StandardConstructStep s WHERE " +
           "(:sstepName IS NULL OR LOWER(s.sstepName) LIKE LOWER(CONCAT('%', :sstepName, '%'))) AND " +
           "(:systemName IS NULL OR LOWER(s.systemName) LIKE LOWER(CONCAT('%', :systemName, '%'))) AND " +
           "(:type IS NULL OR s.type = :type) AND " +
           "(:smilestoneId IS NULL OR s.smilestoneId = :smilestoneId)")
    Page<StandardConstructStep> findByMultipleConditions(
            @Param("sstepName") String sstepName,
            @Param("systemName") String systemName,
            @Param("type") String type,
            @Param("smilestoneId") Long smilestoneId,
            Pageable pageable);



    /**
     * 根据档案系统名称查询所有步骤（按步骤名称排序）
     *
     * @param systemName 档案系统名称
     * @return 步骤列表
     */
    List<StandardConstructStep> findBySystemNameOrderBySstepNameAsc(String systemName);

    /**
     * 获取所有步骤列表（按步骤名称排序）
     *
     * @return 步骤列表
     */
    List<StandardConstructStep> findAllByOrderBySstepNameAsc();

    /**
     * 获取所有不重复的档案系统名称列表（按名称排序）
     *
     * @return 档案系统名称列表
     */
    @Query("SELECT DISTINCT s.systemName FROM StandardConstructStep s WHERE s.systemName IS NOT NULL ORDER BY s.systemName ASC")
    List<String> findDistinctSystemNames();

    /**
     * 根据标准里程碑ID查询所有步骤
     *
     * @param smilestoneId 标准里程碑ID
     * @return 步骤列表
     */
    List<StandardConstructStep> findBySmilestoneId(Long smilestoneId);

    /**
     * 根据步骤类型查询所有步骤
     *
     * @param type 步骤类型
     * @return 步骤列表
     */
    List<StandardConstructStep> findByType(String type);
}