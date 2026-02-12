package com.missoft.pms.service;

import com.missoft.pms.dto.ConstructingProjectCommentReplyDTO;
import com.missoft.pms.entity.ConstructingProject;
import com.missoft.pms.entity.ConstructingProjectComment;
import com.missoft.pms.entity.ConstructingProjectCommentReply;
import com.missoft.pms.entity.User;
import com.missoft.pms.repository.ConstructingProjectCommentReplyRepository;
import com.missoft.pms.repository.ConstructingProjectCommentRepository;
import com.missoft.pms.repository.ConstructingProjectRepository;
import com.missoft.pms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 类级注释：项目评论回复服务
 */
@Service
@Transactional
public class ConstructingProjectCommentReplyService {

    @Autowired
    private ConstructingProjectCommentReplyRepository replyRepository;

    @Autowired
    private ConstructingProjectCommentRepository commentRepository;

    @Autowired
    private ConstructingProjectRepository constructingProjectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConstructingProjectCommentReplyFileService replyFileService;

    /**
     * 函数级注释：创建评论回复
     */
    public ConstructingProjectCommentReplyDTO createReply(ConstructingProjectCommentReplyDTO payload) {
        if (payload == null || payload.getProjectId() == null) {
            throw new IllegalArgumentException("projectId不能为空");
        }
        if (payload.getCommentId() == null) {
            throw new IllegalArgumentException("commentId不能为空");
        }
        if (payload.getUserId() == null) {
            throw new IllegalArgumentException("userId不能为空");
        }
        ConstructingProject project = constructingProjectRepository.findById(payload.getProjectId()).orElse(null);
        if (project == null) {
            throw new IllegalArgumentException("项目不存在");
        }
        ConstructingProjectComment comment = commentRepository.findById(payload.getCommentId()).orElse(null);
        if (comment == null) {
            throw new IllegalArgumentException("评论不存在");
        }
        if (!Objects.equals(comment.getProjectId(), payload.getProjectId())) {
            throw new IllegalArgumentException("评论不属于当前项目");
        }
        User user = userRepository.findById(payload.getUserId()).orElse(null);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        ConstructingProjectCommentReply reply = new ConstructingProjectCommentReply();
        reply.setProjectId(payload.getProjectId());
        reply.setCommentId(payload.getCommentId());
        reply.setUserId(payload.getUserId());
        String content = payload.getContent();
        if (content == null) {
            content = "";
        }
        reply.setContent(content.trim());

        ConstructingProjectCommentReply saved = replyRepository.save(reply);
        return toDto(saved, user);
    }

    /**
     * 函数级注释：按评论ID获取回复列表
     */
    @Transactional(readOnly = true)
    public List<ConstructingProjectCommentReplyDTO> listByCommentId(Long commentId) {
        if (commentId == null) return List.of();
        List<ConstructingProjectCommentReply> list = replyRepository.findByCommentIdOrderByCreateTimeAsc(commentId);
        Set<Long> userIds = list.stream()
                .map(ConstructingProjectCommentReply::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getUserId, u -> u));
        List<ConstructingProjectCommentReplyDTO> result = new ArrayList<>();
        for (ConstructingProjectCommentReply r : list) {
            result.add(toDto(r, userMap.get(r.getUserId())));
        }
        return result;
    }

    /**
     * 函数级注释：删除回复并级联删除附件
     */
    public int deleteReply(Long replyId, Long userId) {
        if (replyId == null) {
            throw new IllegalArgumentException("replyId不能为空");
        }
        if (userId == null) {
            throw new IllegalArgumentException("userId不能为空");
        }
        ConstructingProjectCommentReply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("回复不存在"));
        if (reply.getUserId() == null || !reply.getUserId().equals(userId)) {
            throw new IllegalArgumentException("只能删除自己发表的回复");
        }
        int removed = 0;
        try {
            removed = replyFileService.deleteFilesByReplyId(replyId);
        } catch (Exception ignore) {}
        replyRepository.delete(reply);
        return removed;
    }

    /**
     * 函数级注释：实体转DTO
     */
    private ConstructingProjectCommentReplyDTO toDto(ConstructingProjectCommentReply r, User user) {
        ConstructingProjectCommentReplyDTO dto = new ConstructingProjectCommentReplyDTO();
        dto.setReplyId(r.getReplyId());
        dto.setProjectId(r.getProjectId());
        dto.setCommentId(r.getCommentId());
        dto.setUserId(r.getUserId());
        dto.setContent(r.getContent());
        dto.setCreateTime(r.getCreateTime());
        dto.setUpdateTime(r.getUpdateTime());
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
