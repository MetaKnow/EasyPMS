package com.missoft.pms.service;

import com.missoft.pms.dto.ConstructingProjectCommentDTO;
import com.missoft.pms.entity.ConstructingProject;
import com.missoft.pms.entity.ConstructingProjectComment;
import com.missoft.pms.entity.User;
import com.missoft.pms.repository.ConstructingProjectCommentRepository;
import com.missoft.pms.repository.ConstructingProjectRepository;
import com.missoft.pms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ConstructingProjectCommentService {

    @Autowired
    private ConstructingProjectCommentRepository commentRepository;

    @Autowired
    private ConstructingProjectRepository constructingProjectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConstructingProjectCommentFileService commentFileService;

    public ConstructingProjectCommentDTO createComment(ConstructingProjectCommentDTO payload) {
        if (payload == null || payload.getProjectId() == null) {
            throw new IllegalArgumentException("projectId不能为空");
        }
        if (payload.getUserId() == null) {
            throw new IllegalArgumentException("userId不能为空");
        }
        ConstructingProject project = constructingProjectRepository.findById(payload.getProjectId()).orElse(null);
        if (project == null) {
            throw new IllegalArgumentException("项目不存在");
        }

        User user = userRepository.findById(payload.getUserId()).orElse(null);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        ConstructingProjectComment comment = new ConstructingProjectComment();
        comment.setProjectId(payload.getProjectId());
        comment.setUserId(payload.getUserId());
        String content = payload.getContent();
        if (content == null) {
            content = "";
        }
        comment.setContent(content.trim());

        ConstructingProjectComment saved = commentRepository.save(comment);
        return toDto(saved, user);
    }

    @Transactional(readOnly = true)
    public List<ConstructingProjectCommentDTO> listByProjectId(Long projectId) {
        if (projectId == null) return List.of();
        List<ConstructingProjectComment> list = commentRepository.findByProjectIdOrderByCreateTimeDesc(projectId);
        Set<Long> userIds = list.stream()
                .map(ConstructingProjectComment::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getUserId, u -> u));

        List<ConstructingProjectCommentDTO> result = new ArrayList<>();
        for (ConstructingProjectComment c : list) {
            User user = userMap.get(c.getUserId());
            result.add(toDto(c, user));
        }
        return result;
    }

    public int deleteComment(Long commentId, Long userId) {
        if (commentId == null) {
            throw new IllegalArgumentException("commentId不能为空");
        }
        if (userId == null) {
            throw new IllegalArgumentException("userId不能为空");
        }
        ConstructingProjectComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("评论不存在"));
        if (comment.getUserId() == null || !comment.getUserId().equals(userId)) {
            throw new IllegalArgumentException("只能删除自己发表的评论");
        }
        int removed = 0;
        try {
            removed = commentFileService.deleteFilesByCommentId(commentId);
        } catch (Exception ignore) {}
        commentRepository.delete(comment);
        return removed;
    }

    private ConstructingProjectCommentDTO toDto(ConstructingProjectComment c, User user) {
        ConstructingProjectCommentDTO dto = new ConstructingProjectCommentDTO();
        dto.setCommentId(c.getCommentId());
        dto.setProjectId(c.getProjectId());
        dto.setUserId(c.getUserId());
        dto.setContent(c.getContent());
        dto.setCreateTime(c.getCreateTime());
        dto.setUpdateTime(c.getUpdateTime());
        if (user != null) {
            dto.setUserName(user.getUserName());
            String display = user.getName();
            if (!StringUtils.hasText(display)) {
                display = user.getUserName();
            }
            dto.setDisplayName(display);
        }
        return dto;
    }
}
