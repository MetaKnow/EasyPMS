package com.missoft.pms.service;

import com.missoft.pms.entity.ConstructDeliverableFile;
import com.missoft.pms.repository.ConstructDeliverableFileRepository;
import com.missoft.pms.entity.ConstructingProject;
import com.missoft.pms.entity.StandardDeliverable;
import com.missoft.pms.entity.StandardConstructStep;
import com.missoft.pms.entity.StandardMilestone;
import com.missoft.pms.entity.ConstructMilestone;
import com.missoft.pms.entity.InterfaceEntity;
import com.missoft.pms.entity.PersonalDevelope;
import com.missoft.pms.entity.ProjectSstepRelation;
import com.missoft.pms.repository.ConstructingProjectRepository;
import com.missoft.pms.repository.StandardDeliverableRepository;
import com.missoft.pms.repository.StandardConstructStepRepository;
import com.missoft.pms.repository.StandardMilestoneRepository;
import com.missoft.pms.repository.ConstructMilestoneRepository;
import com.missoft.pms.repository.ProjectSstepRelationRepository;
import com.missoft.pms.repository.InterfaceRepository;
import com.missoft.pms.repository.PersonalDevelopeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.List;

@Service
@Transactional
public class ConstructDeliverableFileService {
    /**
     * 类级注释：
     * 项目交付物文件服务，负责上传、列表、下载与删除。
     * 路径规则：保存至项目根目录 `deliverableFiles/<项目编号-项目名称>/<里程碑名称>/[接口或个性化名称]/`。
     * - 当步骤为“接口开发”或“个性化功能开发”时，依据项目-步骤关系追加对应的接口名称或个性化名称目录段；
     * - 其他步骤保持现有结构（不追加）。
     * 文件重命名规则：`交付物名称-步骤名称（如有）-yyyyMMdd-HHmmss`，保留原扩展名。
     */

    @Autowired
    private ConstructDeliverableFileRepository fileRepository;

    @Autowired
    private ConstructingProjectRepository constructingProjectRepository;

    @Autowired
    private StandardDeliverableRepository standardDeliverableRepository;

    @Autowired
    private StandardConstructStepRepository standardConstructStepRepository;

    @Autowired
    private StandardMilestoneRepository standardMilestoneRepository;

    @Autowired
    private ProjectSstepRelationRepository projectSstepRelationRepository;

    @Autowired
    private InterfaceRepository interfaceRepository;

    @Autowired
    private PersonalDevelopeRepository personalDevelopeRepository;

    @Autowired
    private ConstructMilestoneRepository constructMilestoneRepository;

    /**
     * 函数级注释：上传并保存项目交付物文件。
     * 路径规则：保存到项目根目录的 `deliverableFiles/<项目编号-项目名称>/<里程碑名称>/[接口或个性化名称]/`。
     * - 若提供 `interfaceId` 或 `personalDevId`，优先使用它们解析名称；
     * - 若未提供但有 `projectStepId`，通过 `project_sstep_relations` 解析对应的接口或个性化名称；
     * - 若无法解析到接口/个性化名称，则不追加该目录段。
     * 重命名规则：文件名使用“交付物名称-步骤名称（如有）-当前日期时间”并保留原扩展名。
     * 存储规则：同时写入 `projectId`、`deliverableId`、`projectStepId`/`nstepId` 以及项目里程碑 `construct_milestone.milestoneId` 外键字段。
     *
     * @param projectId      项目ID
     * @param deliverableId  标准交付物ID
     * @param uploaderId     上传人ID（必填，user.userId）。
     * @param projectStepId  项目步骤关系ID（project_sstep_relations.relationId）
     * @param nstepId        非标准步骤ID（预留）
     * @param interfaceId    接口ID（可选）
     * @param personalDevId  个性化开发ID（可选）
     * @param files          上传的文件数组
     * @return 保存的文件记录列表
     */
    public List<ConstructDeliverableFile> uploadFiles(Long projectId, Long deliverableId,
                                                      Long uploaderId, Long projectStepId, Long nstepId,
                                                      Long interfaceId, Long personalDevId,
                                                      MultipartFile[] files) {
        if (projectId == null || deliverableId == null) {
            throw new RuntimeException("项目ID和交付物ID不能为空");
        }
        // 明确校验上传人，数据库约束 uploadUser NOT NULL 且为 user.userId 外键
        if (uploaderId == null) {
            throw new RuntimeException("上传人ID不能为空");
        }

        // 读取项目信息以构建 <项目编号-项目名称>
        ConstructingProject project = constructingProjectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("项目不存在，ID: " + projectId));
        String projNum = Optional.ofNullable(project.getProjectNum()).orElse("unknown").trim();
        String projName = Optional.ofNullable(project.getProjectName()).orElse("未命名项目").trim();
        String safeProjNum = projNum.replaceAll("[\\\\/:*?\"<>|]", "_");
        String safeProjName = projName.replaceAll("[\\\\/:*?\"<>|]", "_");
        String projectKey = safeProjNum + "-" + safeProjName;

