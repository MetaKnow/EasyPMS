package com.missoft.pms.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.function.Predicate;

/**
 * 类级注释：
 * PDF 预览缓存清理工具类。
 * 用于处理由预览接口生成的同目录、同基础文件名的 PDF 缓存文件。
 *
 * 设计说明：
 * 1. 当前项目的多个预览接口会将 Office 文件转换为同目录下的 `*.pdf` 作为缓存文件；
 * 2. 删除原始附件时，需要同步清理该 PDF 缓存，避免残留无用文件；
 * 3. 若同名 `.pdf` 在数据库中本身就是一条真实附件记录，则不能误删；
 * 4. 因此本工具在删除缓存前，会通过调用方传入的回调判断该相对路径是否仍被数据库记录引用。
 */
public final class PdfPreviewCacheUtils {

    /**
     * 类级注释：工具类不允许实例化。
     */
    private PdfPreviewCacheUtils() {
    }

    /**
     * 函数级注释：
     * 删除指定原始文件对应的 PDF 预览缓存文件。
     *
     * 处理规则：
     * 1. 仅对可被项目预览接口转换为 PDF 的 Office 文件进行处理；
     * 2. PDF 原文件本身不进行额外缓存删除；
     * 3. 仅删除与源文件同目录、同基础文件名的 `.pdf` 文件；
     * 4. 若数据库中仍存在该 `.pdf` 相对路径的真实附件记录，则跳过删除，避免误删用户上传的 PDF。
     *
     * @param sourcePath 原始附件的绝对路径
     * @param sourceRelativePath 原始附件在数据库中保存的相对路径
     * @param filePathExistsChecker 用于判断某个相对路径是否仍被数据库记录引用的回调
     */
    public static void deletePdfPreviewCacheIfExists(
            Path sourcePath,
            String sourceRelativePath,
            Predicate<String> filePathExistsChecker) {
        if (sourcePath == null || sourceRelativePath == null || sourceRelativePath.isBlank()) {
            return;
        }

        String fileName = sourcePath.getFileName() != null ? sourcePath.getFileName().toString() : "";
        if (!isConvertibleOfficeFile(fileName)) {
            return;
        }

        Path cachePath = resolvePdfCachePath(sourcePath);
        String cacheRelativePath = resolvePdfCacheRelativePath(sourceRelativePath);
        if (cachePath == null || cacheRelativePath == null || cacheRelativePath.isBlank()) {
            return;
        }

        if (filePathExistsChecker != null && filePathExistsChecker.test(cacheRelativePath)) {
            return;
        }

        try {
            Files.deleteIfExists(cachePath);
        } catch (Exception ignore) {
            // 函数体详细注释：缓存清理属于附加清理逻辑，不影响主删除流程，因此此处吞掉异常。
        }
    }

    /**
     * 函数级注释：
     * 判断文件是否属于当前项目支持转为 PDF 预览缓存的 Office 类型。
     *
     * @param fileName 文件名
     * @return 是否为可转换的 Office 文件
     */
    public static boolean isConvertibleOfficeFile(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return false;
        }
        String lower = fileName.toLowerCase(Locale.ROOT);
        return lower.endsWith(".doc")
                || lower.endsWith(".docx")
                || lower.endsWith(".xls")
                || lower.endsWith(".xlsx")
                || lower.endsWith(".ppt")
                || lower.endsWith(".pptx");
    }

    /**
     * 函数级注释：
     * 根据原始文件绝对路径生成 PDF 缓存绝对路径。
     *
     * @param sourcePath 原始文件绝对路径
     * @return 预览缓存绝对路径；若无法解析则返回 null
     */
    public static Path resolvePdfCachePath(Path sourcePath) {
        if (sourcePath == null || sourcePath.getFileName() == null) {
            return null;
        }
        String fileName = sourcePath.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex <= 0) {
            return null;
        }
        String baseName = fileName.substring(0, dotIndex);
        Path parent = sourcePath.getParent();
        if (parent == null) {
            return null;
        }
        return parent.resolve(baseName + ".pdf").normalize();
    }

    /**
     * 函数级注释：
     * 根据原始文件相对路径生成 PDF 缓存相对路径。
     *
     * @param sourceRelativePath 原始文件相对路径
     * @return PDF 缓存相对路径；若无法解析则返回 null
     */
    public static String resolvePdfCacheRelativePath(String sourceRelativePath) {
        if (sourceRelativePath == null || sourceRelativePath.isBlank()) {
            return null;
        }
        int dotIndex = sourceRelativePath.lastIndexOf('.');
        if (dotIndex <= 0) {
            return null;
        }
        return sourceRelativePath.substring(0, dotIndex) + ".pdf";
    }
}
