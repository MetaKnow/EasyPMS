package com.missoft.pms.controller;

import com.missoft.pms.dto.ConstructingProjectCommentReplyDTO;
import com.missoft.pms.service.ConstructingProjectCommentReplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 类级注释：项目评论回复控制器
 */
@RestController
@RequestMapping("/api/constructing-project-comment-replies")
@CrossOrigin(origins = "*")
public class ConstructingProjectCommentReplyController {

    @Autowired
    private ConstructingProjectCommentReplyService replyService;

    /**
     * 函数级注释：创建评论回复
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody ConstructingProjectCommentReplyDTO payload) {
        try {
            ConstructingProjectCommentReplyDTO created = replyService.createReply(payload);
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
            error.put("error", "创建回复失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 函数级注释：按评论ID获取回复列表
     */
    @GetMapping("/by-comment/{commentId}")
    public ResponseEntity<List<ConstructingProjectCommentReplyDTO>> listByComment(@PathVariable Long commentId) {
        List<ConstructingProjectCommentReplyDTO> list = replyService.listByCommentId(commentId);
        return ResponseEntity.ok(list);
    }

    /**
     * 函数级注释：删除评论回复（仅本人）
     */
    @DeleteMapping("/{replyId}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long replyId, @RequestParam(required = false) Long userId) {
        try {
            int removed = replyService.deleteReply(replyId, userId);
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
            error.put("error", "删除回复失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
