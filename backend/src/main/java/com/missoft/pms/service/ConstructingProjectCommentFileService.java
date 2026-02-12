package com.missoft.pms.service;

import com.missoft.pms.entity.ConstructingProject;
import com.missoft.pms.entity.ConstructingProjectComment;
import com.missoft.pms.entity.ConstructingProjectCommentFile;
import com.missoft.pms.entity.User;
import com.missoft.pms.repository.ConstructingProjectCommentFileRepository;
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

@Service
@Transactional
public class ConstructingProjectCommentFileService {

    @Autowired
    private ConstructingProjectCommentFileRepository fileRepository;

    @Autowired
    private ConstructingProjectRepository constructingProjectRepository;

    @Autowired
    private ConstructingProjectCommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    private Path getProjectRoot() {
        Path cwd = Paths.get("").toAbsolutePath();
        if (cwd.endsWith("backend")) {
            return cwd.getParent();
        }
        return cwd;
    }

    public List<ConstructingProjectCommentFile> uploadFiles(Long projectId, Long commentId, Long uploaderId, MultipartFile[] files) {
        if (projectId == null || commentId == null) {
            throw new RuntimeException("项目ID与评论ID不能为空");
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
        User user = userRepository.findById(uploaderId)
                .orElseThrow(() -> new RuntimeException("用户不存在，ID: " + uploaderId));
        if (!comment.getProjectId().equals(projectId)) {
            throw new RuntimeException("评论不属于当前项目");
        }

        String projNum = safe(project.getProjectNum());
        String projName = safe(project.getProjectName());
        String projectKey = projNum + "-" + projName;
        Path root = getProjectRoot()
                .resolve("deliverableFiles")
                .resolve(projectKey)
                .resolve("项目评论")
                .resolve(String.valueOf(commentId));
        try {
            Files.createDirectories(root);
        } catch (Exception e) {
            throw new RuntimeException("创建附件目录失败: " + e.getMessage());
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");
        String relativeDir = "deliverableFiles/" + projectKey + "/项目评论/" + commentId + "/";
        int seq = 1;
        List<ConstructingProjectCommentFile> saved = new ArrayList<>();
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

            ConstructingProjectCommentFile rec = new ConstructingProjectCommentFile();
            rec.setProjectId(projectId);
            rec.setCommentId(commentId);
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

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listByCommentId(Long commentId) {
        List<ConstructingProjectCommentFile> list = fileRepository.findByCommentIdOrderByCreateTimeDesc(commentId);
        return toMapList(list);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listByProjectId(Long projectId) {
        List<ConstructingProjectCommentFile> list = fileRepository.findByProjectIdOrderByCreateTimeDesc(projectId);
        return toMapList(list);
    }

    public Path resolveFilePath(Long fileId) {
        ConstructingProjectCommentFile rec = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("文件记录不存在，ID: " + fileId));
        String relative = rec.getFilePath() != null ? rec.getFilePath() : "";
        Path root = getProjectRoot();
        Path target = root.resolve(relative).normalize();
        if (!target.startsWith(root)) {
            throw new RuntimeException("非法文件路径");
        }
        return target;
    }

    public int deleteFilesByCommentId(Long commentId) {
        if (commentId == null) return 0;
        List<ConstructingProjectCommentFile> list = fileRepository.findByCommentIdOrderByCreateTimeDesc(commentId);
        if (list == null || list.isEmpty()) return 0;
        Path root = getProjectRoot();
        for (ConstructingProjectCommentFile rec : list) {
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

    private List<Map<String, Object>> toMapList(List<ConstructingProjectCommentFile> list) {
        List<Map<String, Object>> files = new ArrayList<>();
        for (ConstructingProjectCommentFile r : list) {
            Map<String, Object> info = new HashMap<>();
            info.put("fileId", r.getFileId());
            info.put("filePath", r.getFilePath());
            info.put("fileSize", r.getFileSize());
            info.put("uploadUser", r.getUploadUser());
            info.put("projectId", r.getProjectId());
            info.put("commentId", r.getCommentId());
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
