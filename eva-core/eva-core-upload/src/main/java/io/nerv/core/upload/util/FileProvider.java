package io.nerv.core.upload.util;


import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * 文件上传接口
 */
public interface FileProvider {
    /**
     * 上传
     *
     * @param file
     * @return
     */
    String upload(MultipartFile file, String path) throws IOException, Exception;

    /**
     * 存储
     *
     * @param filenames
     * @return
     */
    List<String> storage(String... filenames);

    /**
     * 存储图片并且生成缩略图
     *
     * @param scale
     * @param filenames
     */
    void storageWithThumbnail(float scale, String... filenames);

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
     * 删除文件
     *
     * @param fileName
     */
    void delFromStorage(String fileName);

    /**
     * 缓存清除
     */
    void tempClean();

    void downLoad(String fileName, OutputStream out);
}
