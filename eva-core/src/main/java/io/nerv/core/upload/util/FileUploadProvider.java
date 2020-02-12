package io.nerv.core.upload.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 图片上传接口
 */
public interface FileUploadProvider {
    /**
     * 上传
     * @param image
     * @return
     */
    String upload(MultipartFile image);

    /**
     * 存储
     * @param filenames
     * @return
     */
    List<String> storage(String... filenames);

    /**
     * 存储图片并且生成缩略图
     * @param scale
     * @param filenames
     */
    void storageWithThumbnail(float scale, String... filenames);

    /**
     * 生成缩略图
     * @param file
     * @param scale
     */
    void thumbnail(File file, float scale);

    /**
     * 生成缩略图,可指定缩放
     * @param file
     * @param dest
     * @param scale
     */
    void thumbnail(File file, File dest, float scale);

    /**
     * 删除图片
     * @param fileName
     */
    void delFromStorage(String fileName);
}
