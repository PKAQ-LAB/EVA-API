package io.nerv.web.upload.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.resource.InputStreamResource;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import io.nerv.core.constant.CommonConstant;
import io.nerv.core.enums.BizCodeEnum;
import io.nerv.core.exception.BizException;
import io.nerv.web.upload.condition.FastDfsCondition;
import io.nerv.core.util.JsonUtil;
import io.nerv.properties.EvaConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * 文件上传工具类 - 使用fastdfs
 */
@Slf4j
@Component
@Conditional(FastDfsCondition.class)
public class DfsFileUploadUtil implements FileUploadProvider {
    @Autowired
    private JsonUtil jsonUtil;

    private Snowflake snowflake = IdUtil.getSnowflake(SNOW, FLAKE);

    // 删除接口
    private final static String DELETE_API = "/delete";

    // 上传接口
    private final static String UPLOAD_API = "/upload";

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private EvaConfig evaConfig;

    private final static long SNOW = 16;

    private final static long FLAKE = 18;

    /**
     * 文件上传 默认上传到配置的tmp目录
     * @param file
     * @return
     */
    @Override
    public String upload(MultipartFile file, String path){
        // 上传文件名
        String fileName = file.getOriginalFilename();
        // 后缀名
        String suffixName = "";
        // 存储后返回的信息
        String newFileName = "";

        String file_path = "";

        if (StrUtil.isNotEmpty(fileName)){
            suffixName = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
            newFileName = snowflake.nextIdStr() + "." + suffixName;
        } else {
            log.error(BizCodeEnum.FILEIO_ERROR.getMsg());
            throw new BizException(BizCodeEnum.FILENAME_ERROR);
        }
        // 判断上传文件是否符合格式
        if (evaConfig.getUpload().getAllowSuffixName().toUpperCase().contains(suffixName)){
            InputStreamResource isr = null;
            try {
                isr = new InputStreamResource(file.getInputStream(), newFileName);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throw new BizException(BizCodeEnum.FILEIO_ERROR);
            }

            Map<String, Object> paramMap = new HashMap<>(3);
            //文件
            paramMap.put("file", isr);
            //输出
            paramMap.put("output","json");
            //自定义路径
//            String curDate = DateUtil.format(new Date(), DatePattern.PURE_DATE_FORMAT);
            if (StrUtil.isNotBlank(path)){
                paramMap.put("path", path);
            } else {
                paramMap.put("path", suffixName);
            }

            Map<String, String> jsonObject = jsonUtil.parseObject(this.transfer(paramMap), Map.class);

            file_path = jsonObject.get("path");
        } else {
            log.error(BizCodeEnum.FILETYPE_NOT_SUPPORTED.getMsg());
            throw new BizException(BizCodeEnum.FILETYPE_NOT_SUPPORTED);
        }

        // 放入缓存
        this.cachePut(newFileName);

        return file_path;
    }


    /**
     * 将文件从缓存目录移动到storage目录
     * @param filenames
     * @return
     */
    @Override
    public List<String> storage(String... filenames){ return null;}

    /**
     * 将文件从缓存目录移动到storage目录并生成缩略图
     * @param filenames
     */
    @Override
    public void storageWithThumbnail(float scale, String... filenames){}

    /**
     * 生成缩略图
     * @param file
     * @param scale
     */
    @Override
    public void thumbnail(File file, float scale){}

    /**
     * 生成缩略图
     * @param file
     * @param dest
     * @param scale
     */
    @Override
    public void thumbnail(File file, File dest, float scale){}

    /**
     * 从持久目录删除文件以及缩略图
     * @param path
     */
    @Override
    public void delFromStorage(String path){
       HttpUtil.post(evaConfig.getUpload().getServerUrl()+ this.DELETE_API, path);
    }

    /**
     * 传输到go-fastdfs
     * @return
     */
    public String transfer(Map<String, Object> map){
        return HttpUtil.post(evaConfig.getUpload().getServerUrl() + this.UPLOAD_API, map);
    }

    /**
     * 清除当前时间-2小时前的缓存图片
     */
    @Override
    public void tempClean() {
        Cache cache = cacheManager.getCache(CommonConstant.CACHE_UPLOADFILES);

        String k = CommonConstant.FILE_CACHE_PREFIX + DateUtil.format(DateUtil.offsetHour(new Date(), -2), "HH") ;
        var cacheWrapper = cache.get(k);

        List<String> tmpFileList = null == cacheWrapper? null : (List<String>) cacheWrapper.get();

        if (null == cacheWrapper || CollUtil.isEmpty(tmpFileList)) {
            return;
        }

        tmpFileList.stream().forEach(item -> {
            HttpUtil.post(evaConfig.getUpload().getServerUrl() + DELETE_API, item);
        });

        // 删除完毕 从缓存中移除此key
        cache.evict(k);
    }
    /**
     * 将上传的图片写入缓存 根据清理机制同时会存在3个缓存
     * 即 当前时间-2 当前时间-1 当前时间
     * 清除时 会清除当前时间 - 2 的缓存
     * @param v
     */
    private void cachePut(String v){
        Cache cache = cacheManager.getCache(CommonConstant.CACHE_UPLOADFILES);

        String k = CommonConstant.FILE_CACHE_PREFIX + DateUtil.format(new Date(), "HH") ;
        Cache.ValueWrapper valueWrapper = cache.get(k);

        List<String> tmpFileList = new ArrayList<>();
        if (null == valueWrapper){
            cache.put(k, tmpFileList);
        } else {
            tmpFileList = (List<String>) valueWrapper.get();
        }
        tmpFileList.add(v);
    }
}
