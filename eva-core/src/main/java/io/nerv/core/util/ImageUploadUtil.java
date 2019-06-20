package io.nerv.core.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import io.nerv.core.exception.ImageUploadException;
import io.nerv.core.upload.config.ImageConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * 文件上传工具类
 */
@Slf4j
@Component
public class ImageUploadUtil {

    private Snowflake snowflake = IdUtil.createSnowflake(SNOW, FLAKE);

    @Autowired
    private ImageConfig imageConfig;

    private final static long SNOW = 16;

    private final static long FLAKE = 18;

    /**
     * 文件上传 默认上传到配置的tmp目录
     * @param image
     * @return
     */
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
        if (imageConfig.getAllowSuffixName().contains(suffixName)){
            // 创建临时文件夹
            File tempFileFolder = new File(imageConfig.getTempPath());
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
     */
    public void storage(String... filenames){
        String tempPath = imageConfig.getTempPath();

        for (String filename : filenames) {
            File sourceFile = new File(tempPath, filename);
            File distFile = new File(imageConfig.getStoragePath(), filename);

            FileUtil.copy(sourceFile, distFile, true);
        }
    }
}
