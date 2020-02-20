package io.nerv.core.upload.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import io.nerv.core.constant.CommonConstant;
import io.nerv.core.enums.BizCodeEnum;
import io.nerv.core.exception.FileUploadException;
import io.nerv.core.upload.condition.DefaultNgCondition;
import io.nerv.properties.EvaConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 文件上传工具类
 */
@Slf4j
@Component
@Conditional(DefaultNgCondition.class)
public class NgFileUploadUtil implements FileUploadProvider {

    private Snowflake snowflake = IdUtil.createSnowflake(SNOW, FLAKE);

    private final static String THUMBNAIL_NAME = "thumbnail_";

    private final static long SNOW = 16;

    private final static long FLAKE = 18;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private EvaConfig evaConfig;

    /**
     * 文件上传 默认上传到配置的tmp目录
     * @param image
     * @return
     */
    @Override
    public String upload(MultipartFile image){
        // 上传图片名
        String fileName = image.getOriginalFilename();
        // 后缀名
        String suffixName = "";
        // 新图片名
        String newFileName = "";

        if (StrUtil.isNotBlank(fileName)){
            suffixName = fileName.substring(fileName.lastIndexOf(".") + 1);
            newFileName = snowflake.nextIdStr() + "." + suffixName;
        } else {
            log.error(BizCodeEnum.FILEIO_ERROR.getName());
            throw new FileUploadException(BizCodeEnum.FILENAME_ERROR);
        }
        // 判断上传图片是否符合格式
        if (evaConfig.getUpload().getAllowSuffixName().contains(suffixName)){
            // 创建临时文件夹
            File tempFileFolder = new File(evaConfig.getUpload().getTempPath());
            if (!tempFileFolder.exists() && !tempFileFolder.isDirectory()){
                tempFileFolder.mkdir();
            }
            // 存储图片
            try {
                FileUtil.touch(new File(tempFileFolder, newFileName));
            } catch (Exception e) {
                log.error(BizCodeEnum.FILEIO_ERROR.getName());
                throw new FileUploadException(BizCodeEnum.FILESAVE_ERROR.getName());
            }
        } else {
            log.error(BizCodeEnum.FILETYPE_NOT_SUPPORTED.getName());
            throw new FileUploadException(BizCodeEnum.FILETYPE_NOT_SUPPORTED);
        }

        // 放入缓存
        this.cachePut(newFileName);

        return newFileName;
    }

    /**
     * 将图片从缓存目录移动到storage目录
     * @param filenames
     * @return
     */
    @Override
    public List<String> storage(String... filenames){
        String tempPath = evaConfig.getUpload().getTempPath();

        for (String filename : filenames) {
            File sourceFile = new File(tempPath, filename);
            if (!sourceFile.exists()) continue;
            File distFile = new File(evaConfig.getUpload().getStoragePath(), filename);
            FileUtil.move(sourceFile, distFile, true);
        }

        return null;
    }

    /**
     * 将图片从缓存目录移动到storage目录并生成缩略图
     * @param filenames
     */
    @Override
    public void storageWithThumbnail(float scale, String... filenames){
        String tempPath = evaConfig.getUpload().getTempPath();

        for (String filename : filenames) {
            File sourceFile = new File(tempPath, filename);
            if (!sourceFile.exists()) continue;
            File distFile = new File(evaConfig.getUpload().getStoragePath(), filename);

            File distFileThumbnail = new File(evaConfig.getUpload().getStoragePath(), this.THUMBNAIL_NAME + filename);
            this.thumbnail(sourceFile, distFileThumbnail, scale);
            FileUtil.move(sourceFile, distFile, true);
        }
    }

    /**
     * 生成缩略图
     * @param file
     * @param scale
     */
    @Override
    public void thumbnail(File file, float scale){
        File destFile = new File(file.getParent(), this.THUMBNAIL_NAME + file.getName());
        ImgUtil.scale(file, destFile, scale);
    }

    /**
     * 生成缩略图
     * @param file
     * @param dest
     * @param scale
     */
    @Override
    public void thumbnail(File file, File dest, float scale){
        ImgUtil.scale(file, dest, scale);
    }

    /**
     * 从持久目录删除图片以及缩略图
     * @param fileName
     */
    @Override
    public void delFromStorage(String fileName){
        String sotragePath = evaConfig.getUpload().getStoragePath();
        File sourceFile = new File(sotragePath, fileName);
        File sourceThumbnailFile = new File(sotragePath, this.THUMBNAIL_NAME+fileName);
        // 删除原图
        if (sourceFile.exists()) {
            sourceFile.delete();
        };
        // 删除缩略图
        if (sourceThumbnailFile.exists()) {
            sourceThumbnailFile.delete();
        };
    }

    /**
     * 清除当前时间-2小时前的缓存图片
     */
    @Override
    public void tempClean() {
        File tempFileFolder = new File(evaConfig.getUpload().getTempPath());

        Cache cache = cacheManager.getCache(CommonConstant.CACHE_UPLOADFILES);

        String k = CommonConstant.FILE_CACHE_PREFIX + DateUtil.format(DateUtil.offsetHour(new Date(), -2), "HH") ;
        List<String> tmpFileList = new ArrayList<>();

        Cache.ValueWrapper valueWrapper = cache.get(k);
        if (null != valueWrapper){
            tmpFileList = (List<String>) valueWrapper.get();
        }

        if (CollUtil.isEmpty(tmpFileList)) return;

        tmpFileList.stream().forEach(item -> {
            if (!FileUtil.isDirEmpty(tempFileFolder)){
                FileUtil.del(new File(tempFileFolder, item));
            }
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

        List<String> tmpFileList = null;

        if (null == valueWrapper){
            tmpFileList = new ArrayList<>();
        } else {
            tmpFileList = (List<String>) valueWrapper.get();
        }

        if (null == tmpFileList){
            tmpFileList = new ArrayList<>();
            cache.put(k, tmpFileList);
        }
        tmpFileList.add(v);
    }
}
