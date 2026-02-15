package com.missoft.pms.service;

import com.missoft.pms.entity.ConstructingProject;
import com.missoft.pms.entity.ConstructingProjectRisk;
import com.missoft.pms.entity.ConstructingProjectRiskFile;
import com.missoft.pms.repository.ConstructingProjectRepository;
import com.missoft.pms.repository.ConstructingProjectRiskFileRepository;
import com.missoft.pms.repository.ConstructingProjectRiskRepository;
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

/**
 * 类级注释：项目风险附件服务
 */
@Service
@Transactional
public class ConstructingProjectRiskFileService {

    @Autowired
    private ConstructingProjectRiskFileRepository fileRepository;

    @Autowired
    private ConstructingProjectRepository constructingProjectRepository;

    @Autowired
    private ConstructingProjectRiskRepository riskRepository;

    /**
     * 函数级注释：上传项目风险附件并保存记录
     */
    public List<ConstructingProjectRiskFile> uploadFiles(Long projectId, Long riskId, Long uploaderId, MultipartFile[] files) {
        if (projectId == null || riskId == null) {
            throw new RuntimeException("项目ID与风险ID不能为空");
        }
        if (uploaderId == null) {
            throw new RuntimeException("上传人不能为空");
        }
        if (files == null || files.length == 0) {
            return new ArrayList<>();
        }

        ConstructingProject project = constructingProjectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("项目不存在，ID: " + projectId));
        ConstructingProjectRisk risk = riskRepository.findById(riskId)
            .orElseThrow(() -> new RuntimeException("风险不存在，ID: " + riskId));

        String projNum = safe(project.getProjectNum(), "unknown");
        String projName = safe(project.getProjectName(), "未命名项目");
        String projectKey = safe(projNum) + "-" + safe(projName);
        if (risk.getProjectId() != null && !risk.getProjectId().equals(projectId)) {
            throw new RuntimeException("风险不属于当前项目");
        }

        Path root = getProjectRoot()
            .resolve("deliverableFiles")
            .resolve(projectKey)
            .resolve("项目风险")
            .resolve(String.valueOf(riskId));
        try {
            Files.createDirectories(root);
        } catch (Exception e) {
            throw new RuntimeException("创建附件目录失败: " + e.getMessage());
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
        List<ConstructingProjectRiskFile> saved = new ArrayList<>();
        String relativeDir = "deliverableFiles/" + projectKey + "/项目风险/" + riskId + "/";

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
            String finalName = safeOriginal + "-" + ts + ext;
            Path target = root.resolve(finalName).normalize();
            if (!target.startsWith(root)) {
                throw new RuntimeException("非法文件名");
            }
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                throw new RuntimeException("保存文件失败: " + originalName);
            }

            ConstructingProjectRiskFile rec = new ConstructingProjectRiskFile();
            rec.setProjectId(projectId);
            rec.setRiskId(riskId);
            rec.setUploadUser(uploaderId);
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
     * 函数级注释：获取风险附件列表
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> listFiles(Long riskId) {
        List<ConstructingProjectRiskFile> list = fileRepository.findByRiskId(riskId);
        List<Map<String, Object>> files = new ArrayList<>();
        for (ConstructingProjectRiskFile r : list) {
            Map<String, Object> info = new HashMap<>();
            info.put("fileId", r.getFileId());
            info.put("filePath", r.getFilePath());
            info.put("fileSize", r.getFileSize());
            info.put("uploadUser", r.getUploadUser());
            info.put("projectId", r.getProjectId());
            info.put("riskId", r.getRiskId());
            files.add(info);
        }
        return files;
    }

    /**
     * 函数级注释：解析附件在磁盘上的路径
     */
    public Path resolveFilePath(Long fileId) {
        ConstructingProjectRiskFile rec = fileRepository.findById(fileId)
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
     * 函数级注释：删除单个附件及数据库记录
     */
    public void deleteFile(Long fileId) {
        ConstructingProjectRiskFile rec = fileRepository.findById(fileId)
            .orElseThrow(() -> new RuntimeException("文件记录不存在，ID: " + fileId));
        Path target = resolveFilePath(fileId);
        try {
            if (Files.exists(target)) {
                Files.delete(target);
            }
            fileRepository.deleteById(fileId);
        } catch (Exception e) {
            throw new RuntimeException("删除文件失败: " + e.getMessage());
        }
    }

    /**
     * 函数级注释：按风险ID批量删除附件
     */
    public int deleteFilesByRiskId(Long riskId) {
        if (riskId == null) return 0;
        List<ConstructingProjectRiskFile> files = fileRepository.findByRiskId(riskId);
        int removed = 0;
        if (files != null) {
            for (ConstructingProjectRiskFile f : files) {
                try {
                    Path target = resolveFilePath(f.getFileId());
                    if (Files.exists(target)) {
                        Files.delete(target);
                    }
                    removed++;
                } catch (Exception ignore) {}
            }
        }
        try {
            fileRepository.deleteByRiskId(riskId);
        } catch (Exception ignore) {}
        return removed;
    }

    /**
     * 函数级注释：判断风险是否存在附件
     */
    @Transactional(readOnly = true)
    public boolean hasFiles(Long riskId) {
        if (riskId == null) return false;
        return fileRepository.existsByRiskId(riskId);
    }

    /**
     * 函数级注释：获取项目根目录
     */
    private Path getProjectRoot() {
        Path cwd = Paths.get("").toAbsolutePath();
        if (cwd.endsWith("backend")) {
            return cwd.getParent();
        }
        return cwd;
    }

    /**
     * 函数级注释：清理非法文件名字符
     */
    private String safe(String value) {
        return (value != null ? value.trim() : "").replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    /**
     * 函数级注释：清理非法字符并提供默认值
     */
    private String safe(String value, String fallback) {
        String v = value != null ? value.trim() : "";
        return v.isEmpty() ? fallback : v;
    }
}
