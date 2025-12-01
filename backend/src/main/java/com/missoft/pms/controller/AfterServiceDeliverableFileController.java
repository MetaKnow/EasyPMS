package com.missoft.pms.controller;

import com.missoft.pms.service.AfterServiceDeliverableFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/afterservice-deliverable-files")
@CrossOrigin(origins = "*")
public class AfterServiceDeliverableFileController {

    @Autowired
    private AfterServiceDeliverableFileService fileService;

    /** 上传运维事件附件 */
    @PostMapping("/{projectId}/{eventId}/upload")
    public ResponseEntity<Map<String, Object>> upload(
            @PathVariable Long projectId,
            @PathVariable Long eventId,
            @RequestParam(value = "uploaderId", required = false) Long uploaderId,
            @RequestParam("files") MultipartFile[] files) {
        try {
            var saved = fileService.upload(projectId, eventId, uploaderId, files);
            Map<String, Object> resp = new HashMap<>();
            resp.put("message", "附件上传成功");
            resp.put("count", saved.size());
            resp.put("files", saved);
            return ResponseEntity.ok(resp);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "附件上传失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "附件上传失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /** 列出事件附件 */
    @GetMapping("/{projectId}/{eventId}")
    public ResponseEntity<Map<String, Object>> list(
            @PathVariable Long projectId,
            @PathVariable Long eventId) {
        try {
            List<Map<String, Object>> files = fileService.list(projectId, eventId);
            Map<String, Object> resp = new HashMap<>();
            resp.put("files", files);
            return ResponseEntity.ok(resp);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "获取附件列表失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "获取附件列表失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /** 预览附件（内嵌显示） */
    @GetMapping("/preview/{fileId}")
    public ResponseEntity<Resource> preview(@PathVariable Long fileId) {
        try {
            Path target = fileService.resolveFilePath(fileId);
            if (!Files.exists(target)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            Resource resource = new UrlResource(target.toUri());
            String contentType = Files.probeContentType(target);
            if (contentType == null) contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            String filename = target.getFileName().toString();
            ContentDisposition cd = ContentDisposition.inline().filename(filename, StandardCharsets.UTF_8).build();
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

    /** 删除附件 */
    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> download(@PathVariable Long fileId) {
        try {
            Path target = fileService.resolveFilePath(fileId);
            if (!Files.exists(target)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            Resource resource = new UrlResource(target.toUri());
            String contentType = Files.probeContentType(target);
            if (contentType == null) contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            String filename = target.getFileName().toString();
            ContentDisposition cd = ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build();
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

    /** 删除附件 */
    @DeleteMapping("/{fileId}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long fileId) {
        try {
            fileService.delete(fileId);
            Map<String, Object> resp = new HashMap<>();
            resp.put("message", "删除成功");
            return ResponseEntity.ok(resp);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "删除失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "删除失败");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}

