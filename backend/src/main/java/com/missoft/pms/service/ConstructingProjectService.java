package com.missoft.pms.service;

import com.missoft.pms.dto.ConstructingProjectDTO;
import com.missoft.pms.entity.ConstructingProject;
import com.missoft.pms.repository.ConstructingProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 在建项目服务类
 * 提供在建项目相关的业务逻辑处理
 * 
 * @author MissoftPMS
 * @version 1.0.0
 */
@Service
@Transactional
public class ConstructingProjectService {

    @Autowired
    private ConstructingProjectRepository constructingProjectRepository;

    /**
     * 分页查询在建项目列表
     *
     * @param page          页码（从0开始）
     * @param size          每页大小
     * @param projectName   项目名称（可选）
     * @param year          年度（可选）
     * @param projectState  项目状态（可选）
     * @param projectLeader 项目负责人ID（可选）
     * @param customerId    客户ID（可选）
     * @param sortBy        排序字段
     * @param sortDir       排序方向（asc/desc）
     * @return 项目分页数据
     */
    @Transactional(readOnly = true)
    public Page<ConstructingProject> getConstructingProjects(int page, int size, String projectName,
                                                           Integer year, String projectState, Long projectLeader,
                                                           Long customerId, String sortBy, String sortDir) {
        // 创建排序对象
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        // 处理空字符串为null
        projectName = StringUtils.hasText(projectName) ? projectName.trim() : null;
        projectState = StringUtils.hasText(projectState) ? projectState.trim() : null;

        // 如果所有查询条件都为空，返回所有项目
        if (projectName == null && year == null && projectState == null && 
            projectLeader == null && customerId == null) {
            return constructingProjectRepository.findAll(pageable);
        }

        // 使用多条件查询
        return constructingProjectRepository.findByMultipleConditions(
                projectName, year, projectState, projectLeader, customerId, pageable);
    }

    /**
     * 分页查询在建项目列表（包含客户名称）
     *
     * @param page          页码（从0开始）
     * @param size          每页大小
     * @param projectName   项目名称（可选）
     * @param year          年度（可选）
     * @param projectState  项目状态（可选）
     * @param projectLeader 项目负责人ID（可选）
     * @param customerId    客户ID（可选）
     * @param sortBy        排序字段
     * @param sortDir       排序方向（asc/desc）
     * @return 项目分页数据（包含客户名称）
     */
    @Transactional(readOnly = true)
    public Page<ConstructingProjectDTO> getConstructingProjectsWithCustomerName(int page, int size, String projectName,
                                                                               Integer year, String projectState, Long projectLeader,
                                                                               Long customerId, String sortBy, String sortDir) {
        // 创建排序对象
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        // 处理空字符串为null
        projectName = StringUtils.hasText(projectName) ? projectName.trim() : null;
        projectState = StringUtils.hasText(projectState) ? projectState.trim() : null;

        // 使用多条件查询（包含客户名称）
        return constructingProjectRepository.findByMultipleConditionsWithCustomerName(
                projectName, year, projectState, projectLeader, customerId, pageable);
    }

