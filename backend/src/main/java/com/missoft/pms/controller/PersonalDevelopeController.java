package com.missoft.pms.controller;

import com.missoft.pms.entity.PersonalDevelope;
import com.missoft.pms.service.PersonalDevelopeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 类级注释：
 * 个性化开发控制器，提供创建与按项目查询接口。
 */
@RestController
@RequestMapping("/api/personal-developes")
public class PersonalDevelopeController {

    @Autowired
    private PersonalDevelopeService personalDevelopeService;

    /**
     * 函数级注释：创建个性化开发条目
     * @param payload 请求体，包含 `projectId`, `milestoneId`, `personalDevName`
     * @return 创建后的实体
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody PersonalDevelope payload) {
        PersonalDevelope saved = personalDevelopeService.create(payload);
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("personalDevelope", saved);
        return ResponseEntity.ok(resp);
    }

    /**
     * 函数级注释：按项目ID查询该项目下的个性化开发项
     * @param projectId 项目ID
     * @return 个性化开发列表（直接返回列表，便于前端处理）
     */
    @GetMapping("/by-project/{projectId}")
    public ResponseEntity<List<PersonalDevelope>> listByProject(@PathVariable Long projectId) {
        List<PersonalDevelope> list = personalDevelopeService.listByProjectId(projectId);
        return ResponseEntity.ok(list);
    }

    /**
     * 函数级注释：按个性化开发ID删除记录及其生成的步骤关系
     * 前端在个性化需求信息行点击删除时调用，删除成功后前端应刷新项目摘要以更新里程碑与步骤展示。
     *
     * @param personalDevId 个性化开发ID
     * @return 删除结果（成功/失败信息）
     */
    @DeleteMapping("/{personalDevId}")
    public ResponseEntity<Map<String, Object>> deleteById(@PathVariable Long personalDevId) {
        try {
            boolean ok = personalDevelopeService.deleteById(personalDevId);
            if (!ok) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "个性化开发不存在或已删除"));
            }
            Map<String, Object> resp = new HashMap<>();
            resp.put("message", "deleted");
            resp.put("personalDevId", personalDevId);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "删除个性化开发失败", "message", e.getMessage()));
        }
    }
}