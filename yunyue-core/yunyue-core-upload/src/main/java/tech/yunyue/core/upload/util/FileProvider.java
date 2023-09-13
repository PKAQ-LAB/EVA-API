package tech.yunyue.core.upload.util;


import org.springframework.web.multipart.MultipartFile;
import tech.yunyue.core.upload.enumm.MinIOBucketEnum;

import java.io.File;
import java.io.OutputStream;
import java.util.List;

/**
 * 文件上传下载接口
 */
public interface FileProvider {
    // 图片常见后缀
    String SUFFIXSTR = ".bmp .dib .gif .jfif .jpe .jpeg .jpg .png .tif .tiff .ico";

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
    default String upload(MultipartFile file, MinIOBucketEnum target, String path) {
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
    default void storage(MinIOBucketEnum target, String... filenames) {
    }

    /**
     * 将文件从源桶转移到目标桶 并删除源文件
     *
     * @param source    源桶
     * @param target    目标桶
     * @param filenames 文件名
     */
    default void storage(MinIOBucketEnum source, MinIOBucketEnum target, String... filenames) {
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
    default void delete(MinIOBucketEnum target, String fileName) {
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
    default void deleteLogic(MinIOBucketEnum source, String fileName) {
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
    default void downLoad(String fileName, OutputStream out, MinIOBucketEnum target) {
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
    default String preview(String fileName, MinIOBucketEnum target) {
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
}
