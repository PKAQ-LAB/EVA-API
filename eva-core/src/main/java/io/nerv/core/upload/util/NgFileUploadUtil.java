package io.nerv.core.upload.util;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import io.nerv.core.exception.ImageUploadException;
import io.nerv.core.upload.condition.DefaultNgCondition;
import io.nerv.properties.EvaConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
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

    @Autowired
    private EvaConfig evaConfig;

    private final static long SNOW = 16;

    private final static long FLAKE = 18;

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

        if (StringUtils.isNotEmpty(fileName)){
            suffixName = fileName.substring(fileName.lastIndexOf(".") + 1);
            newFileName = snowflake.nextIdStr() + "." + suffixName;
        } else {
            log.error("文件名错误：");
            throw new ImageUploadException("文件名错误");
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
                log.error("图片保存错误："+e.getMessage());
                throw new ImageUploadException("图片保存错误");
            }
        } else {
            log.error("上传格式错误：");
            throw new ImageUploadException("上传格式错误");
        }

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

}
