package com.missoft.pms.controller;

import com.missoft.pms.service.AfterServiceLeadDeliverableFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
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
@RequestMapping("/api/afterservice-lead-files")
@CrossOrigin(origins = "*")
public class AfterServiceLeadDeliverableFileController {

    @Autowired
    private AfterServiceLeadDeliverableFileService fileService;

    @PostMapping("/{projectId}/{leadsId}/upload")
    public ResponseEntity<Map<String, Object>> upload(
            @PathVariable Long projectId,
            @PathVariable Long leadsId,
            @RequestParam(value = "uploaderId", required = false) Long uploaderId,
            @RequestParam("files") MultipartFile[] files) {
        try {
            var saved = fileService.uploadFiles(projectId, leadsId, uploaderId, files);
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

    @GetMapping("/by-lead/{leadsId}")
    public ResponseEntity<Map<String, Object>> list(@PathVariable Long leadsId) {
        try {
            List<Map<String, Object>> files = fileService.listFiles(leadsId);
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

        String base = input.getFileName().toString().replaceFirst("\\.[^.]+$", "");
        Path realOut = outDir.resolve(base + ".pdf");
        if (!Files.exists(realOut)) {
            throw new IOException("未找到转换后的 PDF 文件: " + realOut);
        }
        return realOut;
    }

    private Path resolveSofficeExecutable() {
        Path cwd = Paths.get(System.getProperty("user.dir"));
        Path candidate = cwd.resolve("..").resolve("tools").resolve("LibreOffice").resolve("program")
                .resolve(isWindows() ? "soffice.exe" : "soffice").normalize();
        if (Files.exists(candidate)) return candidate;

        Path candidate2 = cwd.resolve("tools").resolve("LibreOffice").resolve("program")
                .resolve(isWindows() ? "soffice.exe" : "soffice").normalize();
        if (Files.exists(candidate2)) return candidate2;

        return null;
    }

    private boolean isWindows() {
        String os = System.getProperty("os.name", "").toLowerCase();
        return os.contains("win");
    }

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

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long fileId) {
        try {
            fileService.deleteFile(fileId);
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

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<Map<String, Object>> handleMissingPart(MissingServletRequestPartException e) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", "附件上传失败");
        error.put("message", "未检测到上传文件");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxSize(MaxUploadSizeExceededException e) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", "附件上传失败");
        error.put("message", "上传文件过大");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
