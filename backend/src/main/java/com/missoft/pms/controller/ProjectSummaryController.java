package com.missoft.pms.controller;

import com.missoft.pms.entity.*;
import com.missoft.pms.repository.ArchieveSoftRepository;
import com.missoft.pms.repository.StandardConstructStepRepository;
import com.missoft.pms.repository.StandardDeliverableRepository;
import com.missoft.pms.repository.ConstructDeliverableFileRepository;
import com.missoft.pms.service.ConstructMilestoneService;
import com.missoft.pms.service.ConstructingProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 项目汇总接口：返回指定项目的步骤、里程碑、交付物与文件信息
 */
@RestController
@RequestMapping("/api/projects")
public class ProjectSummaryController {

    @Autowired
    private ConstructingProjectService constructingProjectService;
    @Autowired
    private ArchieveSoftRepository archieveSoftRepository;
    @Autowired
    private StandardConstructStepRepository standardConstructStepRepository;
    @Autowired
    private StandardDeliverableRepository standardDeliverableRepository;
    @Autowired
    private ConstructDeliverableFileRepository constructDeliverableFileRepository;
    @Autowired
    private ConstructMilestoneService constructMilestoneService;

    @GetMapping("/{projectId}/summary")
    public ResponseEntity<Map<String, Object>> getProjectSummary(@PathVariable Long projectId) {
        try {
            ConstructingProject project = constructingProjectService.getConstructingProjectById(projectId);

            String systemName = null;
            if (project.getSoftId() != null) {
                ArchieveSoft soft = archieveSoftRepository.findById(project.getSoftId()).orElse(null);
                if (soft != null && soft.getSoftName() != null) {
                    systemName = soft.getSoftName().trim();
                }
            }

            // 步骤（按产品 systemName 获取标准步骤）
            List<StandardConstructStep> steps = (systemName != null && !systemName.isEmpty())
                    ? standardConstructStepRepository.findBySystemNameOrderBySstepNameAsc(systemName)
                    : standardConstructStepRepository.findAllByOrderBySstepNameAsc();

            // 里程碑（项目专属）
            List<ConstructMilestone> milestones = constructMilestoneService.getMilestonesByProjectId(projectId);

            // 交付物（按产品 systemName 获取标准交付物）
            List<StandardDeliverable> deliverables = (systemName != null && !systemName.isEmpty())
                    ? standardDeliverableRepository.findBySystemNameOrderByDeliverableNameAsc(systemName)
                    : standardDeliverableRepository.findAllByOrderByDeliverableNameAsc();

            // 项目交付文件（实际上传）
            List<ConstructDeliverableFile> files = constructDeliverableFileRepository.findByProjectId(projectId);

            Map<String, Object> data = new HashMap<>();
            data.put("project", project);
            data.put("steps", steps);
            data.put("milestones", milestones);
            data.put("deliverables", deliverables);
            data.put("files", files);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "查询成功");
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "项目不存在");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "查询项目汇总失败");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}