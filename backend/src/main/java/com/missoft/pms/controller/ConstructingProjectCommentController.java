package com.missoft.pms.controller;

import com.missoft.pms.dto.ConstructingProjectCommentDTO;
import com.missoft.pms.service.ConstructingProjectCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/constructing-project-comments")
public class ConstructingProjectCommentController {

    @Autowired
    private ConstructingProjectCommentService commentService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody ConstructingProjectCommentDTO payload) {
        try {
            ConstructingProjectCommentDTO created = commentService.createComment(payload);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("data", created);
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "创建评论失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/by-project/{projectId}")
    public ResponseEntity<List<ConstructingProjectCommentDTO>> listByProject(@PathVariable Long projectId) {
        List<ConstructingProjectCommentDTO> list = commentService.listByProjectId(projectId);
        return ResponseEntity.ok(list);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long commentId, @RequestParam(required = false) Long userId) {
        try {
            int removed = commentService.deleteComment(commentId, userId);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("removedFiles", removed);
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "删除评论失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
