package io.nerv.core.upload.util

import org.springframework.web.multipart.MultipartFile
import java.io.File

/**
 * 文件上传接口
 */
interface FileUploadProvider {
    /**
     * 上传
     * @param file
     * @return
     */
    fun upload(file: MultipartFile, path: String?): String?

    /**
     * 存储
     * @param filenames
     * @return
     */
    fun storage(vararg filenames: String?): List<String?>?

    /**
     * 存储图片并且生成缩略图
     * @param scale
     * @param filenames
     */
    fun storageWithThumbnail(scale: Float, vararg filenames: String)

    /**
     * 生成缩略图
     * @param file
     * @param scale
     */
    fun thumbnail(file: File, scale: Float)

    /**
     * 生成缩略图,可指定缩放
     * @param file
     * @param dest
     * @param scale
     */
    fun thumbnail(file: File?, dest: File?, scale: Float)

    /**
     * 删除图片
     * @param fileName
     */
    fun delFromStorage(fileName: String)

    /**
     * 缓存清除
     */
    fun tempClean()
}