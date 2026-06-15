package com.missoft.pms.controller;

import com.missoft.pms.dto.CustomerFollowUpDTO;
import com.missoft.pms.service.CustomerFollowUpService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
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
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long operatorUserId,
            HttpServletRequest request) {

        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "followUpDate"));
        Long resolvedOperatorUserId = resolveOperatorUserId(operatorUserId, request);
        Page<CustomerFollowUpDTO> followUpPage = customerFollowUpService.getFollowUps(afterServiceProjectId, resolvedOperatorUserId, pageRequest);

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
    public ResponseEntity<Map<String, Object>> getFollowUpsAll(@RequestParam Long afterServiceProjectId,
                                                               @RequestParam(required = false) Long operatorUserId,
                                                               HttpServletRequest request) {
        Long resolvedOperatorUserId = resolveOperatorUserId(operatorUserId, request);
        List<CustomerFollowUpDTO> list = customerFollowUpService.getFollowUpsAll(afterServiceProjectId, resolvedOperatorUserId);
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
    public ResponseEntity<Map<String, Object>> updateFollowUp(@PathVariable Long id,
                                                               @RequestBody CustomerFollowUpDTO dto,
                                                               @RequestParam(required = false) Long operatorUserId,
                                                               HttpServletRequest request) {
        try {
            Long resolvedOperatorUserId = resolveOperatorUserId(operatorUserId, request);
            CustomerFollowUpDTO updated = customerFollowUpService.updateFollowUp(id, dto, resolvedOperatorUserId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", updated);
            response.put("message", "更新成功");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            if (e.getMessage() != null && e.getMessage().contains("仅可编辑或删除本人新增")) {
                error.put("message", "您没有编辑该回访记录的权限");
                error.put("error", e.getMessage());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteFollowUp(@PathVariable Long id,
                                                               @RequestParam(required = false) Long operatorUserId,
                                                               HttpServletRequest request) {
        try {
            Long resolvedOperatorUserId = resolveOperatorUserId(operatorUserId, request);
            customerFollowUpService.deleteFollowUp(id, resolvedOperatorUserId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "删除成功");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            if (e.getMessage() != null && e.getMessage().contains("仅可编辑或删除本人新增")) {
                error.put("message", "您没有删除该回访记录的权限");
                error.put("error", e.getMessage());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    private Long resolveOperatorUserId(Long operatorUserId, HttpServletRequest request) {
        if (operatorUserId != null) {
            return operatorUserId;
        }
        Long fromHeader = parseLong(request.getHeader("X-User-Id"));
        if (fromHeader != null) {
            return fromHeader;
        }
        String authorization = request.getHeader("Authorization");
        if (authorization != null && !authorization.isBlank()) {
            String token = authorization.trim();
            if (token.regionMatches(true, 0, "Bearer ", 0, 7)) {
                token = token.substring(7).trim();
            }
            if (token.startsWith("simple_token_")) {
                String[] parts = token.split("_");
                if (parts.length >= 3) {
                    return parseLong(parts[2]);
                }
            }
        }
        return null;
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
