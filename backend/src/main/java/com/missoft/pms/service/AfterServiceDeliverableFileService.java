package com.missoft.pms.service;

import com.missoft.pms.entity.AfterServiceDeliverable;
import com.missoft.pms.entity.AfterServiceEvent;
import com.missoft.pms.entity.AfterServiceProject;
import com.missoft.pms.repository.AfterServiceDeliverableRepository;
import com.missoft.pms.repository.AfterServiceEventRepository;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class AfterServiceDeliverableFileService {

    @Autowired
    private AfterServiceDeliverableRepository fileRepository;

    @Autowired
    private AfterServiceProjectRepository projectRepository;

    @Autowired
    private AfterServiceEventRepository eventRepository;

    private Path getProjectRoot() {
        Path cwd = Paths.get("").toAbsolutePath();
        if (cwd.endsWith("backend")) {
            return cwd.getParent();
        }
        return cwd;
    }

    /**
     * 上传附件（可多文件）
     */
    public List<AfterServiceDeliverable> upload(Long projectId, Long eventId, Long uploaderId, MultipartFile[] files) {
        if (files == null || files.length == 0) return new ArrayList<>();

        AfterServiceProject project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("运维项目不存在，ID: " + projectId));
        AfterServiceEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("运维事件不存在，ID: " + eventId));

        String projNum = safe(project.getProjectNum());
        String projName = safe(project.getProjectName());
        String projectKey = projNum + "-" + projName;

        String eventName = safe(event.getEventName());
        String eventDateStr = (event.getEventDate() != null) ? event.getEventDate().toString() : "未知日期";
        String safeEventDate = safe(eventDateStr);
        String eventFolder = safeEventDate + "-" + eventName;

        Path repoRoot = getProjectRoot();
        Path root = repoRoot.resolve("afterServiceDeliverableFiles").resolve(projectKey).resolve(eventFolder);
        try {
            Files.createDirectories(root);
        } catch (Exception e) {
            throw new RuntimeException("创建附件目录失败: " + e.getMessage());
        }
        String relativeDir = "afterServiceDeliverableFiles/" + projectKey + "/" + eventFolder + "/";

        // 使用到毫秒的时间戳，避免同秒内多文件覆盖；若仍重名再追加序号
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");
        int seq = 1;
        List<AfterServiceDeliverable> saved = new ArrayList<>();
        for (MultipartFile f : files != null ? files : new MultipartFile[0]) {
            if (f == null || f.isEmpty()) continue;
            String originalName = Paths.get(f.getOriginalFilename()).getFileName().toString();
            String ext = "";
            int dot = originalName.lastIndexOf('.');
            if (dot >= 0) ext = originalName.substring(dot);
            String ts = LocalDateTime.now().format(fmt);
            String baseName = eventName + "-" + ts;
            String finalName = baseName + ext;
            Path target = root.resolve(finalName).normalize();
            while (Files.exists(target)) {
                finalName = baseName + "-" + (seq++) + ext;
                target = root.resolve(finalName).normalize();
            }
            if (!target.startsWith(root)) {
                throw new RuntimeException("非法文件名");
            }
            try (InputStream in = f.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                throw new RuntimeException("保存附件失败: " + originalName);
            }
            AfterServiceDeliverable rec = new AfterServiceDeliverable();
            rec.setProjectId(projectId);
            rec.setEventId(eventId);
            rec.setUploadUser(uploaderId);
            // 交付物名称与重命名后的文件名称保持一致（包含扩展名）
            rec.setDeliverableName(finalName);
            rec.setFilePath(relativeDir + finalName);
            try { rec.setFileSize(Files.size(target)); } catch (Exception ex) { rec.setFileSize(0L); }
            saved.add(fileRepository.save(rec));
        }
        return saved;
    }

    public List<Map<String, Object>> list(Long projectId, Long eventId) {
        var records = fileRepository.findByProjectIdAndEventId(projectId, eventId);
        List<Map<String, Object>> list = new ArrayList<>();
        for (var r : records) {
            Map<String, Object> m = new HashMap<>();
            m.put("fileId", r.getDeliverableId());
            m.put("deliverableName", r.getDeliverableName());
            m.put("filePath", r.getFilePath());
            m.put("fileSize", r.getFileSize());
            list.add(m);
        }
        return list;
    }

    public Path resolveFilePath(Long fileId) {
        var rec = fileRepository.findById(fileId).orElseThrow(() -> new RuntimeException("文件记录不存在"));
        Path repoRoot = getProjectRoot();
        Path target = repoRoot.resolve(rec.getFilePath()).normalize();
        if (!target.startsWith(repoRoot)) {
            throw new RuntimeException("非法路径");
        }
        return target;
    }

    public void delete(Long fileId) {
        var rec = fileRepository.findById(fileId).orElseThrow(() -> new RuntimeException("文件记录不存在"));
        Path repoRoot = getProjectRoot();
        Path target = repoRoot.resolve(rec.getFilePath()).normalize();
        try {
            Files.deleteIfExists(target);
        } catch (Exception ignore) {}
        fileRepository.deleteById(fileId);
    }

    /**
     * 根据项目与事件删除该事件下的所有附件文件与数据库记录。
     * 注意：仅删除 afterService_deliverable 表记录及其对应的物理文件，不影响其他数据。
     * @return 实际删除的文件记录数量
     */
    public int deleteByEvent(Long projectId, Long eventId) {
        if (projectId == null || eventId == null) {
            return 0;
        }
        List<AfterServiceDeliverable> list = fileRepository.findByProjectIdAndEventId(projectId, eventId);
        if (list == null || list.isEmpty()) {
            return 0;
        }
        Path repoRoot = getProjectRoot();
        for (AfterServiceDeliverable rec : list) {
            String path = rec != null ? rec.getFilePath() : null;
            if (path == null || path.isBlank()) continue;
            Path target = repoRoot.resolve(path).normalize();
            if (!target.startsWith(repoRoot)) continue; // 安全保护
            try {
                Files.deleteIfExists(target);
            } catch (Exception ignore) {}
        }
        // 删除数据库记录
        try {
            fileRepository.deleteAll(list);
        } catch (Exception ignore) {}

        // 试图清理事件目录（若为空则删除，不为空则忽略）
        try {
            AfterServiceProject project = projectRepository.findById(projectId).orElse(null);
            AfterServiceEvent event = eventRepository.findById(eventId).orElse(null);
            if (project != null && event != null) {
                String projectKey = safe(project.getProjectNum()) + "-" + safe(project.getProjectName());
                String eventFolder = safe(event.getEventDate() != null ? event.getEventDate().toString() : "未知日期") + "-" + safe(event.getEventName());
                Path dir = repoRoot.resolve("afterServiceDeliverableFiles").resolve(projectKey).resolve(eventFolder);
                try {
                    Files.delete(dir); // 若非空会抛出异常，忽略即可
                } catch (Exception ignore) {}
            }
        } catch (Exception ignore) {}

        return list.size();
    }

    private String safe(String s) {
        if (s == null) return "";
        return s.trim().replaceAll("[\\/:*?\"<>|]", "_");
    }
}
