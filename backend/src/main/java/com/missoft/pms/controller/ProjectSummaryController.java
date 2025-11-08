package com.missoft.pms.controller;

import com.missoft.pms.entity.*;
import com.missoft.pms.repository.ArchieveSoftRepository;
import com.missoft.pms.repository.StandardConstructStepRepository;
import com.missoft.pms.repository.StandardDeliverableRepository;
import com.missoft.pms.repository.ConstructDeliverableFileRepository;
import com.missoft.pms.repository.UserRepository;
import com.missoft.pms.service.ConstructMilestoneService;
import com.missoft.pms.service.ConstructingProjectService;
import com.missoft.pms.service.ProjectSstepRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

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
  @Autowired
  private UserRepository userRepository;

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

      // 读取项目-标准步骤关系，并按 sstepId 建立映射（支持同一标准步骤的多条关系，例如接口开发）
      List<ProjectSstepRelation> relations = projectSstepRelationService.getRelationsByProjectId(projectId);
      Map<Long, List<ProjectSstepRelation>> relListBySstepId = new HashMap<>();
      for (ProjectSstepRelation rel : relations) {
        Long sid = rel.getSstepId();
        if (sid == null) continue;
        relListBySstepId.computeIfAbsent(sid, k -> new ArrayList<>()).add(rel);
      }

      // 预取负责人姓名映射，便于前端直接显示姓名
      Set<Long> directorIds = relations.stream()
              .map(ProjectSstepRelation::getDirector)
              .filter(Objects::nonNull)
              .collect(Collectors.toSet());
      Map<Long, String> directorNameMap = new HashMap<>();
      if (!directorIds.isEmpty()) {
        List<User> users = userRepository.findAllById(directorIds);
        for (User u : users) {
          String nm = (u.getName() != null && !u.getName().trim().isEmpty()) ? u.getName().trim() : u.getUserName();
          directorNameMap.put(u.getUserId(), nm);
        }
      }

      // 将标准步骤与关系字段合并，保证无关系数据时仍显示标准步骤
      List<Map<String, Object>> stepViews = new ArrayList<>();
      Set<Long> baseStepIds = new HashSet<>();
      for (StandardConstructStep s : steps) {
        baseStepIds.add(s.getSstepId());
        List<ProjectSstepRelation> relList = relListBySstepId.get(s.getSstepId());
        // 若存在关系
        if (relList != null && !relList.isEmpty()) {
          // 接口开发：为每条关系单独生成一个视图，避免丢失其他接口的步骤
          if ("接口开发".equals(s.getType())) {
            for (ProjectSstepRelation rel : relList) {
              Map<String, Object> m = new HashMap<>();
              m.put("relationId", rel.getRelationId());
              m.put("projectId", rel.getProjectId());
              m.put("sstepId", s.getSstepId());
              m.put("sstepName", s.getSstepName());
              m.put("type", s.getType());
              m.put("systemName", s.getSystemName());
              m.put("smilestoneId", s.getSmilestoneId());
              m.put("interfaceId", rel.getInterfaceId());
              m.put("director", rel.getDirector());
              m.put("directorName", rel.getDirector() != null ? directorNameMap.get(rel.getDirector()) : null);
              m.put("planStartDate", rel.getPlanStartDate());
              m.put("planEndDate", rel.getPlanEndDate());
              m.put("actualStartDate", rel.getActualStartDate());
              m.put("actualEndDate", rel.getActualEndDate());
              m.put("planPeriod", rel.getPlanPeriod());
              m.put("actualPeriod", rel.getActualPeriod());
              m.put("stepStatus", rel.getStepStatus());
              stepViews.add(m);
            }
          } else if ("个性化功能开发".equals(s.getType())) {
            // 个性化功能开发：每条关系对应一个视图，区分具体个性化需求块
            for (ProjectSstepRelation rel : relList) {
              Map<String, Object> m = new HashMap<>();
              m.put("relationId", rel.getRelationId());
              m.put("projectId", rel.getProjectId());
              m.put("sstepId", s.getSstepId());
              m.put("sstepName", s.getSstepName());
              m.put("type", s.getType());
              m.put("systemName", s.getSystemName());
              m.put("smilestoneId", s.getSmilestoneId());
              m.put("personalDevId", rel.getPersonalDevId());
              m.put("director", rel.getDirector());
              m.put("directorName", rel.getDirector() != null ? directorNameMap.get(rel.getDirector()) : null);
              m.put("planStartDate", rel.getPlanStartDate());
              m.put("planEndDate", rel.getPlanEndDate());
              m.put("actualStartDate", rel.getActualStartDate());
              m.put("actualEndDate", rel.getActualEndDate());
              m.put("planPeriod", rel.getPlanPeriod());
              m.put("actualPeriod", rel.getActualPeriod());
              m.put("stepStatus", rel.getStepStatus());
              stepViews.add(m);
            }
          } else {
            // 非接口类型：按旧逻辑仅合并一条关系（通常只有一条）
            ProjectSstepRelation rel = relList.get(relList.size() - 1);
            Map<String, Object> m = new HashMap<>();
            m.put("relationId", rel.getRelationId());
            m.put("projectId", rel.getProjectId());
            m.put("sstepId", s.getSstepId());
            m.put("sstepName", s.getSstepName());
            m.put("type", s.getType());
            m.put("systemName", s.getSystemName());
            m.put("smilestoneId", s.getSmilestoneId());
            m.put("director", rel.getDirector());
            m.put("directorName", rel.getDirector() != null ? directorNameMap.get(rel.getDirector()) : null);
            m.put("planStartDate", rel.getPlanStartDate());
            m.put("planEndDate", rel.getPlanEndDate());
            m.put("actualStartDate", rel.getActualStartDate());
            m.put("actualEndDate", rel.getActualEndDate());
            m.put("planPeriod", rel.getPlanPeriod());
            m.put("actualPeriod", rel.getActualPeriod());
            m.put("stepStatus", rel.getStepStatus());
            stepViews.add(m);
          }
        } else {
          // 无关系时仍返回基础步骤（前端会过滤掉无 relationId 的项）
          Map<String, Object> m = new HashMap<>();
          m.put("sstepId", s.getSstepId());
          m.put("sstepName", s.getSstepName());
          m.put("type", s.getType());
          m.put("systemName", s.getSystemName());
          m.put("smilestoneId", s.getSmilestoneId());
          stepViews.add(m);
        }
      }

      // 若存在关系但其 sstepId 不在标准步骤中（例如历史或自定义），也追加展示
      for (ProjectSstepRelation rel : relations) {
        Long sid = rel.getSstepId();
        if (sid == null || baseStepIds.contains(sid)) continue;
        Map<String, Object> m = new HashMap<>();
        m.put("relationId", rel.getRelationId());
        m.put("projectId", rel.getProjectId());
        m.put("sstepId", rel.getSstepId());
        // 返回 interfaceId / personalDevId，供前端将步骤归属到具体块
        m.put("interfaceId", rel.getInterfaceId());
        m.put("personalDevId", rel.getPersonalDevId());
        m.put("director", rel.getDirector());
        m.put("directorName", rel.getDirector() != null ? directorNameMap.get(rel.getDirector()) : null);
        m.put("planStartDate", rel.getPlanStartDate());
        m.put("planEndDate", rel.getPlanEndDate());
        m.put("actualStartDate", rel.getActualStartDate());
        m.put("actualEndDate", rel.getActualEndDate());
        m.put("planPeriod", rel.getPlanPeriod());
        m.put("actualPeriod", rel.getActualPeriod());
        // 返回步骤状态，供前端状态列展示
        m.put("stepStatus", rel.getStepStatus());
        StandardConstructStep s = standardConstructStepRepository.findById(rel.getSstepId()).orElse(null);
        if (s != null) {
          m.put("sstepName", s.getSstepName());
          m.put("type", s.getType());
          m.put("smilestoneId", s.getSmilestoneId());
          m.put("systemName", s.getSystemName());
        }
        stepViews.add(m);
      }

      // 根据最新步骤实际工期，刷新项目里程碑的工期汇总并持久化
      constructMilestoneService.updateMilestonePeriodsForProject(projectId);

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