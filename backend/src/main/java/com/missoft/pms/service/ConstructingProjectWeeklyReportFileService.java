package com.missoft.pms.service;

import com.missoft.pms.entity.ConstructingProject;
import com.missoft.pms.entity.ConstructingProjectWeeklyReport;
import com.missoft.pms.entity.ConstructingProjectWeeklyReportFile;
import com.missoft.pms.repository.ConstructingProjectRepository;
import com.missoft.pms.repository.ConstructingProjectWeeklyReportFileRepository;
import com.missoft.pms.repository.ConstructingProjectWeeklyReportRepository;
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
 * 类级注释：项目周报附件服务
 */
@Service
@Transactional
public class ConstructingProjectWeeklyReportFileService {

    @Autowired
    private ConstructingProjectWeeklyReportFileRepository fileRepository;

    @Autowired
    private ConstructingProjectRepository constructingProjectRepository;

    @Autowired
    private ConstructingProjectWeeklyReportRepository weeklyReportRepository;

    /**
     * 函数级注释：上传项目周报附件并保存记录
     */
    public List<ConstructingProjectWeeklyReportFile> uploadFiles(Long projectId, Long weeklyReportId, Long uploaderId, MultipartFile[] files) {
        if (projectId == null || weeklyReportId == null) {
            throw new RuntimeException("项目ID与周报ID不能为空");
        }
        if (uploaderId == null) {
            throw new RuntimeException("上传人不能为空");
        }
        if (files == null || files.length == 0) {
            return new ArrayList<>();
        }

        ConstructingProject project = constructingProjectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("项目不存在，ID: " + projectId));
        ConstructingProjectWeeklyReport report = weeklyReportRepository.findById(weeklyReportId)
            .orElseThrow(() -> new RuntimeException("周报不存在，ID: " + weeklyReportId));

        String projNum = safe(project.getProjectNum(), "unknown");
        String projName = safe(project.getProjectName(), "未命名项目");
        String projectKey = safe(projNum) + "-" + safe(projName);
        if (report.getProjectId() != null && !report.getProjectId().equals(projectId)) {
            throw new RuntimeException("周报不属于当前项目");
        }

        Path root = getProjectRoot()
            .resolve("deliverableFiles")
            .resolve(projectKey)
            .resolve("项目周报")
            .resolve(String.valueOf(weeklyReportId));
        try {
            Files.createDirectories(root);
        } catch (Exception e) {
            throw new RuntimeException("创建附件目录失败: " + e.getMessage());
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
        List<ConstructingProjectWeeklyReportFile> saved = new ArrayList<>();
        String relativeDir = "deliverableFiles/" + projectKey + "/项目周报/" + weeklyReportId + "/";

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

            ConstructingProjectWeeklyReportFile rec = new ConstructingProjectWeeklyReportFile();
            rec.setProjectId(projectId);
            rec.setWeeklyReportId(weeklyReportId);
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
     * 函数级注释：获取周报附件列表
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> listFiles(Long weeklyReportId) {
        List<ConstructingProjectWeeklyReportFile> list = fileRepository.findByWeeklyReportId(weeklyReportId);
        List<Map<String, Object>> files = new ArrayList<>();
        for (ConstructingProjectWeeklyReportFile r : list) {
            Map<String, Object> info = new HashMap<>();
            info.put("fileId", r.getFileId());
            info.put("filePath", r.getFilePath());
            info.put("fileSize", r.getFileSize());
            info.put("uploadUser", r.getUploadUser());
            info.put("projectId", r.getProjectId());
            info.put("weeklyReportId", r.getWeeklyReportId());
            files.add(info);
        }
        return files;
    }

    /**
     * 函数级注释：解析附件在磁盘上的路径
     */
    public Path resolveFilePath(Long fileId) {
        ConstructingProjectWeeklyReportFile rec = fileRepository.findById(fileId)
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
        ConstructingProjectWeeklyReportFile rec = fileRepository.findById(fileId)
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
     * 函数级注释：按周报ID批量删除附件
     */
    public int deleteFilesByWeeklyReportId(Long weeklyReportId) {
        if (weeklyReportId == null) return 0;
        List<ConstructingProjectWeeklyReportFile> files = fileRepository.findByWeeklyReportId(weeklyReportId);
        int removed = 0;
        if (files != null) {
            for (ConstructingProjectWeeklyReportFile f : files) {
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
            fileRepository.deleteByWeeklyReportId(weeklyReportId);
        } catch (Exception ignore) {}
        return removed;
    }

    /**
     * 函数级注释：判断周报是否存在附件
     */
    @Transactional(readOnly = true)
    public boolean hasFiles(Long weeklyReportId) {
        if (weeklyReportId == null) return false;
        return fileRepository.existsByWeeklyReportId(weeklyReportId);
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
