package tech.yunyue.core.upload.util;


import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.web.multipart.MultipartFile;
import tech.yunyue.core.upload.enumm.BucketTypeEnum;
import tech.yunyue.core.upload.enumm.IBucket;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * 文件上传下载接口
 */
public interface FileProvider<T extends IBucket> {
    // 图片常见后缀
    String SUFFIXSTR = ".bmp .dib .gif .jfif .jpe .jpeg .jpg .png .tif .tiff .ico .webp .svg .raw .psd";
    String THUMBNAIL_NAME = "thumbnail_";
    Snowflake snowflake = IdUtil.getSnowflake(16, 18);

    /**
     * 上传文件到临时目录，按文件类型/YYYYMM结构存储文件
     *
     * @param file 文件
     * @return 生成的文件目录+保存的文件名
     * @throws Exception
     */
    default String upload(MultipartFile file) {
        return "";
    }

    /**
     * 上传文件到临时目录，存在指定path下
     *
     * @param file 文件
     * @param path 目录
     * @return path+文件名
     * @throws Exception
     */
    String upload(MultipartFile file, String path);

    /**
     * 上传文件到指定桶，存在指定path下
     *
     * @param file   文件
     * @param target 桶名
     * @param path   目录
     * @return path+文件名
     * @throws Exception
     */
    default String upload(MultipartFile file, BucketTypeEnum target, String path) {
        return "";
    }

    /**
     * 将文件从临时目录转移到持久目录 并删除源文件
     *
     * @param filenames 需要转移的文件名
     * @return
     */
    List<String> storage(String... filenames);

    /**
     * 将文件从临时目录转移到目标桶 并删除源文件
     *
     * @param target    目标桶
     * @param filenames 需要转移的文件名
     */
    default void storage(BucketTypeEnum target, String... filenames) {
    }

    /**
     * 将文件从源桶转移到目标桶 并删除源文件
     *
     * @param source    源桶
     * @param target    目标桶
     * @param filenames 文件名
     */
    default void storage(BucketTypeEnum source, BucketTypeEnum target, String... filenames) {
    }

    /**
     * 将文件从临时目录移动到持久目录 是图片则根据配置的长宽生成缩略图 300*300
     *
     * @param filenames 文件名
     */
    default void storageWithThumbnail(String... filenames) {
    }

    /**
     * 将文件从缓存目录移动到storage目录 是图片则按比例生成缩略图
     *
     * @param scale
     * @param filenames
     */
    void storageWithThumbnail(float scale, String... filenames);

    /**
     * 将文件从缓存目录移动到storage目录 是图片则按长宽生成缩略图
     *
     * @param filenames
     */
    default void storageWithThumbnail(int width, int height, String... filenames) {
    }


    /**
     * 生成缩略图
     *
     * @param file
     * @param scale
     */
    void thumbnail(File file, float scale);

    /**
     * 生成缩略图,可指定缩放
     *
     * @param file
     * @param dest
     * @param scale
     */
    void thumbnail(File file, File dest, float scale);

    /**
     * 从持久目录删除文件
     *
     * @param fileName 文件名
     */
    void delFromStorage(String fileName);

    /**
     * 从指定桶删除文件(物理删除)
     *
     * @param fileName 文件名
     */
    default void delete(BucketTypeEnum target, String fileName) {
    }

    /**
     * 从持久目录删除文件（逻辑删除，转移到归档桶）
     *
     * @param fileName 文件名
     */
    default void deleteLogic(String fileName) {
    }

    /**
     * 从source删除文件（逻辑删除，转移到归档桶）
     *
     * @param fileName 文件名
     */
    default void deleteLogic(BucketTypeEnum source, String fileName) {
    }

    /**
     * 缓存清除
     */
    void tempClean();

    /**
     * 从持久桶下载文件
     *
     * @param fileName 文件名
     * @param out      输出流
     */
    default void downLoad(String fileName, OutputStream out) {
    }

    /**
     * 从目标桶下载文件
     *
     * @param fileName 文件名
     * @param target   桶名
     * @param out      输出流
     */
    default void downLoad(String fileName, OutputStream out, BucketTypeEnum target) {
    }

    /**
     * 查看持久桶中该文件的缩略图
     *
     * @param fileName 文件名
     * @return 文件的缩略图预览url
     */
    default String previewThumbnail(String fileName) {
        return "";
    }

    /**
     * 预览持久桶中的文件
     *
     * @param fileName 文件名
     * @return 文件的预览url
     */
    default String preview(String fileName) {
        return "";
    }

    /**
     * 预览目标桶的文件
     *
     * @param fileName 文件名
     * @param target   目标桶
     * @return 文件的预览url
     */
    default String preview(String fileName, BucketTypeEnum target) {
        return "";
    }

    /**
     * 根据图片的预览链接返回源文件名
     *
     * @param previewUrl 持久桶的预览连接或者源文件名
     * @return 原文件名
     */
    default String parsePreviewUrlToFileName(String previewUrl) {
        return previewUrl;
    }

    /**
     * @param fileName 文件名
     * @return 获取持久桶的文件流
     */
    default InputStream getFileInputStream(String fileName) {
        return new ByteArrayInputStream(new byte[0]);
    }

    /**
     * @param fileName 文件名
     * @param target   目标桶
     * @return 获取目标桶对应文件的文件流
     */
    default InputStream getFileInputStream(String fileName, BucketTypeEnum target) {
        return new ByteArrayInputStream(new byte[0]);
    }

    /**
     * 将持久桶的原文件复制到原目录下
     *
     * @param sourceName 原文件名
     * @return 生成的文件目录+保存的文件名
     */
    default String copy(String sourceName) {
        // 原文件新名称
        var lastIndex = FileUtil.lastIndexOfSeparator(sourceName) + 1;
        var targetName = "%s%s%s".formatted(sourceName.substring(0, lastIndex), snowflake.nextIdStr(), sourceName.substring(lastIndex));
        // 复制原文件
        copy(sourceName, BucketTypeEnum.STORAGE, targetName, BucketTypeEnum.STORAGE);

        // 如果是图片 处理缩略图
        if (SUFFIXSTR.contains(FileUtil.extName(sourceName))) {
            // 原缩略图路径
            String fileName = sourceName.substring(sourceName.lastIndexOf(StrUtil.SLASH) + 1);
            var sThumbnailName = sourceName.replace(fileName, THUMBNAIL_NAME + fileName);
            // 新图缩略图路径
            String newfileName = targetName.substring(targetName.lastIndexOf(StrUtil.SLASH) + 1);
            var targetThumbnailName = targetName.replace(newfileName, THUMBNAIL_NAME + newfileName);
            // 复制缩略图
            copy(sThumbnailName, BucketTypeEnum.STORAGE, targetThumbnailName, BucketTypeEnum.STORAGE);
        }
        return targetName;
    }

    /**
     * 将原文件夹从原桶复制到目标桶
     *
     * @param sourceName 原文件名
     * @param source     原桶
     * @param target     目标桶
     * @param targetName 目标名称
     * @return 目标名称
     */
    default String copy(String sourceName, BucketTypeEnum source, String targetName, BucketTypeEnum target) {
        return targetName;
    }
}
