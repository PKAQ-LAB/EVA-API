package io.nerv.core.upload.util

import cn.hutool.core.collection.CollUtil
import cn.hutool.core.date.DateUtil
import cn.hutool.core.img.ImgUtil
import cn.hutool.core.io.FileUtil
import cn.hutool.core.util.IdUtil
import cn.hutool.core.util.StrUtil
import io.nerv.core.constant.CommonConstant
import io.nerv.core.enums.BizCodeEnum
import io.nerv.core.upload.condition.DefaultNgCondition
import io.nerv.exception.FileUploadException
import io.nerv.properties.EvaConfig
import lombok.extern.slf4j.Slf4j
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.util.*

/**
 * 文件上传工具类
 */
@Component
@Conditional(DefaultNgCondition::class)
open class NgFileUploadUtil : FileUploadProvider {

    var log = LoggerFactory.getLogger(this.javaClass)

    private val snowflake = IdUtil.createSnowflake(SNOW, FLAKE)

    @Autowired
    private val cacheManager: CacheManager? = null

    @Autowired
    private val evaConfig: EvaConfig? = null

    /**
     * 文件上传 默认上传到配置的tmp目录
     * @param file
     * @return
     */
    override fun upload(file: MultipartFile, path: String?): String? {
        // 上传图片名
        val fileName = file.originalFilename
        // 后缀名
        var suffixName = ""
        // 新图片名
        var newFileName = ""
        if (StrUtil.isNotBlank(fileName)) {
            suffixName = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase()
            newFileName = snowflake.nextIdStr() + "." + suffixName
        } else {
            log.error(BizCodeEnum.FILEIO_ERROR.getName())
            throw FileUploadException(BizCodeEnum.FILENAME_ERROR)
        }
        // 判断上传文件是否符合格式
        if (evaConfig!!.upload!!.allowSuffixName!!.toLowerCase().contains(suffixName)) {
            var destPath = evaConfig.upload!!.tempPath
            if (StrUtil.isNotBlank(destPath) && !destPath!!.endsWith("/")) {
                destPath += "/"
            }

            // 创建临时文件夹
            val tempFile = File(destPath + newFileName)
            if (!tempFile.parentFile.exists()) {
                tempFile.parentFile.mkdir()
            }
            // 存储文件
            try {
                file.transferTo(tempFile)
                tempFile.setReadable(true, false)
            } catch (e: Exception) {
                log.error(BizCodeEnum.FILEIO_ERROR.getName())
                throw FileUploadException(BizCodeEnum.FILESAVE_ERROR.getName()!!)
            }
        } else {
            log.error(BizCodeEnum.FILETYPE_NOT_SUPPORTED.getName())
            throw FileUploadException(BizCodeEnum.FILETYPE_NOT_SUPPORTED)
        }

        // 放入缓存
        cachePut(newFileName)
        return newFileName
    }

    /**
     * 将图片从缓存目录移动到storage目录
     * @param filenames
     * @return
     */
    override fun storage(vararg filenames: String?): List<String?>? {
        val tempPath = evaConfig!!.upload!!.tempPath
        for (filename in filenames) {
            val sourceFile = File(tempPath, filename)
            if (!sourceFile.exists()) continue
            val distFile = File(evaConfig.upload!!.storagePath, filename)
            FileUtil.move(sourceFile, distFile, true)
            distFile.setReadable(true, false)
        }
        return null
    }

    /**
     * 将图片从缓存目录移动到storage目录并生成缩略图
     * @param filenames
     */
    override fun storageWithThumbnail(scale: Float, vararg filenames: String) {
        val tempPath = evaConfig!!.upload!!.tempPath
        for (filename in filenames) {
            val sourceFile = File(tempPath, filename)
            if (!sourceFile.exists()) continue
            val distFile = File(evaConfig.upload!!.storagePath, filename)
            val distFileThumbnail = File(evaConfig.upload!!.storagePath, THUMBNAIL_NAME + filename)
            this.thumbnail(sourceFile, distFileThumbnail, scale)
            FileUtil.move(sourceFile, distFile, true)
            distFile.setReadable(true, false)
            distFileThumbnail.setReadable(true, false)
        }
    }

    /**
     * 生成缩略图
     * @param file
     * @param scale
     */
    override fun thumbnail(file: File, scale: Float) {
        val destFile = File(file.parent, THUMBNAIL_NAME + file.name)
        ImgUtil.scale(file, destFile, scale)
        destFile.setReadable(true, false)
    }

    /**
     * 生成缩略图
     * @param file
     * @param dest
     * @param scale
     */
    override fun thumbnail(file: File?, dest: File?, scale: Float) {
        ImgUtil.scale(file, dest, scale)
    }

    /**
     * 从持久目录删除图片以及缩略图
     * @param fileName
     */
    override fun delFromStorage(fileName: String) {
        val sotragePath = evaConfig!!.upload!!.storagePath
        val sourceFile = File(sotragePath, fileName)
        val sourceThumbnailFile = File(sotragePath, THUMBNAIL_NAME + fileName)
        // 删除原图
        if (sourceFile.exists()) {
            sourceFile.delete()
        }
        // 删除缩略图
        if (sourceThumbnailFile.exists()) {
            sourceThumbnailFile.delete()
        }
    }

    /**
     * 清除当前时间-2小时前的缓存图片
     */
    override fun tempClean() {
        val tempFileFolder = File(evaConfig!!.upload!!.tempPath)
        val cache = cacheManager!!.getCache(CommonConstant.CACHE_UPLOADFILES)
        val k = CommonConstant.FILE_CACHE_PREFIX + DateUtil.format(DateUtil.offsetHour(Date(), -2), "HH")
        var tmpFileList: List<String?> = ArrayList()
        val valueWrapper = cache[k]
        if (null != valueWrapper) {
            tmpFileList = valueWrapper.get() as List<String?>
        }
        if (CollUtil.isEmpty(tmpFileList)) return
        tmpFileList.stream().forEach { item: String? ->
            if (!FileUtil.isDirEmpty(tempFileFolder)) {
                FileUtil.del(File(tempFileFolder, item))
            }
        }

        // 删除完毕 从缓存中移除此key
        cache.evict(k)
    }

    /**
     * 将上传的图片写入缓存 根据清理机制同时会存在3个缓存
     * 即 当前时间-2 当前时间-1 当前时间
     * 清除时 会清除当前时间 - 2 的缓存
     * @param v
     */
    private fun cachePut(v: String) {
        val cache = cacheManager!!.getCache(CommonConstant.CACHE_UPLOADFILES)
        val k = CommonConstant.FILE_CACHE_PREFIX + DateUtil.format(Date(), "HH")
        val valueWrapper = cache[k]
        var tmpFileList: MutableList<String?>? = null
        tmpFileList = if (null == valueWrapper) {
            ArrayList()
        } else {
            valueWrapper.get() as MutableList<String?>
        }
        if (null == tmpFileList) {
            tmpFileList = ArrayList()
            cache.put(k, tmpFileList)
        }
        tmpFileList.add(v)
    }

    companion object {
        private const val THUMBNAIL_NAME = "thumbnail_"
        private const val SNOW: Long = 16
        private const val FLAKE: Long = 18
    }
}