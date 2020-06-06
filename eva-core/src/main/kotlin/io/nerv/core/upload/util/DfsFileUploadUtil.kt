package io.nerv.core.upload.util

import cn.hutool.core.collection.CollUtil
import cn.hutool.core.date.DateUtil
import cn.hutool.core.io.resource.InputStreamResource
import cn.hutool.core.util.IdUtil
import cn.hutool.core.util.StrUtil
import cn.hutool.http.HttpUtil
import io.nerv.core.constant.CommonConstant
import io.nerv.core.enums.BizCodeEnum
import io.nerv.core.upload.condition.FastDfsCondition
import io.nerv.core.util.JsonUtil
import io.nerv.exception.FileUploadException
import io.nerv.properties.EvaConfig
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.IOException
import java.util.*

/**
 * 文件上传工具类 - 使用fastdfs
 */
@Component
@Conditional(FastDfsCondition::class)
open class DfsFileUploadUtil : FileUploadProvider {

    var log = LoggerFactory.getLogger(this.javaClass)

    @Autowired
    private val jsonUtil: JsonUtil? = null
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
        // 上传文件名
        val fileName = file.originalFilename
        // 后缀名
        var suffixName = ""
        // 存储后返回的信息
        var newFileName = ""
        var file_path = ""
        if (StrUtil.isNotEmpty(fileName)) {
            suffixName = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase()
            newFileName = snowflake.nextIdStr() + "." + suffixName
        } else {
            log.error(BizCodeEnum.FILEIO_ERROR.getName())
            throw FileUploadException(BizCodeEnum.FILENAME_ERROR)
        }
        // 判断上传文件是否符合格式
        if (evaConfig!!.upload!!.allowSuffixName!!.toUpperCase().contains(suffixName)) {
            var isr: InputStreamResource? = null
            isr = try {
                InputStreamResource(file.inputStream, newFileName)
            } catch (e: IOException) {
                log.error(e.message, e)
                throw FileUploadException(BizCodeEnum.FILEIO_ERROR)
            }
            val paramMap: MutableMap<String, Any?> = HashMap(3)
            //文件
            paramMap["file"] = isr
            //输出
            paramMap["output"] = "json"
            //自定义路径
//            String curDate = DateUtil.format(new Date(), DatePattern.PURE_DATE_FORMAT);
            if (StrUtil.isNotBlank(path)) {
                paramMap["path"] = path
            } else {
                paramMap["path"] = suffixName
            }
            val jsonObject: Map<String, String> = jsonUtil!!.parseObject<Map<String, String>>(transfer(paramMap), MutableMap::class.java)
            file_path = jsonObject["path"]!!
        } else {
            log.error(BizCodeEnum.FILETYPE_NOT_SUPPORTED.getName())
            throw FileUploadException(BizCodeEnum.FILETYPE_NOT_SUPPORTED)
        }

        // 放入缓存
        cachePut(newFileName)
        return file_path
    }

    /**
     * 将文件从缓存目录移动到storage目录
     * @param filenames
     * @return
     */
    override fun storage(vararg filenames: String?): List<String?>? {
        return null
    }

    /**
     * 将文件从缓存目录移动到storage目录并生成缩略图
     * @param filenames
     */
    override fun storageWithThumbnail(scale: Float, vararg filenames: String) {}

    /**
     * 生成缩略图
     * @param file
     * @param scale
     */
    override fun thumbnail(file: File, scale: Float) {}

    /**
     * 生成缩略图
     * @param file
     * @param dest
     * @param scale
     */
    override fun thumbnail(file: File?, dest: File?, scale: Float) {}

    /**
     * 从持久目录删除文件以及缩略图
     * @param path
     */
    override fun delFromStorage(path: String) {
        HttpUtil.post(evaConfig!!.upload!!.serverUrl + DELETE_API, path)
    }

    /**
     * 传输到go-fastdfs
     * @return
     */
    fun transfer(map: Map<String, Any?>?): String {
        return HttpUtil.post(evaConfig!!.upload!!.serverUrl + UPLOAD_API, map)
    }

    /**
     * 清除当前时间-2小时前的缓存图片
     */
    override fun tempClean() {
        val cache = cacheManager!!.getCache(CommonConstant.CACHE_UPLOADFILES)
        val k = CommonConstant.FILE_CACHE_PREFIX + DateUtil.format(DateUtil.offsetHour(Date(), -2), "HH")
        val cacheWrapper = cache[k]
        val tmpFileList = if (null == cacheWrapper) null else cacheWrapper.get() as List<String?>
        if (null == cacheWrapper || CollUtil.isEmpty(tmpFileList)) return
        tmpFileList!!.stream().forEach { item: String? -> HttpUtil.post(evaConfig!!.upload!!.serverUrl + DELETE_API, item) }

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
        var tmpFileList: MutableList<String?> = ArrayList()
        if (null == valueWrapper) {
            cache.put(k, tmpFileList)
        } else {
            tmpFileList = valueWrapper.get() as MutableList<String?>
        }
        tmpFileList.add(v)
    }

    companion object {
        // 删除接口
        private const val DELETE_API = "/delete"

        // 上传接口
        private const val UPLOAD_API = "/upload"
        private const val SNOW: Long = 16
        private const val FLAKE: Long = 18
    }
}