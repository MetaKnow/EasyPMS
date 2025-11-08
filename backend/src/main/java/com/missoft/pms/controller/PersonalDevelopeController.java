package com.missoft.pms.controller;

import com.missoft.pms.entity.PersonalDevelope;
import com.missoft.pms.service.PersonalDevelopeService;
import org.springframework.beans.factory.annotation.Autowired;
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
}