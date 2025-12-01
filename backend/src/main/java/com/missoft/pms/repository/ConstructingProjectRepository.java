package com.missoft.pms.repository;

import com.missoft.pms.entity.ConstructingProject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 在建项目数据访问层接口
 * 提供在建项目相关的数据库操作方法
 * 
 * @author MissoftPMS
 * @version 1.0.0
 */
@Repository
public interface ConstructingProjectRepository extends JpaRepository<ConstructingProject, Long> {

    /**
     * 根据项目编号查找项目
     *
     * @param projectNum 项目编号
     * @return 项目信息
     */
    Optional<ConstructingProject> findByProjectNum(String projectNum);

    /**
     * 根据项目名称查找项目（模糊查询）
     *
     * @param projectName 项目名称
     * @param pageable    分页参数
     * @return 项目分页列表
     */
    Page<ConstructingProject> findByProjectNameContainingIgnoreCase(String projectName, Pageable pageable);

    /**
     * 根据年度查找项目
     *
     * @param year     年度
     * @param pageable 分页参数
     * @return 项目分页列表
     */
    Page<ConstructingProject> findByYear(Integer year, Pageable pageable);

    /**
     * 根据项目状态查找项目
     *
     * @param projectState 项目状态
     * @param pageable     分页参数
     * @return 项目分页列表
     */
    Page<ConstructingProject> findByProjectState(String projectState, Pageable pageable);

    /**
     * 根据项目负责人查找项目
     *
     * @param projectLeader 项目负责人ID
     * @param pageable      分页参数
     * @return 项目分页列表
     */
    Page<ConstructingProject> findByProjectLeader(Long projectLeader, Pageable pageable);

    /**
     * 根据客户ID查找项目
     *
     * @param customerId 客户ID
     * @param pageable   分页参数
     * @return 项目分页列表
     */
    Page<ConstructingProject> findByCustomerId(Long customerId, Pageable pageable);

    /**
     * 多条件查询项目
     *
     * @param projectName       项目名称（可选）
     * @param year              年度（可选）
     * @param projectState      项目状态（可选）
     * @param projectLeader     项目负责人ID（可选）
     * @param customerId        客户ID（可选）
     * @param pageable          分页参数
     * @return 项目分页列表
     */
    @Query("SELECT cp FROM ConstructingProject cp WHERE " +
           "(:projectName IS NULL OR LOWER(cp.projectName) LIKE LOWER(CONCAT('%', :projectName, '%'))) AND " +
           "(:year IS NULL OR cp.year = :year) AND " +
           "(:projectState IS NULL OR cp.projectState = :projectState) AND " +
           "(:projectLeader IS NULL OR cp.projectLeader = :projectLeader) AND " +
           "(:customerId IS NULL OR cp.customerId = :customerId)")
    Page<ConstructingProject> findByMultipleConditions(
            @Param("projectName") String projectName,
            @Param("year") Integer year,
            @Param("projectState") String projectState,
            @Param("projectLeader") Long projectLeader,
            @Param("customerId") Long customerId,
            Pageable pageable);

    /**
     * 多条件查询项目（包含客户名称）
     *
     * @param projectName       项目名称（可选）
     * @param year              年度（可选）
     * @param projectState      项目状态（可选）
     * @param projectLeader     项目负责人ID（可选）
     * @param customerId        客户ID（可选）
     * @param pageable          分页参数
     * @return 项目分页列表（包含客户名称）
     */
    @Query("SELECT new com.missoft.pms.dto.ConstructingProjectDTO(" +
           "cp.projectId, cp.projectNum, cp.projectName, cp.projectType, cp.projectState, " +
           "cp.projectLeader, COALESCE(u.name, u.userName, ''), cp.startDate, cp.planEndDate, cp.value, cp.year, " +
           "cp.customerId, COALESCE(c.customerName, ''), cp.softId, COALESCE(a.softName, ''), COALESCE(a.softVersion, ''), cp.isCommitAfterSale) " +
           "FROM ConstructingProject cp " +
           "LEFT JOIN Customer c ON cp.customerId = c.customerId " +
           "LEFT JOIN User u ON cp.projectLeader = u.userId " +
           "LEFT JOIN ArchieveSoft a ON cp.softId = a.softId " +
           "WHERE (:projectName IS NULL OR LOWER(cp.projectName) LIKE LOWER(CONCAT('%', :projectName, '%'))) AND " +
           "(:year IS NULL OR cp.year = :year) AND " +
           "(:projectState IS NULL OR cp.projectState = :projectState) AND " +
           "(:projectLeader IS NULL OR cp.projectLeader = :projectLeader) AND " +
           "(:customerId IS NULL OR cp.customerId = :customerId)")
    Page<com.missoft.pms.dto.ConstructingProjectDTO> findByMultipleConditionsWithCustomerName(
            @Param("projectName") String projectName,
            @Param("year") Integer year,
            @Param("projectState") String projectState,
            @Param("projectLeader") Long projectLeader,
            @Param("customerId") Long customerId,
            Pageable pageable);

    /**
     * 根据是否渠道项目查找项目
     *
     * @param isAgent  是否渠道项目（0-否，1-是）
     * @param pageable 分页参数
     * @return 项目分页列表
     */
    Page<ConstructingProject> findByIsAgent(Integer isAgent, Pageable pageable);

    /**
     * 根据渠道ID查找项目
     *
     * @param channelId 渠道ID
     * @param pageable  分页参数
     * @return 项目分页列表
     */
    Page<ConstructingProject> findByChannelId(Long channelId, Pageable pageable);

    /**
     * 检查项目编号是否已存在
     *
     * @param projectNum 项目编号
     * @return 是否存在
     */
    boolean existsByProjectNum(String projectNum);

    /**
     * 根据年度统计项目数量
     *
     * @param year 年度
     * @return 项目数量
     */
    long countByYear(Integer year);

    /**
     * 根据项目状态统计项目数量
     *
     * @param projectState 项目状态
     * @return 项目数量
     */
    long countByProjectState(String projectState);
}