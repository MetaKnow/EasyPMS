package com.missoft.pms.controller;

import com.missoft.pms.entity.*;
import com.missoft.pms.repository.ArchieveSoftRepository;
import com.missoft.pms.repository.StandardConstructStepRepository;
import com.missoft.pms.repository.StandardDeliverableRepository;
import com.missoft.pms.repository.ConstructDeliverableFileRepository;
import com.missoft.pms.service.ConstructMilestoneService;
import com.missoft.pms.service.ConstructingProjectService;
import com.missoft.pms.service.ProjectSstepRelationService;
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
  @Autowired
  private ProjectSstepRelationService projectSstepRelationService;

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

      // 先按产品 systemName 获取标准步骤作为基础列表
      List<StandardConstructStep> steps = (systemName != null && !systemName.isEmpty())
          ? standardConstructStepRepository.findBySystemNameOrderBySstepNameAsc(systemName)
          : standardConstructStepRepository.findAllByOrderBySstepNameAsc();

      // 读取项目-标准步骤关系，并按 sstepId 建立映射
      List<ProjectSstepRelation> relations = projectSstepRelationService.getRelationsByProjectId(projectId);
      Map<Long, ProjectSstepRelation> relBySstepId = new HashMap<>();
      for (ProjectSstepRelation rel : relations) {
        if (rel.getSstepId() != null) {
          relBySstepId.put(rel.getSstepId(), rel);
        }
      }

      // 将标准步骤与关系字段合并，保证无关系数据时仍显示标准步骤
      List<Map<String, Object>> stepViews = new ArrayList<>();
      Set<Long> baseStepIds = new HashSet<>();
      for (StandardConstructStep s : steps) {
        baseStepIds.add(s.getSstepId());
        Map<String, Object> m = new HashMap<>();
        m.put("sstepId", s.getSstepId());
        m.put("sstepName", s.getSstepName());
        m.put("type", s.getType());
        m.put("systemName", s.getSystemName());
        m.put("smilestoneId", s.getSmilestoneId());
        ProjectSstepRelation rel = relBySstepId.get(s.getSstepId());
        if (rel != null) {
          m.put("relationId", rel.getRelationId());
          m.put("projectId", rel.getProjectId());
          m.put("director", rel.getDirector());
          m.put("planStartDate", rel.getPlanStartDate());
          m.put("planEndDate", rel.getPlanEndDate());
          m.put("actualStartDate", rel.getActualStartDate());
          m.put("actualEndDate", rel.getActualEndDate());
          m.put("planPeriod", rel.getPlanPeriod());
          m.put("actualPeriod", rel.getActualPeriod());
        }
        stepViews.add(m);
      }

      // 若存在关系但其 sstepId 不在标准步骤中（例如历史或自定义），也追加展示
      for (ProjectSstepRelation rel : relations) {
        Long sid = rel.getSstepId();
        if (sid == null || baseStepIds.contains(sid)) continue;
        Map<String, Object> m = new HashMap<>();
        m.put("relationId", rel.getRelationId());
        m.put("projectId", rel.getProjectId());
        m.put("sstepId", rel.getSstepId());
        m.put("director", rel.getDirector());
        m.put("planStartDate", rel.getPlanStartDate());
        m.put("planEndDate", rel.getPlanEndDate());
        m.put("actualStartDate", rel.getActualStartDate());
        m.put("actualEndDate", rel.getActualEndDate());
        m.put("planPeriod", rel.getPlanPeriod());
        m.put("actualPeriod", rel.getActualPeriod());
        StandardConstructStep s = standardConstructStepRepository.findById(rel.getSstepId()).orElse(null);
        if (s != null) {
          m.put("sstepName", s.getSstepName());
          m.put("type", s.getType());
          m.put("smilestoneId", s.getSmilestoneId());
          m.put("systemName", s.getSystemName());
        }
        stepViews.add(m);
      }

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
      data.put("steps", stepViews); // 返回组合后的步骤视图
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