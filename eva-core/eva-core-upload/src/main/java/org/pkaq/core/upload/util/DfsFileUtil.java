package org.pkaq.core.upload.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.constant.CommonConstant;
import org.pkaq.core.exception.BizException;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.upload.condition.FastDfsCondition;
import org.pkaq.core.upload.provider.FileProvider;
import org.pkaq.core.util.CollUtils;
import org.pkaq.core.util.DateUtils;
import org.pkaq.core.util.Snowflake;
import org.pkaq.core.util.StrUtils;
import org.pkaq.core.util.json.JsonUtil;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

/**
 * 文件上传工具类 - 使用fastdfs
 */
@Slf4j
@Component
@Conditional(FastDfsCondition.class)
@RequiredArgsConstructor
public class DfsFileUtil implements FileProvider {
    private final RestTemplate restTemplate;

    // 删除接口
    private static final String DELETE_API = "/delete";
    // 上传接口
    private static final String UPLOAD_API = "/upload";
    private static final long SNOW = 16;
    private static final long FLAKE = 18;
    private final Snowflake snowflake = new Snowflake(SNOW, FLAKE);
    private final CacheManager cacheManager;
    private final EvaConfig evaConfig;

    /**
     * 文件上传 默认上传到配置的tmp目录
     *
     * @param file
     * @return
     */
    @Override
    public String upload(MultipartFile file, String path) {
        // 上传文件名
        String fileName = file.getOriginalFilename();
        // 后缀名
        String suffixName = "";
        // 存储后返回的信息
        String newFileName = "";

        String file_path = "";

        if (StrUtils.isNotEmpty(fileName)) {
            suffixName = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
            newFileName = snowflake.nextId() + "." + suffixName;
        } else {
            log.error(CommonCodes.FILEIO_ERROR.getMsg());
            throw new BizException(CommonCodes.FILENAME_ERROR);
        }
        // 判断上传文件是否符合格式
        if (evaConfig.getUpload().getAllowSuffixName().toUpperCase().contains(suffixName)) {
            InputStreamResource isr = null;
            try {
                isr = new InputStreamResource(file.getInputStream(), newFileName);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throw new BizException(CommonCodes.FILEIO_ERROR);
            }

            Map<String, Object> paramMap = HashMap.newHashMap(3);
            //文件
            paramMap.put("file", isr);
            //输出
            paramMap.put("output", "json");
            //自定义路径
//            String curDate = DateUtils.format(new Date(), DatePattern.PURE_DATE_FORMAT);
            if (StrUtils.isNotBlank(path)) {
                paramMap.put("path", path);
            } else {
                paramMap.put("path", suffixName);
            }

            Map<String, String> jsonObject = JsonUtil.parse(this.transfer(paramMap), Map.class);

            if (jsonObject != null) {
                file_path = jsonObject.get("path");
            }
        } else {
            log.error(CommonCodes.FILETYPE_NOT_SUPPORTED.getMsg());
            throw new BizException(CommonCodes.FILETYPE_NOT_SUPPORTED);
        }

        // 放入缓存
        this.cachePut(newFileName);

        return file_path;
    }


    /**
     * 将文件从缓存目录移动到storage目录
     *
     * @param filenames
     * @return
     */
    @Override
    public List<String> storage(String... filenames) {
        return null;
    }

    /**
     * 将文件从缓存目录移动到storage目录并生成缩略图
     *
     * @param filenames
     */
    @Override
    public void storageWithThumbnail(float scale, String... filenames) {
    }

    /**
     * 生成缩略图
     *
     * @param file
     * @param scale
     */
    @Override
    public void thumbnail(File file, float scale) {
    }

    /**
     * 生成缩略图
     *
     * @param file
     * @param dest
     * @param scale
     */
    @Override
    public void thumbnail(File file, File dest, float scale) {
    }

    /**
     * 从持久目录删除文件以及缩略图
     *
     * @param path
     */
    @Override
    public void delFromStorage(String path) {
        restTemplate.postForEntity(evaConfig.getUpload().getServerUrl() + DELETE_API, path, String.class);
    }

    /**
     * 传输到go-fastdfs
     *
     * @return
     */
    public String transfer(Map<String, Object> map) {
        return restTemplate.postForObject(evaConfig.getUpload().getServerUrl() + UPLOAD_API, map, String.class);
    }

    /**
     * 清除当前时间-2小时前的缓存图片
     */
    @Override
    public void tempClean() {
        Cache cache = cacheManager.getCache(CommonConstant.CACHE_UPLOADFILES);

        String k = CommonConstant.FILE_CACHE_PREFIX + DateUtils.format(DateUtils.addHours(new Date(), -2), "HH");
        var cacheWrapper = cache.get(k);

        List<String> tmpFileList = null == cacheWrapper ? null : (List<String>) cacheWrapper.get();

        if (null == cacheWrapper || CollUtils.isEmpty(tmpFileList)) {
            return;
        }

        for (String item : tmpFileList) {
            restTemplate.postForObject(evaConfig.getUpload().getServerUrl() + UPLOAD_API, item, String.class);
        }

        // 删除完毕 从缓存中移除此key
        cache.evict(k);
    }

    @Override
    public void downLoad(String fileName, OutputStream out) {

    }

    /**
     * 将上传的图片写入缓存 根据清理机制同时会存在3个缓存
     * 即 当前时间-2 当前时间-1 当前时间
     * 清除时 会清除当前时间 - 2 的缓存
     *
     * @param v
     */
    private void cachePut(String v) {
        Cache cache = cacheManager.getCache(CommonConstant.CACHE_UPLOADFILES);

        String k = CommonConstant.FILE_CACHE_PREFIX + DateUtils.format(new Date(), "HH");
        Cache.ValueWrapper valueWrapper = cache.get(k);

        List<String> tmpFileList = new ArrayList<>();
        if (null == valueWrapper) {
            cache.put(k, tmpFileList);
        } else {
            tmpFileList = (List<String>) valueWrapper.get();
        }
        tmpFileList.add(v);
    }
}
