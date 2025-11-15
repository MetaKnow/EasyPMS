package com.missoft.pms.controller;

import com.missoft.pms.service.ConstructDeliverableFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpRange;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/construct-deliverable-files")
@CrossOrigin(origins = "*")
/**
 * 类级注释：项目交付物文件控制器
 * 提供上传、列表、下载接口。
 * 上传路径遵循 deliverableFiles/<项目编号-项目名称>/<里程碑名称>/[接口或个性化名称]/ 规则。
 */
public class ConstructDeliverableFileController {

    @Autowired
    private ConstructDeliverableFileService fileService;

    @Autowired
    private com.missoft.pms.repository.ConstructDeliverableFileRepository fileRepository;
    @Autowired
    private com.missoft.pms.repository.ProjectSstepRelationRepository relationRepository;
    @Autowired
    private com.missoft.pms.repository.StandardConstructStepRepository stepRepository;
    @Autowired
    private com.missoft.pms.repository.InterfaceRepository interfaceRepository;
    @Autowired
    private com.missoft.pms.repository.PersonalDevelopeRepository personalDevelopeRepository;
    @Autowired
    private com.missoft.pms.repository.ConstructMilestoneRepository constructMilestoneRepository;
    @Autowired
    private com.missoft.pms.repository.StandardDeliverableRepository standardDeliverableRepository;


