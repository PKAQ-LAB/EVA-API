package io.nerv.core.upload.ctrl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import io.nerv.core.enums.HttpCodeEnum;
import io.nerv.core.upload.config.ImageConfig;
import io.nerv.core.mvc.util.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Map;

/**
 * 图片上传Ctrl
 *
*/
@Slf4j
@RestController
@RequestMapping("/upload")
public class UploadImageCtrl{

    @Autowired
    private ImageConfig imageConfig;

    private Snowflake snowflake = IdUtil.createSnowflake(1, 1);

    @PostMapping("/image")
    public Response uploadImage(MultipartFile image){
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

        return response.success(MapUtil.of("pname", newFileName));
    }


}
