package com.missoft.pms.controller;

import com.missoft.pms.dto.AfterServiceEventDTO;
import com.missoft.pms.service.AfterServiceEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 运维事件控制器
 */
@RestController
@RequestMapping("/api/afterservice-events")
public class AfterServiceEventController {

    @Autowired
    private AfterServiceEventService afterServiceEventService;

    /**
     * 分页获取运维事件（按项目）
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getEvents(
            @RequestParam Long afterServiceProjectId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "eventDate"));
        Page<AfterServiceEventDTO> eventPage = afterServiceEventService.getEvents(afterServiceProjectId, pageRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        Map<String, Object> data = new HashMap<>();
        data.put("list", eventPage.getContent());
        data.put("total", eventPage.getTotalElements());
        data.put("pages", eventPage.getTotalPages());
        response.put("data", data);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取指定项目的全部事件（不分页）
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getEventsAll(@RequestParam Long afterServiceProjectId) {
        List<AfterServiceEventDTO> list = afterServiceEventService.getEventsAll(afterServiceProjectId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", list.size());
        response.put("data", data);
        return ResponseEntity.ok(response);
    }

    /**
     * 创建运维事件
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createEvent(@RequestBody AfterServiceEventDTO dto) {
        AfterServiceEventDTO created = afterServiceEventService.createEvent(dto);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", created);
        response.put("message", "创建成功");
        return ResponseEntity.ok(response);
    }

    /**
     * 更新运维事件
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateEvent(@PathVariable Long id, @RequestBody AfterServiceEventDTO dto) {
        AfterServiceEventDTO updated = afterServiceEventService.updateEvent(id, dto);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", updated);
        response.put("message", "更新成功");
        return ResponseEntity.ok(response);
    }

    /**
     * 删除运维事件
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteEvent(@PathVariable Long id) {
        afterServiceEventService.deleteEvent(id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "删除成功");
        return ResponseEntity.ok(response);
    }
}

