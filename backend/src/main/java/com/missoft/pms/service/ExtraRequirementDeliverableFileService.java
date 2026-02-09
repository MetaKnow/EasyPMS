package com.missoft.pms.service;

import com.missoft.pms.entity.ConstructingProject;
import com.missoft.pms.entity.ExtraRequirement;
import com.missoft.pms.entity.ExtraRequirementDeliverableFile;
import com.missoft.pms.repository.ConstructingProjectRepository;
import com.missoft.pms.repository.ExtraRequirementDeliverableFileRepository;
import com.missoft.pms.repository.ExtraRequirementRepository;
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
 * 类级注释：
 * 合同外需求附件服务，负责上传、列表、下载与删除。
 */
@Service
@Transactional
public class ExtraRequirementDeliverableFileService {

    @Autowired
    private ExtraRequirementDeliverableFileRepository fileRepository;

    @Autowired
    private ConstructingProjectRepository constructingProjectRepository;

    @Autowired
    private ExtraRequirementRepository extraRequirementRepository;

    /**
     * 函数级注释：上传合同外需求附件
     * @param projectId 项目ID
     * @param requirementId 合同外需求ID
     * @param uploaderName 上传人名称
     * @param files 文件数组
     * @return 保存的文件记录
     */
    public List<ExtraRequirementDeliverableFile> uploadFiles(Long projectId, Long requirementId, String uploaderName, MultipartFile[] files) {
        if (projectId == null || requirementId == null) {
            throw new RuntimeException("项目ID与需求ID不能为空");
        }
        if (files == null || files.length == 0) {
            return new ArrayList<>();
        }

        ConstructingProject project = constructingProjectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("项目不存在，ID: " + projectId));
        ExtraRequirement requirement = extraRequirementRepository.findById(requirementId)
            .orElseThrow(() -> new RuntimeException("需求不存在，ID: " + requirementId));

        String projNum = safe(project.getProjectNum(), "unknown");
        String projName = safe(project.getProjectName(), "未命名项目");
        String projectKey = safe(projNum) + "-" + safe(projName);
        if (requirement.getProjectId() != null && !requirement.getProjectId().equals(projectId)) {
            throw new RuntimeException("需求不属于当前项目");
        }
        String requirementName = safe(requirement.getRequirementName(), "未命名需求");
        String safeUploader = (uploaderName != null && !uploaderName.trim().isEmpty()) ? uploaderName.trim() : "未知";

        Path root = getProjectRoot()
            .resolve("deliverableFiles")
            .resolve(projectKey)
            .resolve("合同外需求")
            .resolve(requirementName);
        try {
            Files.createDirectories(root);
        } catch (Exception e) {
            throw new RuntimeException("创建附件目录失败: " + e.getMessage());
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
        List<ExtraRequirementDeliverableFile> saved = new ArrayList<>();
        String relativeDir = "deliverableFiles/" + projectKey + "/合同外需求/" + requirementName + "/";

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

            ExtraRequirementDeliverableFile rec = new ExtraRequirementDeliverableFile();
            rec.setProjectId(projectId);
            rec.setRequirementId(requirementId);
            rec.setUploadUser(safeUploader);
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
     * 函数级注释：列出合同外需求附件列表
     * @param requirementId 需求ID
     * @return 附件列表
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> listFiles(Long requirementId) {
        List<ExtraRequirementDeliverableFile> list = fileRepository.findByRequirementId(requirementId);
        List<Map<String, Object>> files = new ArrayList<>();
        for (ExtraRequirementDeliverableFile r : list) {
            Map<String, Object> info = new HashMap<>();
            info.put("fileId", r.getFileId());
            info.put("filePath", r.getFilePath());
            info.put("fileSize", r.getFileSize());
            info.put("uploadUser", r.getUploadUser());
            info.put("projectId", r.getProjectId());
            info.put("requirementId", r.getRequirementId());
            files.add(info);
        }
        return files;
    }

    /**
     * 函数级注释：解析文件路径
     * @param fileId 文件ID
     * @return 绝对路径
     */
    public Path resolveFilePath(Long fileId) {
        ExtraRequirementDeliverableFile rec = fileRepository.findById(fileId)
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
     * 函数级注释：删除附件文件与记录
     * @param fileId 文件ID
     */
    public void deleteFile(Long fileId) {
        ExtraRequirementDeliverableFile rec = fileRepository.findById(fileId)
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
     * 函数级注释：删除某个需求下的全部附件（含文件）
     * @param requirementId 需求ID
     * @return 删除数量
     */
    public int deleteFilesByRequirementId(Long requirementId) {
        if (requirementId == null) return 0;
        List<ExtraRequirementDeliverableFile> files = fileRepository.findByRequirementId(requirementId);
        int removed = 0;
        if (files != null) {
            for (ExtraRequirementDeliverableFile f : files) {
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
            fileRepository.deleteByRequirementId(requirementId);
        } catch (Exception ignore) {}
        return removed;
    }

    private Path getProjectRoot() {
        Path cwd = Paths.get("").toAbsolutePath();
        if (cwd.endsWith("backend")) {
            return cwd.getParent();
        }
        return cwd;
    }

    private String safe(String value) {
        return (value != null ? value.trim() : "").replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    private String safe(String value, String fallback) {
        String v = value != null ? value.trim() : "";
        return v.isEmpty() ? fallback : v;
    }
}