    /**
     * 函数级注释：上传项目交付物文件。
     * 路径保存规则：deliverableFiles/<项目编号-项目名称>/<里程碑名称>/[接口或个性化名称]/。
     * - 当步骤为接口开发或个性化功能开发时，追加接口或个性化名称目录段（来自 interfaceId/personalDevId 或 projectStepId 的项目-步骤关系解析）。
     * - 文件名重命名为“交付物名称-步骤名称（如有）-当前日期时间”。
     */
    @PostMapping("/{projectId}/{deliverableId}/upload")
    public ResponseEntity<Map<String, Object>> upload(
            @PathVariable Long projectId,
            @PathVariable Long deliverableId,
            @RequestParam(value = "uploaderId", required = false) Long uploaderId,
            @RequestParam(value = "projectStepId", required = false) Long projectStepId,
            @RequestParam(value = "nstepId", required = false) Long nstepId,
            @RequestParam(value = "interfaceId", required = false) Long interfaceId,
            @RequestParam(value = "personalDevId", required = false) Long personalDevId,
            @RequestParam("files") MultipartFile[] files) {
        try {
            var saved = fileService.uploadFiles(projectId, deliverableId, uploaderId, projectStepId, nstepId, interfaceId, personalDevId, files);
            Map<String, Object> resp = new HashMap<>();
            resp.put("message", "文件上传成功");
            resp.put("count", saved.size());
            resp.put("files", saved);
            return ResponseEntity.ok(resp);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "文件上传失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "文件上传失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 函数级注释：列出项目交付物文件
     * 支持通过可选 query 参数 `projectStepId` 按步骤 relationId 过滤。
     * 示例：GET /api/construct-deliverable-files/{projectId}/{deliverableId}?projectStepId=123
     */
    @GetMapping("/{projectId}/{deliverableId}")
    public ResponseEntity<Map<String, Object>> list(
            @PathVariable Long projectId,
            @PathVariable Long deliverableId,
            @RequestParam(value = "projectStepId", required = false) Long projectStepId) {
        try {
            List<Map<String, Object>> files = fileService.listFiles(projectId, deliverableId, projectStepId);
            Map<String, Object> resp = new HashMap<>();
            resp.put("files", files);
            return ResponseEntity.ok(resp);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "获取文件列表失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "获取文件列表失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 下载项目交付物文件
     */
    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> download(@PathVariable Long fileId) {
        try {
            Path target = fileService.resolveFilePath(fileId);
            if (!Files.exists(target)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            Resource resource = new UrlResource(target.toUri());
            String contentType = Files.probeContentType(target);
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }
            // 使用 Spring 的 ContentDisposition 处理中文文件名编码（RFC 5987）
            String filename = target.getFileName().toString();
            ContentDisposition cd = ContentDisposition
                    .attachment()
                    .filename(filename, StandardCharsets.UTF_8)
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, cd.toString())
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 函数级注释：下载步骤交付物文件集合。
     * 行为：
     * - 若该步骤仅上传了一个文件，则直接回传该文件；
     * - 若上传了多个文件，则打包为ZIP返回；ZIP名称为“步骤名称.zip”，
     *   当为接口/个性化步骤时为“开发项名称-步骤名称.zip”。
     * 压缩结构：平铺文件，不再按交付物分子目录。
     */
    @GetMapping("/download/step/{projectId}/{relationId}")
    public ResponseEntity<StreamingResponseBody> downloadStepBundle(
            @PathVariable Long projectId,
            @PathVariable Long relationId) {
        try {
            var records = fileRepository.findByProjectIdAndProjectStepId(projectId, relationId);
            if (records == null || records.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(outputStream -> {});
            }

            // 解析步骤与开发项名称，用于压缩包命名
            String stepName = "步骤";
            String itemName = null;
            var rel = relationRepository.findById(relationId).orElse(null);
            if (rel != null && rel.getSstepId() != null) {
                var step = stepRepository.findById(rel.getSstepId()).orElse(null);
                if (step != null && step.getSstepName() != null) {
                    stepName = step.getSstepName().trim();
                }
                if (rel.getInterfaceId() != null) {
                    var iface = interfaceRepository.findById(rel.getInterfaceId()).orElse(null);
                    if (iface != null && iface.getIntegrationSysName() != null) {
                        itemName = iface.getIntegrationSysName().trim();
                    }
                } else if (rel.getPersonalDevId() != null) {
                    var pdev = personalDevelopeRepository.findById(rel.getPersonalDevId()).orElse(null);
                    if (pdev != null && pdev.getPersonalDevName() != null) {
                        itemName = pdev.getPersonalDevName().trim();
                    }
                }
            }
            String zipName = (itemName != null && !itemName.isEmpty() ? (itemName + "-") : "") + stepName + ".zip";

            // 单文件直接下载
            if (records.size() == 1) {
                var target = fileService.resolveFilePath(records.get(0).getFileId());
                if (!java.nio.file.Files.exists(target)) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(outputStream -> {});
                }
                org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(target.toUri());
                String contentType = java.nio.file.Files.probeContentType(target);
                if (contentType == null) contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
                String filename = target.getFileName().toString();
                ContentDisposition cd = ContentDisposition.attachment().filename(filename, java.nio.charset.StandardCharsets.UTF_8).build();
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, cd.toString())
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(outputStream -> {
                            try (var in = resource.getInputStream()) {
                                in.transferTo(outputStream);
                            }
                        });
            }

            // 多文件：压缩为ZIP
            StreamingResponseBody srb = outputStream -> {
                try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(outputStream)) {
                    for (var rec : records) {
                        var path = fileService.resolveFilePath(rec.getFileId());
                        if (!java.nio.file.Files.exists(path)) continue;
                        String entryName = path.getFileName().toString();
                        java.util.zip.ZipEntry entry = new java.util.zip.ZipEntry(entryName);
                        zos.putNextEntry(entry);
                        try (var in = java.nio.file.Files.newInputStream(path)) {
                            in.transferTo(zos);
                        }
                        zos.closeEntry();
                    }
                }
            };
            ContentDisposition cd = ContentDisposition.attachment().filename(zipName, java.nio.charset.StandardCharsets.UTF_8).build();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, cd.toString())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(srb);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(outputStream -> {
                try {
                    outputStream.write(("Failed: " + e.getMessage()).getBytes());
                } catch (Exception ignore) {}
            });
        }
    }

    /**
     * 函数级注释：下载里程碑交付物集合。
     * 行为：
     * - includeSteps=false：仅打包该里程碑的里程碑交付物；若仅一个文件则直接下载；
     * - includeSteps=true：将里程碑交付物放入“里程碑名称/”文件夹，将该里程碑所属步骤的交付物按
     *   “[开发项名称/]<步骤名称>/”分文件夹存放，统一打包为一个ZIP；若仅一个文件则直接下载。
     * 压缩包名称：默认“里程碑名称.zip”。
     */
    @GetMapping("/download/milestone/{projectId}/{milestoneId}")
    public ResponseEntity<StreamingResponseBody> downloadMilestoneBundle(
            @PathVariable Long projectId,
            @PathVariable Long milestoneId,
            @RequestParam(name = "includeSteps", defaultValue = "false") boolean includeSteps) {
        try {
            var all = fileRepository.findByProjectIdAndMilestoneId(projectId, milestoneId);
            if (all == null || all.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(outputStream -> {});
            }

            // 解析里程碑名称
            String milestoneName = constructMilestoneRepository.findById(milestoneId)
                    .map(m -> m.getMilestoneName() != null ? m.getMilestoneName().trim() : "里程碑")
                    .orElse("里程碑");

            // 分类：里程碑交付物 vs 步骤交付物
            java.util.Map<Long, String> typeByDeliverableId = new java.util.HashMap<>();
            for (var rec : all) {
                Long did = rec.getDeliverableId();
                if (did == null || typeByDeliverableId.containsKey(did)) continue;
                var d = standardDeliverableRepository.findById(did).orElse(null);
                String tp = d != null ? d.getDeliverableType() : null;
                typeByDeliverableId.put(did, tp);
            }
            java.util.List<com.missoft.pms.entity.ConstructDeliverableFile> milestoneFiles = new java.util.ArrayList<>();
            java.util.List<com.missoft.pms.entity.ConstructDeliverableFile> stepFiles = new java.util.ArrayList<>();
            for (var rec : all) {
                String tp = typeByDeliverableId.get(rec.getDeliverableId());
                if ("里程碑交付物".equals(tp)) milestoneFiles.add(rec); else stepFiles.add(rec);
            }

            java.util.List<com.missoft.pms.entity.ConstructDeliverableFile> working = includeSteps ? all : milestoneFiles;
            if (working.size() == 1) {
                var target = fileService.resolveFilePath(working.get(0).getFileId());
                if (!java.nio.file.Files.exists(target)) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(outputStream -> {});
                }
                org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(target.toUri());
                String contentType = java.nio.file.Files.probeContentType(target);
                if (contentType == null) contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
                String filename = target.getFileName().toString();
                ContentDisposition cd = ContentDisposition.attachment().filename(filename, java.nio.charset.StandardCharsets.UTF_8).build();
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, cd.toString())
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(outputStream -> {
                            try (var in = resource.getInputStream()) {
                                in.transferTo(outputStream);
                            }
                        });
            }

            // 多文件压缩：构建目录结构
            String zipName = milestoneName + ".zip";
            StreamingResponseBody srb = outputStream -> {
                try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(outputStream)) {
                    for (var rec : working) {
                        var path = fileService.resolveFilePath(rec.getFileId());
                        if (!java.nio.file.Files.exists(path)) continue;
                        String base = path.getFileName().toString();
                        String entryName;
                        String tp = typeByDeliverableId.get(rec.getDeliverableId());
                        if ("里程碑交付物".equals(tp)) {
                            // 调整：当同时下载所属步骤时，里程碑交付物直接位于ZIP根目录
                            entryName = includeSteps ? base : (milestoneName + "/" + base);
                        } else {
                            // 步骤文件：按 [开发项名称/]步骤名称/ 目录
                            String stepFolder = "步骤";
                            String itemFolder = null;
                            Long relId = rec.getProjectStepId();
                            if (relId != null) {
                                var relx = relationRepository.findById(relId).orElse(null);
                                if (relx != null && relx.getSstepId() != null) {
                                    var step = stepRepository.findById(relx.getSstepId()).orElse(null);
                                    if (step != null && step.getSstepName() != null) {
                                        stepFolder = step.getSstepName().trim();
                                    }
                                }
                                if (relx != null && relx.getInterfaceId() != null) {
                                    var iface = interfaceRepository.findById(relx.getInterfaceId()).orElse(null);
                                    if (iface != null && iface.getIntegrationSysName() != null) {
                                        itemFolder = iface.getIntegrationSysName().trim();
                                    }
                                } else if (relx != null && relx.getPersonalDevId() != null) {
                                    var pdev = personalDevelopeRepository.findById(relx.getPersonalDevId()).orElse(null);
                                    if (pdev != null && pdev.getPersonalDevName() != null) {
                                        itemFolder = pdev.getPersonalDevName().trim();
                                    }
                                }
                            }
                            if (itemFolder != null && !itemFolder.isEmpty()) {
                                entryName = itemFolder + "/" + stepFolder + "/" + base;
                            } else {
                                entryName = stepFolder + "/" + base;
                            }
                        }
                        java.util.zip.ZipEntry entry = new java.util.zip.ZipEntry(entryName);
                        zos.putNextEntry(entry);
                        try (var in = java.nio.file.Files.newInputStream(path)) {
                            in.transferTo(zos);
                        }
                        zos.closeEntry();
                    }
                }
            };
            ContentDisposition cd = ContentDisposition.attachment().filename(zipName, java.nio.charset.StandardCharsets.UTF_8).build();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, cd.toString())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(srb);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(outputStream -> {
                try {
                    outputStream.write(("Failed: " + e.getMessage()).getBytes());
                } catch (Exception ignore) {}
            });
        }
    }

    /**
     * 函数级注释：将指定文件转换为 PDF 并内联预览。
     * 支持 doc、docx、xls、xlsx、ppt、pptx、pdf。非 PDF 文件将通过项目内置的 LibreOffice 进行转换。
     * - 若已存在同名 PDF 且未过期，则直接使用；否则触发转换。
     * - 返回 `inline` 方式的 `application/pdf` 资源，便于前端 iframe 内嵌预览。
     */
    @GetMapping("/preview/pdf/{fileId}")
    public ResponseEntity<Resource> previewPdf(@PathVariable Long fileId) {
        try {
            Path input = fileService.resolveFilePath(fileId);
            if (!Files.exists(input)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            String filename = input.getFileName().toString();
            String lower = filename.toLowerCase();
            Path output;

            if (lower.endsWith(".pdf")) {
                output = input;
            } else if (lower.endsWith(".doc") || lower.endsWith(".docx")
                    || lower.endsWith(".xls") || lower.endsWith(".xlsx")
                    || lower.endsWith(".ppt") || lower.endsWith(".pptx")) {
                Path outDir = input.getParent();
                String base = filename.replaceFirst("\\.[^.]+$", "");
                Path expected = outDir.resolve(base + ".pdf");
                boolean needConvert = !Files.exists(expected)
                        || Files.getLastModifiedTime(expected).toMillis() < Files.getLastModifiedTime(input).toMillis();
                if (needConvert) {
                    output = convertToPdfWithLibreOffice(input, outDir);
                } else {
                    output = expected;
                }
            } else {
                // 不支持的文件类型
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).build();
            }

            if (!Files.exists(output)) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            Resource resource = new UrlResource(output.toUri());
            ContentDisposition cd = ContentDisposition
                    .inline()
                    .filename(output.getFileName().toString(), StandardCharsets.UTF_8)
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, cd.toString())
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(resource);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 函数级注释：在线播放 MP4 视频（支持 Range 断点续传与拖动）。
     * - 返回 inline 的 `video/mp4`，浏览器 <video> 可直接播放；
     * - 若带 `Range` 请求头，则返回 206 Partial Content 并携带 `Content-Range` 等头。
     */
    @GetMapping("/preview/video/{fileId}")
    public ResponseEntity<StreamingResponseBody> previewVideo(
            @PathVariable Long fileId,
            @RequestHeader(value = "Range", required = false) String rangeHeader) {
        try {
            Path input = fileService.resolveFilePath(fileId);
            if (!Files.exists(input)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            String filename = input.getFileName().toString().toLowerCase();
            if (!filename.endsWith(".mp4")) {
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).build();
            }

            long fileSize = Files.size(input);
            MediaType mediaType = MediaType.valueOf("video/mp4");
            ContentDisposition cd = ContentDisposition.inline()
                    .filename(input.getFileName().toString(), StandardCharsets.UTF_8)
                    .build();

            if (rangeHeader != null && !rangeHeader.isBlank()) {
                var ranges = HttpRange.parseRanges(rangeHeader);
                HttpRange range = ranges.get(0);
                long start = range.getRangeStart(fileSize);
                long end = range.getRangeEnd(fileSize);
                long length = end - start + 1;

                StreamingResponseBody body = outputStream -> {
                    try (var is = Files.newInputStream(input)) {
                        if (start > 0) {
                            long skipped = is.skip(start);
                            if (skipped < start) {
                                throw new IOException("无法跳过到指定位置: " + start);
                            }
                        }
                        byte[] buffer = new byte[8192];
                        long remaining = length;
                        while (remaining > 0) {
                            int read = is.read(buffer, 0, (int) Math.min(buffer.length, remaining));
                            if (read == -1) break;
                            outputStream.write(buffer, 0, read);
                            remaining -= read;
                        }
                    }
                };

                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                        .header(HttpHeaders.CONTENT_TYPE, mediaType.toString())
                        .header(HttpHeaders.CONTENT_DISPOSITION, cd.toString())
                        .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                        .header(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileSize)
                        .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(length))
                        .body(body);
            } else {
                StreamingResponseBody body = outputStream -> {
                    try (var is = Files.newInputStream(input)) {
                        byte[] buffer = new byte[8192];
                        int read;
                        while ((read = is.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, read);
                        }
                    }
                };
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, mediaType.toString())
                        .header(HttpHeaders.CONTENT_DISPOSITION, cd.toString())
                        .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                        .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileSize))
                        .body(body);
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 函数级注释：使用项目内置 LibreOffice 将输入文件转换为 PDF。
     * - 优先尝试从 backend 的上级目录 `../tools/LibreOffice/program/soffice(.exe)` 定位可执行文件；
     * - 若不存在，回退到当前目录 `./tools/...`；
     * - 不使用任何系统绝对路径回退，请将 LibreOffice 放在项目 tools 目录下。
     * 返回转换后 PDF 的实际路径。
     */
    private Path convertToPdfWithLibreOffice(Path input, Path outDir) throws IOException, InterruptedException {
        Path soffice = resolveSofficeExecutable();
        if (soffice == null || !Files.exists(soffice)) {
            throw new IOException("LibreOffice (soffice) 未找到，请将其放置在项目 tools/LibreOffice 下");
        }

        ProcessBuilder pb = new ProcessBuilder(
                soffice.toString(),
                "--headless",
                "--nologo",
                "--nolockcheck",
                "--nodefault",
                "--nofirststartwizard",
                "--convert-to", "pdf",
                "--outdir", outDir.toString(),
                input.toString()
        );
        pb.redirectErrorStream(true);

        Process p = pb.start();
        StringBuilder log = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            int lines = 0;
            while ((line = r.readLine()) != null && lines < 2000) {
                log.append(line).append('\n');
                lines++;
            }
        }

        boolean exited = p.waitFor(60, TimeUnit.SECONDS);
        if (!exited) {
            p.destroyForcibly();
            throw new IOException("LibreOffice 转换超时");
        }
        int code = p.exitValue();
        if (code != 0) {
            throw new IOException("LibreOffice 转换失败, code=" + code + "\n" + log);
        }

        // 输出文件通常为同名 .pdf
        String base = input.getFileName().toString().replaceFirst("\\.[^.]+$", "");
        Path realOut = outDir.resolve(base + ".pdf");
        if (!Files.exists(realOut)) {
            // 某些情况下 LibreOffice 可能输出到临时目录或不同命名，这里简单回退为预期路径检查失败。
            throw new IOException("未找到转换后的 PDF 文件: " + realOut);
        }
        return realOut;
    }

    /**
     * 函数级注释：尝试定位项目内或系统中的 soffice 可执行文件路径。
     */
    private Path resolveSofficeExecutable() {
        Path cwd = Paths.get(System.getProperty("user.dir"));
        // 优先从 backend 的父目录查找（通常项目根）
        Path candidate = cwd.resolve("..").resolve("tools").resolve("LibreOffice").resolve("program")
                .resolve(isWindows() ? "soffice.exe" : "soffice").normalize();
        if (Files.exists(candidate)) return candidate;

        // 其次尝试当前目录下的 tools
        Path candidate2 = cwd.resolve("tools").resolve("LibreOffice").resolve("program")
                .resolve(isWindows() ? "soffice.exe" : "soffice").normalize();
        if (Files.exists(candidate2)) return candidate2;

        // 不回退到系统绝对路径
        return null;
    }

    /**
     * 函数级注释：判断当前运行环境是否为 Windows。
     */
    private boolean isWindows() {
        String os = System.getProperty("os.name", "").toLowerCase();
        return os.contains("win");
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/{fileId}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long fileId) {
        try {
            fileService.deleteFile(fileId);
            Map<String, Object> resp = new HashMap<>();
            resp.put("message", "文件删除成功");
            resp.put("fileId", fileId);
            return ResponseEntity.ok(resp);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "删除文件失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "删除文件失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}