        // 读取交付物信息与步骤/里程碑信息以构建里程碑名称与重命名前缀
        StandardDeliverable deliverable = standardDeliverableRepository.findById(deliverableId)
                .orElseThrow(() -> new RuntimeException("标准交付物不存在，ID: " + deliverableId));
        String deliverableName = Optional.ofNullable(deliverable.getDeliverableName()).orElse("交付物").trim();
        String safeDeliverableName = deliverableName.replaceAll("[\\\\/:*?\"<>|]", "_");

        String stepName = null;
        Long smilestoneId = null;
        Long sstepIdFromRelation = null;
        if (projectStepId != null) {
            ProjectSstepRelation rel = projectSstepRelationRepository.findById(projectStepId).orElse(null);
            if (rel != null && rel.getSstepId() != null) {
                sstepIdFromRelation = rel.getSstepId();
                StandardConstructStep step = standardConstructStepRepository.findById(sstepIdFromRelation)
                        .orElse(null);
                if (step != null) {
                    stepName = Optional.ofNullable(step.getSstepName()).orElse(null);
                    smilestoneId = step.getSmilestoneId();
                }
            }
        }
        if (smilestoneId == null) {
            smilestoneId = deliverable.getMilestoneId();
        }
        String milestoneName = null;
        if (smilestoneId != null) {
            StandardMilestone sm = standardMilestoneRepository.findById(smilestoneId).orElse(null);
            if (sm != null) {
                milestoneName = Optional.ofNullable(sm.getMilestoneName()).orElse(null);
            }
        }
        String safeStepName = stepName == null ? null : stepName.replaceAll("[\\\\/:*?\"<>|]", "_");
        String safeMilestoneName = Optional.ofNullable(milestoneName).orElse("未分组里程碑").trim()
                .replaceAll("[\\\\/:*?\"<>|]", "_");

        // 解析项目里程碑ID（construct_milestone.milestoneId），用于外键写入
        Long constructMilestoneId = null;
        if (milestoneName != null && projectId != null) {
            try {
                List<ConstructMilestone> cms = constructMilestoneRepository
                        .findByProjectIdAndMilestoneNames(projectId, List.of(milestoneName.trim()));
                if (cms != null && !cms.isEmpty()) {
                    constructMilestoneId = cms.get(0).getMilestoneId();
                }
            } catch (Exception ignore) {}
        }

        // 解析接口/个性化名称（当为接口开发或个性化功能开发步骤时追加目录段）
        String itemFolderName = null;
        // 优先使用显式传入的 ID
        Long finalInterfaceId = interfaceId;
        Long finalPersonalDevId = personalDevId;
        if ((finalInterfaceId == null && finalPersonalDevId == null) && projectStepId != null) {
            ProjectSstepRelation rel = projectSstepRelationRepository.findById(projectStepId).orElse(null);
            if (rel != null) {
                if (rel.getInterfaceId() != null) {
                    finalInterfaceId = rel.getInterfaceId();
                } else if (rel.getPersonalDevId() != null) {
                    finalPersonalDevId = rel.getPersonalDevId();
                }
            }
        }

        if (finalInterfaceId != null) {
            InterfaceEntity iface = interfaceRepository.findById(finalInterfaceId).orElse(null);
            String ifaceName = iface != null && iface.getIntegrationSysName() != null
                    ? iface.getIntegrationSysName().trim() : ("接口-" + finalInterfaceId);
            itemFolderName = ifaceName.replaceAll("[\\\\/:*?\"<>|]", "_");
        } else if (finalPersonalDevId != null) {
            PersonalDevelope pdev = personalDevelopeRepository.findById(finalPersonalDevId).orElse(null);
            String pdevName = pdev != null && pdev.getPersonalDevName() != null
                    ? pdev.getPersonalDevName().trim() : ("个性化开发-" + finalPersonalDevId);
            itemFolderName = pdevName.replaceAll("[\\\\/:*?\"<>|]", "_");
        }

        // 目标目录：deliverableFiles/<项目编号-项目名称>/<里程碑名称>/[接口或个性化名称]/
        List<ConstructDeliverableFile> saved = new ArrayList<>();
        Path projectRoot = Paths.get("").toAbsolutePath().getParent();
        Path root = projectRoot
                .resolve("deliverableFiles")
                .resolve(projectKey)
                .resolve(safeMilestoneName);
        if (itemFolderName != null && !itemFolderName.isEmpty()) {
            root = root.resolve(itemFolderName);
        }
        try {
            Files.createDirectories(root);
        } catch (Exception e) {
            throw new RuntimeException("创建文件目录失败: " + e.getMessage());
        }
        String relativeDir = "deliverableFiles/" + projectKey + "/" + safeMilestoneName + "/" +
                (itemFolderName != null && !itemFolderName.isEmpty() ? (itemFolderName + "/") : "");

