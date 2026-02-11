package com.missoft.pms.controller;

import com.missoft.pms.dto.AfterServiceLeadDTO;
import com.missoft.pms.service.AfterServiceLeadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/afterservice-leads")
public class AfterServiceLeadController {

    @Autowired
    private AfterServiceLeadService afterServiceLeadService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getLeads(
            @RequestParam Long afterServiceProjectId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<AfterServiceLeadDTO> leadsPage = afterServiceLeadService.getLeads(afterServiceProjectId, pageRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        Map<String, Object> data = new HashMap<>();
        data.put("list", leadsPage.getContent());
        data.put("total", leadsPage.getTotalElements());
        data.put("pages", leadsPage.getTotalPages());
        response.put("data", data);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getLeadsAll(@RequestParam Long afterServiceProjectId) {
        List<AfterServiceLeadDTO> list = afterServiceLeadService.getLeadsAll(afterServiceProjectId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", list.size());
        response.put("data", data);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createLead(@RequestBody AfterServiceLeadDTO dto) {
        AfterServiceLeadDTO created = afterServiceLeadService.createLead(dto);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", created);
        response.put("message", "创建成功");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateLead(@PathVariable Long id, @RequestBody AfterServiceLeadDTO dto) {
        AfterServiceLeadDTO updated = afterServiceLeadService.updateLead(id, dto);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", updated);
        response.put("message", "更新成功");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteLead(@PathVariable Long id) {
        afterServiceLeadService.deleteLead(id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "删除成功");
        return ResponseEntity.ok(response);
    }
}
