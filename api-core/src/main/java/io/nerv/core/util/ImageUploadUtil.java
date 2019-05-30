package io.nerv.core.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import io.nerv.core.enums.HttpCodeEnum;
import io.nerv.core.mvc.util.Response;
import io.nerv.core.upload.config.ImageConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Map;

/**
 * 文件上传工具类
 */
@Slf4j
@Component
public class ImageUploadUtil {

    private Snowflake snowflake = IdUtil.createSnowflake(SNOW, FLAKE);

    @Autowired
    private ImageConfig imageConfig;

    private final static long SNOW = 7910;

    private final static long FLAKE = 9870;

    /**
     * 文件上传 默认上传到配置的tmp目录
     * @param image
     * @return
     */
    public Response upload(MultipartFile image){
        Response response = new Response();
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
            return response.failure(HttpCodeEnum.SERVER_ERROR.getIndex(), "文件名错误");
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
                return response.failure(HttpCodeEnum.SERVER_ERROR.getIndex(), "图片保存错误");
            }
        } else {
            log.error("上传格式错误：");
            return response.failure(HttpCodeEnum.SERVER_ERROR.getIndex(), "上传格式错误");
        }

        return response.success(Map.of("pname", newFileName));
    }

    /**
     * 将图片从缓存目录移动到storage目录
     * @param filenames
     */
    public void storage(String... filenames){
        String tempPath = imageConfig.getTempPath();
               tempPath = tempPath.endsWith("/")? tempPath +  + this.SNOW+this.FLAKE: tempPath+"/" + this.SNOW+this.FLAKE;

        for (String filename : filenames) {
            File sourceFile = new File(tempPath, filename);
            File distFile = new File(imageConfig.getStoragePath(), filename);

            FileUtil.move(sourceFile, distFile, true);
        }
    }
}