        // 时间戳格式：yyyyMMdd-HHmmss
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

        if (files != null) {
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) continue;
                String originalName = Paths.get(file.getOriginalFilename()).getFileName().toString();
                String ext = "";
                int dot = originalName.lastIndexOf('.');
                if (dot >= 0) {
                    ext = originalName.substring(dot); // 包含点
                }
                String ts = LocalDateTime.now().format(fmt);
                String base = safeDeliverableName + (safeStepName != null && !safeStepName.isEmpty() ? ("-" + safeStepName) : "") + "-" + ts;
                String finalName = base + ext;

                Path target = root.resolve(finalName).normalize();
                if (!target.startsWith(root)) {
                    throw new RuntimeException("非法文件名");
                }
                try (InputStream in = file.getInputStream()) {
                    Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception e) {
                    throw new RuntimeException("保存文件失败: " + originalName);
                }
                ConstructDeliverableFile record = new ConstructDeliverableFile();
                record.setProjectId(projectId);
                record.setDeliverableId(deliverableId);
                record.setUploaderId(uploaderId);
                record.setProjectStepId(projectStepId);
                record.setNstepId(nstepId);
                // 写入项目里程碑外键（construct_milestone.milestoneId），当无法解析时允许为空
                record.setMilestoneId(constructMilestoneId);
                record.setFilePath(relativeDir + finalName);
                try {
                    record.setFileSize(Files.size(target));
                } catch (Exception ex) {
                    record.setFileSize(0L);
                }
                saved.add(fileRepository.save(record));
            }
        }
        return saved;
    }

    /**
     * 函数级注释：列出项目交付物文件
     * 支持按步骤 relationId（projectStepId）过滤，用于“上传交付物”界面加载当前步骤已上传文件。
     * 若未提供 projectStepId，则返回该项目下该交付物的全部记录。
     * @param projectId 项目ID
     * @param deliverableId 交付物ID
     * @param projectStepId 项目-步骤关系ID（可选）
     * @return 文件信息列表（包含 projectStepId、deliverableId、milestoneId、尺寸等）
     */
    private Path getProjectRoot() {
        Path cwd = Paths.get("").toAbsolutePath();
        if (cwd.endsWith("backend")) {
            return cwd.getParent();
        }
        return cwd;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listFiles(Long projectId, Long deliverableId, Long projectStepId) {
        List<ConstructDeliverableFile> records;
        if (projectStepId != null) {
            records = fileRepository.findByProjectIdAndDeliverableIdAndProjectStepId(projectId, deliverableId, projectStepId);
        } else {
            records = fileRepository.findByProjectIdAndDeliverableId(projectId, deliverableId);
        }
        List<Map<String, Object>> files = new ArrayList<>();
        Path projectRoot = getProjectRoot();
        for (ConstructDeliverableFile r : records) {
            Map<String, Object> info = new HashMap<>();
            // 函数级注释：为前端查看弹窗提供必要的上下文字段
            // - 返回 projectStepId：用于接口/个性化步骤的过滤（relationId）
            // - 返回 deliverableId：前端映射到指定交付物的文件列表
            // - 返回 milestoneId：用于里程碑查看过滤
            info.put("projectStepId", r.getProjectStepId());
            info.put("deliverableId", r.getDeliverableId());
            info.put("fileId", r.getFileId());
            info.put("filePath", r.getFilePath());
            info.put("fileSize", r.getFileSize());
            info.put("milestoneId", r.getMilestoneId());
            Path target = projectRoot.resolve(r.getFilePath()).normalize();
            try {
                info.put("exists", Files.exists(target));
                if (Files.exists(target)) {
                    info.put("lastModified", Files.getLastModifiedTime(target).toMillis());
                }
            } catch (Exception e) {
                info.put("exists", false);
            }
            files.add(info);
        }
        return files;
    }

    @Transactional(readOnly = true)
    public Path resolveFilePath(Long fileId) {
        ConstructDeliverableFile r = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("文件记录不存在，ID: " + fileId));
        Path projectRoot = getProjectRoot();
        return projectRoot.resolve(r.getFilePath()).normalize();
    }

    public void deleteFile(Long fileId) {
        ConstructDeliverableFile r = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("文件记录不存在，ID: " + fileId));
        // 函数级注释：禁止在“已完成”项目下删除交付物文件
        Long projectId = r.getProjectId();
        if (projectId != null) {
            ConstructingProject project = constructingProjectRepository.findById(projectId).orElse(null);
            if (project != null && "已完成".equals(project.getProjectState())) {
                throw new RuntimeException("已完成项目不允许删除交付物文件");
            }
        }
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
}
