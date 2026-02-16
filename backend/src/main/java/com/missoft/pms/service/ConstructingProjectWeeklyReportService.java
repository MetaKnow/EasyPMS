package com.missoft.pms.service;

import com.missoft.pms.entity.ConstructingProjectWeeklyReport;
import com.missoft.pms.repository.ConstructingProjectWeeklyReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 类级注释：项目周报服务
 */
@Service
@Transactional
public class ConstructingProjectWeeklyReportService {

    @Autowired
    private ConstructingProjectWeeklyReportRepository weeklyReportRepository;

    @Autowired
    private ConstructingProjectWeeklyReportFileService weeklyReportFileService;

    /**
     * 函数级注释：创建项目周报
     */
    public ConstructingProjectWeeklyReport create(ConstructingProjectWeeklyReport payload) {
        if (payload == null || payload.getProjectId() == null) {
            throw new IllegalArgumentException("projectId 不能为空");
        }
        if (payload.getPeriod() == null || payload.getPeriod().trim().isEmpty()) {
            throw new IllegalArgumentException("周期不能为空");
        }
        if (payload.getSubmitUser() == null) {
            throw new IllegalArgumentException("提交用户不能为空");
        }
        if (payload.getSubmitDate() == null) {
            payload.setSubmitDate(LocalDate.now());
        }
        if (payload.getWeeklyWorkload() == null) {
            throw new IllegalArgumentException("本周工作量不能为空");
        }
        return weeklyReportRepository.save(payload);
    }

    /**
     * 函数级注释：按项目ID加载周报列表并标记附件状态
     */
    @Transactional(readOnly = true)
    public List<ConstructingProjectWeeklyReport> listByProjectId(Long projectId) {
        if (projectId == null) return List.of();
        List<ConstructingProjectWeeklyReport> list = weeklyReportRepository.findByProjectId(projectId);
        for (ConstructingProjectWeeklyReport r : list) {
            if (r != null && r.getWeeklyReportId() != null) {
                r.setHasFiles(weeklyReportFileService.hasFiles(r.getWeeklyReportId()));
            }
        }
        return list;
    }

    /**
     * 函数级注释：更新项目周报
     */
    public ConstructingProjectWeeklyReport update(Long id, ConstructingProjectWeeklyReport payload) {
        ConstructingProjectWeeklyReport existing = weeklyReportRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("未找到指定的项目周报"));

        if (payload.getPeriod() != null && !payload.getPeriod().trim().isEmpty()) {
            existing.setPeriod(payload.getPeriod().trim());
        }
        if (payload.getSubmitUser() != null) {
            existing.setSubmitUser(payload.getSubmitUser());
        }
        if (payload.getSubmitDate() != null) {
            existing.setSubmitDate(payload.getSubmitDate());
        }
        if (payload.getWeeklyWorkload() == null) {
            throw new IllegalArgumentException("本周工作量不能为空");
        }
        existing.setWeeklyWorkload(payload.getWeeklyWorkload());
        if (payload.getWorkDifficulties() != null) {
            existing.setWorkDifficulties(payload.getWorkDifficulties());
        }
        return weeklyReportRepository.save(existing);
    }

    /**
     * 函数级注释：删除项目周报并清理附件
     */
    public void delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("周报ID不能为空");
        }
        ConstructingProjectWeeklyReport existing = weeklyReportRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("未找到指定的项目周报"));
        weeklyReportFileService.deleteFilesByWeeklyReportId(id);
        weeklyReportRepository.delete(existing);
    }
}
