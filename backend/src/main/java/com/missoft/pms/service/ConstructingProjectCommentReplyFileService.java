package com.missoft.pms.service;

import com.missoft.pms.entity.ConstructingProject;
import com.missoft.pms.entity.ConstructingProjectComment;
import com.missoft.pms.entity.ConstructingProjectCommentReply;
import com.missoft.pms.entity.ConstructingProjectCommentReplyFile;
import com.missoft.pms.entity.User;
import com.missoft.pms.repository.ConstructingProjectCommentReplyFileRepository;
import com.missoft.pms.repository.ConstructingProjectCommentReplyRepository;
import com.missoft.pms.repository.ConstructingProjectCommentRepository;
import com.missoft.pms.repository.ConstructingProjectRepository;
import com.missoft.pms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 类级注释：项目评论回复附件服务
 */
@Service
@Transactional
public class ConstructingProjectCommentReplyFileService {

    @Autowired
    private ConstructingProjectCommentReplyFileRepository fileRepository;

    @Autowired
    private ConstructingProjectRepository constructingProjectRepository;

    @Autowired
    private ConstructingProjectCommentRepository commentRepository;

    @Autowired
    private ConstructingProjectCommentReplyRepository replyRepository;

    @Autowired
    private UserRepository userRepository;

    private Path getProjectRoot() {
        Path cwd = Paths.get("").toAbsolutePath();
        if (cwd.endsWith("backend")) {
            return cwd.getParent();
        }
        return cwd;
    }

    /**
     * 函数级注释：上传评论回复附件
     */
    public List<ConstructingProjectCommentReplyFile> uploadFiles(Long projectId, Long commentId, Long replyId, Long uploaderId, MultipartFile[] files) {
        if (projectId == null || commentId == null || replyId == null) {
            throw new RuntimeException("项目ID、评论ID与回复ID不能为空");
        }
        if (uploaderId == null) {
            throw new RuntimeException("上传人不能为空");
        }
        if (files == null || files.length == 0) {
            return new ArrayList<>();
        }

        ConstructingProject project = constructingProjectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("项目不存在，ID: " + projectId));
        ConstructingProjectComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("评论不存在，ID: " + commentId));
        ConstructingProjectCommentReply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new RuntimeException("回复不存在，ID: " + replyId));
        User user = userRepository.findById(uploaderId)
                .orElseThrow(() -> new RuntimeException("用户不存在，ID: " + uploaderId));
        if (!Objects.equals(comment.getProjectId(), projectId) || !Objects.equals(reply.getProjectId(), projectId)) {
            throw new RuntimeException("回复不属于当前项目");
        }
        if (!Objects.equals(reply.getCommentId(), commentId)) {
            throw new RuntimeException("回复不属于当前评论");
        }

        String projNum = safe(project.getProjectNum());
        String projName = safe(project.getProjectName());
        String projectKey = projNum + "-" + projName;
        Path root = getProjectRoot()
                .resolve("deliverableFiles")
                .resolve(projectKey)
                .resolve("项目评论")
                .resolve("回复")
                .resolve(String.valueOf(commentId))
                .resolve(String.valueOf(replyId));
        try {
            Files.createDirectories(root);
        } catch (Exception e) {
            throw new RuntimeException("创建附件目录失败: " + e.getMessage());
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");
        String relativeDir = "deliverableFiles/" + projectKey + "/项目评论/回复/" + commentId + "/" + replyId + "/";
        int seq = 1;
        List<ConstructingProjectCommentReplyFile> saved = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;
            String originalName = Paths.get(file.getOriginalFilename()).getFileName().toString();
            String safeOriginal = originalName.replaceAll("[\\\\/:*?\"<>|]", "_");
            String ext = "";
            int dot = safeOriginal.lastIndexOf('.');
            if (dot >= 0) {
                ext = safeOriginal.substring(dot);
                safeOriginal = safeOriginal.substring(0, dot);
            }
            String ts = LocalDateTime.now().format(fmt);
            String baseName = safeOriginal + "-" + ts;
            String finalName = baseName + ext;
            Path target = root.resolve(finalName).normalize();
            while (Files.exists(target)) {
                finalName = baseName + "-" + (seq++) + ext;
                target = root.resolve(finalName).normalize();
            }
            if (!target.startsWith(root)) {
                throw new RuntimeException("非法文件名");
            }
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                throw new RuntimeException("保存文件失败: " + originalName);
            }

            ConstructingProjectCommentReplyFile rec = new ConstructingProjectCommentReplyFile();
            rec.setProjectId(projectId);
            rec.setCommentId(commentId);
            rec.setReplyId(replyId);
            rec.setUploadUser(user.getUserId());
            rec.setFilePath(relativeDir + finalName);
            try {
                rec.setFileSize(Files.size(target));
            } catch (Exception ex) {
                rec.setFileSize(0L);
            }
            saved.add(fileRepository.save(rec));
        }
        return saved;
    }

    /**
     * 函数级注释：按评论ID获取回复附件
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> listByCommentId(Long commentId) {
        List<ConstructingProjectCommentReplyFile> list = fileRepository.findByCommentIdOrderByCreateTimeAsc(commentId);
        return toMapList(list);
    }

    /**
     * 函数级注释：按回复ID获取回复附件
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> listByReplyId(Long replyId) {
        List<ConstructingProjectCommentReplyFile> list = fileRepository.findByReplyIdOrderByCreateTimeAsc(replyId);
        return toMapList(list);
    }

    /**
     * 函数级注释：解析回复附件路径
     */
    public Path resolveFilePath(Long fileId) {
        ConstructingProjectCommentReplyFile rec = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("文件记录不存在，ID: " + fileId));
        String relative = rec.getFilePath() != null ? rec.getFilePath() : "";
        Path root = getProjectRoot();
        Path target = root.resolve(relative).normalize();
        if (!target.startsWith(root)) {
            throw new RuntimeException("非法文件路径");
        }
        return target;
    }

    /**
     * 函数级注释：按回复ID删除回复附件与文件
     */
    public int deleteFilesByReplyId(Long replyId) {
        if (replyId == null) return 0;
        List<ConstructingProjectCommentReplyFile> list = fileRepository.findByReplyIdOrderByCreateTimeAsc(replyId);
        if (list == null || list.isEmpty()) return 0;
        Path root = getProjectRoot();
        for (ConstructingProjectCommentReplyFile rec : list) {
            String relative = rec != null ? rec.getFilePath() : null;
            if (relative == null || relative.isBlank()) continue;
            Path target = root.resolve(relative).normalize();
            if (!target.startsWith(root)) continue;
            try {
                Files.deleteIfExists(target);
            } catch (Exception ignore) {}
        }
        try {
            fileRepository.deleteAll(list);
        } catch (Exception ignore) {}
        return list.size();
    }

    private List<Map<String, Object>> toMapList(List<ConstructingProjectCommentReplyFile> list) {
        List<Map<String, Object>> files = new ArrayList<>();
        for (ConstructingProjectCommentReplyFile r : list) {
            Map<String, Object> info = new HashMap<>();
            info.put("fileId", r.getFileId());
            info.put("filePath", r.getFilePath());
            info.put("fileSize", r.getFileSize());
            info.put("uploadUser", r.getUploadUser());
            info.put("projectId", r.getProjectId());
            info.put("commentId", r.getCommentId());
            info.put("replyId", r.getReplyId());
            info.put("createTime", r.getCreateTime());
            files.add(info);
        }
        return files;
    }

    private String safe(String s) {
        if (s == null) return "";
        return s.trim().replaceAll("[\\/:*?\"<>|]", "_");
    }
}
