package com.missoft.pms.controller;

import com.missoft.pms.dto.CustomerFollowUpDTO;
import com.missoft.pms.service.CustomerFollowUpService;
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
@RequestMapping("/api/customer-follow-ups")
public class CustomerFollowUpController {

    @Autowired
    private CustomerFollowUpService customerFollowUpService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getFollowUps(
            @RequestParam Long afterServiceProjectId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "followUpDate"));
        Page<CustomerFollowUpDTO> followUpPage = customerFollowUpService.getFollowUps(afterServiceProjectId, pageRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        Map<String, Object> data = new HashMap<>();
        data.put("list", followUpPage.getContent());
        data.put("total", followUpPage.getTotalElements());
        data.put("pages", followUpPage.getTotalPages());
        response.put("data", data);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getFollowUpsAll(@RequestParam Long afterServiceProjectId) {
        List<CustomerFollowUpDTO> list = customerFollowUpService.getFollowUpsAll(afterServiceProjectId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", list.size());
        response.put("data", data);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createFollowUp(@RequestBody CustomerFollowUpDTO dto) {
        try {
            CustomerFollowUpDTO created = customerFollowUpService.createFollowUp(dto);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", created);
            response.put("message", "创建成功");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateFollowUp(@PathVariable Long id, @RequestBody CustomerFollowUpDTO dto) {
        try {
            CustomerFollowUpDTO updated = customerFollowUpService.updateFollowUp(id, dto);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", updated);
            response.put("message", "更新成功");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteFollowUp(@PathVariable Long id) {
        customerFollowUpService.deleteFollowUp(id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "删除成功");
        return ResponseEntity.ok(response);
    }
}
