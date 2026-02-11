package com.missoft.pms.service;

import com.missoft.pms.entity.AfterServiceLead;
import com.missoft.pms.entity.AfterServiceLeadDeliverableFile;
import com.missoft.pms.entity.AfterServiceProject;
import com.missoft.pms.repository.AfterServiceLeadDeliverableFileRepository;
import com.missoft.pms.repository.AfterServiceLeadRepository;
import com.missoft.pms.repository.AfterServiceProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class AfterServiceLeadDeliverableFileService {

    @Autowired
    private AfterServiceLeadDeliverableFileRepository fileRepository;

    @Autowired
    private AfterServiceProjectRepository projectRepository;

    @Autowired
    private AfterServiceLeadRepository leadRepository;

    public List<AfterServiceLeadDeliverableFile> uploadFiles(Long projectId, Long leadsId, Long uploaderId, MultipartFile[] files) {
        if (leadsId == null) {
            throw new RuntimeException("销售线索ID不能为空");
        }
        if (uploaderId == null) {
            throw new RuntimeException("上传人不能为空");
        }
        if (files == null || files.length == 0) {
            return new ArrayList<>();
        }

        AfterServiceLead lead = leadRepository.findById(leadsId)
                .orElseThrow(() -> new RuntimeException("销售线索不存在，ID: " + leadsId));
        Long effectiveProjectId = lead.getAfterServiceProjectId() != null ? lead.getAfterServiceProjectId() : projectId;
        if (effectiveProjectId == null) {
            throw new RuntimeException("项目ID不能为空");
        }
        AfterServiceProject project = projectRepository.findById(effectiveProjectId)
                .orElseThrow(() -> new RuntimeException("运维项目不存在，ID: " + effectiveProjectId));

        String projNum = safe(project.getProjectNum(), "unknown");
        String projName = safe(project.getProjectName(), "未命名项目");
        String projectKey = safe(projNum) + "-" + safe(projName);
        LocalDate createDate = lead.getCreateTime() != null ? lead.getCreateTime().toLocalDate() : LocalDate.now();
        String dateFolder = safe(createDate.toString());

        Path root = getProjectRoot()
                .resolve("afterServiceDeliverableFiles")
                .resolve(projectKey)
                .resolve("销售线索")
                .resolve(dateFolder);
        try {
            Files.createDirectories(root);
        } catch (Exception e) {
            throw new RuntimeException("创建附件目录失败: " + e.getMessage());
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");
        List<AfterServiceLeadDeliverableFile> saved = new ArrayList<>();
        String relativeDir = "afterServiceDeliverableFiles/" + projectKey + "/销售线索/" + dateFolder + "/";

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

            AfterServiceLeadDeliverableFile rec = new AfterServiceLeadDeliverableFile();
            rec.setAfterServiceProjectId(effectiveProjectId);
            rec.setLeadsId(leadsId);
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

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listFiles(Long leadsId) {
        List<AfterServiceLeadDeliverableFile> list = fileRepository.findByLeadsId(leadsId);
        List<Map<String, Object>> files = new ArrayList<>();
        for (AfterServiceLeadDeliverableFile r : list) {
            Map<String, Object> info = new HashMap<>();
            info.put("fileId", r.getFileId());
            info.put("filePath", r.getFilePath());
            info.put("fileSize", r.getFileSize());
            info.put("uploadUser", r.getUploadUser());
            info.put("afterServiceProjectId", r.getAfterServiceProjectId());
            info.put("leadsId", r.getLeadsId());
            files.add(info);
        }
        return files;
    }

    public Path resolveFilePath(Long fileId) {
        AfterServiceLeadDeliverableFile rec = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("文件记录不存在，ID: " + fileId));
        String relative = rec.getFilePath() != null ? rec.getFilePath() : "";
        Path root = getProjectRoot();
        Path target = root.resolve(relative).normalize();
        if (!target.startsWith(root)) {
            throw new RuntimeException("非法文件路径");
        }
        return target;
    }

    public void deleteFile(Long fileId) {
        fileRepository.findById(fileId)
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

    public void deleteFilesByLeadId(Long leadsId) {
        if (leadsId == null) return;
        List<AfterServiceLeadDeliverableFile> files = fileRepository.findByLeadsId(leadsId);
        for (AfterServiceLeadDeliverableFile file : files) {
            if (file == null || file.getFileId() == null) continue;
            deleteFile(file.getFileId());
        }
    }

    @Transactional(readOnly = true)
    public boolean hasFiles(Long leadsId) {
        if (leadsId == null) return false;
        return fileRepository.existsByLeadsId(leadsId);
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