    /**
     * 根据ID查询在建项目
     *
     * @param projectId 项目ID
     * @return 项目对象
     * @throws RuntimeException 如果项目不存在
     */
    @Transactional(readOnly = true)
    public ConstructingProject getConstructingProjectById(Long projectId) {
        return constructingProjectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("项目不存在，ID: " + projectId));
    }

    /**
     * 创建新项目
     *
     * @param constructingProject 项目对象
     * @return 保存后的项目对象
     * @throws RuntimeException 如果项目编号已存在或数据验证失败
     */
    public ConstructingProject createConstructingProject(ConstructingProject constructingProject) {
        // 验证必填字段
        validateConstructingProject(constructingProject);

        // 检查项目编号是否已存在
        if (StringUtils.hasText(constructingProject.getProjectNum()) && 
            constructingProjectRepository.existsByProjectNum(constructingProject.getProjectNum())) {
            throw new RuntimeException("项目编号已存在: " + constructingProject.getProjectNum());
        }

        // 如果没有提供项目编号，自动生成
        if (!StringUtils.hasText(constructingProject.getProjectNum())) {
            constructingProject.setProjectNum(generateProjectNumber());
        }

        // 设置默认值
        if (constructingProject.getProjectState() == null) {
            constructingProject.setProjectState("待开始");
        }
        if (constructingProject.getIsAgent() == null) {
            constructingProject.setIsAgent(0);
        }
        if (constructingProject.getReceivedMoney() == null) {
            constructingProject.setReceivedMoney(BigDecimal.ZERO);
        }
        if (constructingProject.getUnreceiveMoney() == null) {
            constructingProject.setUnreceiveMoney(BigDecimal.ZERO);
        }

        // 计算未回款金额
        calculateUnreceiveMoney(constructingProject);

        return constructingProjectRepository.save(constructingProject);
    }

    /**
     * 更新项目
     *
     * @param projectId           项目ID
     * @param constructingProject 项目对象
     * @return 更新后的项目对象
     * @throws RuntimeException 如果项目不存在或数据验证失败
     */
    public ConstructingProject updateConstructingProject(Long projectId, ConstructingProject constructingProject) {
        // 检查项目是否存在
        ConstructingProject existingProject = getConstructingProjectById(projectId);

        // 验证必填字段
        validateConstructingProject(constructingProject);

        // 检查项目编号是否与其他项目冲突
        if (StringUtils.hasText(constructingProject.getProjectNum()) && 
            !constructingProject.getProjectNum().equals(existingProject.getProjectNum()) &&
            constructingProjectRepository.existsByProjectNum(constructingProject.getProjectNum())) {
            throw new RuntimeException("项目编号已存在: " + constructingProject.getProjectNum());
        }

        // 更新字段
        if (StringUtils.hasText(constructingProject.getProjectNum())) {
            existingProject.setProjectNum(constructingProject.getProjectNum());
        }
        existingProject.setYear(constructingProject.getYear());
        existingProject.setProjectName(constructingProject.getProjectName());
        existingProject.setProjectType(constructingProject.getProjectType());
        existingProject.setProjectLeader(constructingProject.getProjectLeader());
        existingProject.setSaleLeader(constructingProject.getSaleLeader());
        existingProject.setCustomerId(constructingProject.getCustomerId());
        existingProject.setProjectState(constructingProject.getProjectState());
        existingProject.setSoftId(constructingProject.getSoftId());
        existingProject.setStartDate(constructingProject.getStartDate());
        existingProject.setPlanEndDate(constructingProject.getPlanEndDate());
        existingProject.setActualEndDate(constructingProject.getActualEndDate());
        existingProject.setPlanPeriod(constructingProject.getPlanPeriod());
        existingProject.setActualPeriod(constructingProject.getActualPeriod());
        existingProject.setIsAgent(constructingProject.getIsAgent());
        existingProject.setChannelId(constructingProject.getChannelId());
        existingProject.setValue(constructingProject.getValue());
        existingProject.setReceivedMoney(constructingProject.getReceivedMoney());
        existingProject.setAcceptanceDate(constructingProject.getAcceptanceDate());
        existingProject.setConstructContent(constructingProject.getConstructContent());

        // 计算未回款金额
        calculateUnreceiveMoney(existingProject);

        return constructingProjectRepository.save(existingProject);
    }

    /**
     * 删除项目
     *
     * @param projectId 项目ID
     * @throws RuntimeException 如果项目不存在
     */
    public void deleteConstructingProject(Long projectId) {
        if (!constructingProjectRepository.existsById(projectId)) {
            throw new RuntimeException("项目不存在，ID: " + projectId);
        }
        constructingProjectRepository.deleteById(projectId);
    }

    /**
     * 批量删除项目
     *
     * @param projectIds 项目ID列表
     */
    public void batchDeleteConstructingProjects(List<Long> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            throw new RuntimeException("项目ID列表不能为空");
        }
        
        for (Long projectId : projectIds) {
            if (!constructingProjectRepository.existsById(projectId)) {
                throw new RuntimeException("项目不存在，ID: " + projectId);
            }
        }
        
        constructingProjectRepository.deleteAllById(projectIds);
    }

    /**
     * 验证项目数据
     *
     * @param constructingProject 项目对象
     * @throws RuntimeException 如果验证失败
     */
    private void validateConstructingProject(ConstructingProject constructingProject) {
        if (constructingProject == null) {
            throw new RuntimeException("项目信息不能为空");
        }

        if (!StringUtils.hasText(constructingProject.getProjectName())) {
            throw new RuntimeException("项目名称不能为空");
        }

        if (!StringUtils.hasText(constructingProject.getProjectType())) {
            throw new RuntimeException("项目类型不能为空");
        }

        if (constructingProject.getYear() == null) {
            throw new RuntimeException("年度不能为空");
        }

        // 验证年度范围
        int currentYear = LocalDate.now().getYear();
        if (constructingProject.getYear() < 2000 || constructingProject.getYear() > currentYear + 10) {
            throw new RuntimeException("年度必须在2000年到" + (currentYear + 10) + "年之间");
        }

        // 验证金额字段
        if (constructingProject.getValue() != null && constructingProject.getValue().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("项目金额不能为负数");
        }

        if (constructingProject.getReceivedMoney() != null && constructingProject.getReceivedMoney().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("已回款金额不能为负数");
        }

        // 验证渠道项目信息
        if (constructingProject.getIsAgent() != null && constructingProject.getIsAgent() == 1) {
            if (constructingProject.getChannelId() == null) {
                throw new RuntimeException("渠道项目必须选择渠道名称");
            }
        }
    }

    /**
     * 生成项目编号
     * 格式：MS-YYYYMMDDHHMMSS
     *
     * @return 项目编号
     */
    private String generateProjectNumber() {
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String dateStr = now.format(formatter);
        
        // 获取当天已有项目数量，用于生成序号
        long count = constructingProjectRepository.countByYear(now.getYear());
        String sequence = String.format("%04d", count + 1);
        
        return "MS-" + dateStr + sequence;
    }

    /**
     * 计算未回款金额
     *
     * @param constructingProject 项目对象
     */
    private void calculateUnreceiveMoney(ConstructingProject constructingProject) {
        if (constructingProject.getValue() != null && constructingProject.getReceivedMoney() != null) {
            BigDecimal unreceiveMoney = constructingProject.getValue().subtract(constructingProject.getReceivedMoney());
            constructingProject.setUnreceiveMoney(unreceiveMoney.max(BigDecimal.ZERO));
        } else if (constructingProject.getValue() != null) {
            constructingProject.setUnreceiveMoney(constructingProject.getValue());
        } else {
            constructingProject.setUnreceiveMoney(BigDecimal.ZERO);
        }
    }

    /**
     * 获取所有项目（不分页）
     *
     * @return 项目列表
     */
    @Transactional(readOnly = true)
    public List<ConstructingProject> getAllConstructingProjects() {
        return constructingProjectRepository.findAll();
    }

    /**
     * 根据年度统计项目数量
     *
     * @param year 年度
     * @return 项目数量
     */
    @Transactional(readOnly = true)
    public long countByYear(Integer year) {
        return constructingProjectRepository.countByYear(year);
    }

    /**
     * 根据项目状态统计项目数量
     *
     * @param projectState 项目状态
     * @return 项目数量
     */
    @Transactional(readOnly = true)
    public long countByProjectState(String projectState) {
        return constructingProjectRepository.countByProjectState(projectState);
    }
}