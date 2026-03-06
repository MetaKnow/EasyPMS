package com.missoft.pms.controller;

import com.missoft.pms.entity.ProjectSstepRelation;
import com.missoft.pms.service.ConstructingProjectModifyRecordService;
import com.missoft.pms.service.ConstructMilestoneService;
import com.missoft.pms.repository.ProjectSstepRelationRepository;
import com.missoft.pms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * 项目-标准步骤关系编辑接口
 */
@RestController
@RequestMapping("/api/project-relations")
@CrossOrigin(origins = "*")
public class ProjectSstepRelationController {

    @Autowired
    private ProjectSstepRelationRepository relationRepository;

    @Autowired
    private ConstructMilestoneService constructMilestoneService;

    @Autowired
    private com.missoft.pms.repository.ConstructingProjectRepository constructingProjectRepository;

    @Autowired
    private ConstructingProjectModifyRecordService modifyRecordService;

    @Autowired
    private UserRepository userRepository;

    /**
     * 更新项目步骤关系的字段（支持部分字段）
     * PUT /api/project-relations/{relationId}
     * 请求体支持字段：director（Long）、planStartDate/planEndDate/actualStartDate/actualEndDate（yyyy-MM-dd）、
     * planPeriod/actualPeriod（Integer）
     */
    @PutMapping("/{relationId}")
    public ResponseEntity<Map<String, Object>> updateRelation(
            @PathVariable Long relationId,
            @RequestBody Map<String, Object> body
    ) {
        try {
            ProjectSstepRelation rel = relationRepository.findById(relationId).orElse(null);
            if (rel == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "关系不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            Long projectId = rel.getProjectId();
            var project = projectId != null ? constructingProjectRepository.findById(projectId).orElse(null) : null;
            if (projectId != null) {
                if (project != null && "已完成".equals(project.getProjectState())) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("error", "已完成项目不允许修改步骤字段");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                }
            }

            Long oldDirector = rel.getDirector();
            LocalDate oldPlanStartDate = rel.getPlanStartDate();
            LocalDate oldPlanEndDate = rel.getPlanEndDate();
            LocalDate oldActualStartDate = rel.getActualStartDate();
            LocalDate oldActualEndDate = rel.getActualEndDate();

            // 负责人
            if (body.containsKey("director")) {
                Object d = body.get("director");
                rel.setDirector(d != null ? Long.valueOf(d.toString()) : null);
            }

            // 日期（按yyyy-MM-dd解析）
            rel.setPlanStartDate(parseDateFromBody(body, "planStartDate", rel.getPlanStartDate()));
            rel.setPlanEndDate(parseDateFromBody(body, "planEndDate", rel.getPlanEndDate()));
            rel.setActualStartDate(parseDateFromBody(body, "actualStartDate", rel.getActualStartDate()));
            rel.setActualEndDate(parseDateFromBody(body, "actualEndDate", rel.getActualEndDate()));

            // 工期
            if (body.containsKey("planPeriod")) {
                Object pp = body.get("planPeriod");
                rel.setPlanPeriod(pp != null ? Integer.valueOf(pp.toString()) : null);
            }
            if (body.containsKey("actualPeriod")) {
                Object ap = body.get("actualPeriod");
                rel.setActualPeriod(ap != null ? Integer.valueOf(ap.toString()) : null);
            }

            // 步骤状态
            if (body.containsKey("stepStatus")) {
                Object s = body.get("stepStatus");
                rel.setStepStatus(s != null ? s.toString() : null);
            }

            relationRepository.save(rel);

            Long modifyUser = parseLong(body.get("modifyUser"));
            if (modifyUser == null && project != null) {
                modifyUser = project.getProjectLeader() != null ? project.getProjectLeader() : project.getSaleLeader();
            }
            if (projectId != null && modifyUser != null) {
                if (!java.util.Objects.equals(oldDirector, rel.getDirector())) {
                    String beforeText = "步骤ID=" + relationId + ", 负责人=" + formatUserLabel(oldDirector);
                    String afterText = "步骤ID=" + relationId + ", 负责人=" + formatUserLabel(rel.getDirector());
                    modifyRecordService.createRecord(projectId, modifyUser, "修改步骤负责人", beforeText, afterText);
                }
                if (!java.util.Objects.equals(oldPlanStartDate, rel.getPlanStartDate())) {
                    String beforeText = "步骤ID=" + relationId + ", 计划开始日期=" + formatValue(oldPlanStartDate);
                    String afterText = "步骤ID=" + relationId + ", 计划开始日期=" + formatValue(rel.getPlanStartDate());
                    modifyRecordService.createRecord(projectId, modifyUser, "修改步骤计划开始日期", beforeText, afterText);
                }
                if (!java.util.Objects.equals(oldPlanEndDate, rel.getPlanEndDate())) {
                    String beforeText = "步骤ID=" + relationId + ", 计划结束日期=" + formatValue(oldPlanEndDate);
                    String afterText = "步骤ID=" + relationId + ", 计划结束日期=" + formatValue(rel.getPlanEndDate());
                    modifyRecordService.createRecord(projectId, modifyUser, "修改步骤计划结束日期", beforeText, afterText);
                }
                if (!java.util.Objects.equals(oldActualStartDate, rel.getActualStartDate())) {
                    String beforeText = "步骤ID=" + relationId + ", 实际开始日期=" + formatValue(oldActualStartDate);
                    String afterText = "步骤ID=" + relationId + ", 实际开始日期=" + formatValue(rel.getActualStartDate());
                    modifyRecordService.createRecord(projectId, modifyUser, "修改步骤实际开始日期", beforeText, afterText);
                }
                if (!java.util.Objects.equals(oldActualEndDate, rel.getActualEndDate())) {
                    String beforeText = "步骤ID=" + relationId + ", 实际结束日期=" + formatValue(oldActualEndDate);
                    String afterText = "步骤ID=" + relationId + ", 实际结束日期=" + formatValue(rel.getActualEndDate());
                    modifyRecordService.createRecord(projectId, modifyUser, "修改步骤实际结束日期", beforeText, afterText);
                }
            }

            // 步骤工期/日期/负责人变化后，刷新并写回项目里程碑的工期汇总
            if (rel.getProjectId() != null) {
                constructMilestoneService.updateMilestonePeriodsForProject(rel.getProjectId());
            }

            Map<String, Object> res = new HashMap<>();
            res.put("message", "更新成功");
            res.put("relation", rel);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "更新失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    private LocalDate parseDateFromBody(Map<String, Object> body, String key, LocalDate fallback) {
        if (!body.containsKey(key)) return fallback;
        Object v = body.get(key);
        if (v == null || v.toString().trim().isEmpty()) return null;
        try {
            return LocalDate.parse(v.toString().trim());
        } catch (Exception e) {
            return fallback; // 非法日期时保持原值
        }
    }

    private Long parseLong(Object value) {
        if (value == null) return null;
        try {
            String s = value.toString().trim();
            if (s.isEmpty()) return null;
            return Long.valueOf(s);
        } catch (Exception e) {
            return null;
        }
    }

    private String formatValue(Object value) {
        if (value == null) return "无";
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? "无" : text;
    }

    private String formatUserLabel(Long userId) {
        if (userId == null) return "无";
        var user = userRepository.findById(userId).orElse(null);
        if (user == null) return "用户ID=" + userId;
        String name = user.getName();
        if (name != null && !name.trim().isEmpty()) return name.trim() + "(ID=" + userId + ")";
        String userName = user.getUserName();
        if (userName != null && !userName.trim().isEmpty()) return userName.trim() + "(ID=" + userId + ")";
        return "用户ID=" + userId;
    }
}
