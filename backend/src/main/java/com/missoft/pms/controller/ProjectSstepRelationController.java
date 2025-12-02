package com.missoft.pms.controller;

import com.missoft.pms.entity.ProjectSstepRelation;
import com.missoft.pms.service.ConstructMilestoneService;
import com.missoft.pms.repository.ProjectSstepRelationRepository;
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
            if (projectId != null) {
                var project = constructingProjectRepository.findById(projectId).orElse(null);
                if (project != null && "已完成".equals(project.getProjectState())) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("error", "已完成项目不允许修改步骤字段");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                }
            }

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
}
