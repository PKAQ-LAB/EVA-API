package io.nerv.core.upload.ctrl;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import io.nerv.core.enums.BizCodeEnum;
import io.nerv.core.exception.BizException;
import io.nerv.core.mvc.util.Response;
import io.nerv.properties.EvaConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * 图片上传Ctrl
 *
*/
@Slf4j
@RestController
@RequestMapping("/upload")
public class ImageUploadCtrl {

    @Autowired
    private EvaConfig evaConfig;

    private Snowflake snowflake = IdUtil.createSnowflake(1, 1);

    @PostMapping("/image")
    public Response uploadImage(MultipartFile file){
        Response response = new Response();
        // 上传图片名
        String fileName = file.getOriginalFilename();
        // 后缀名
        String suffixName = "";
        // 新图片名
        String newFileName = "";
        if (StringUtils.isNotEmpty(fileName)){
            suffixName = fileName.substring(fileName.lastIndexOf(".") + 1);
            newFileName = snowflake.nextIdStr() + "." + suffixName;
        } else {
            log.error("文件名错误：");
            throw new BizException(BizCodeEnum.FILENAME_ERROR);
        }
        // 判断上传图片是否符合格式
        if (evaConfig.getUpload().getAllowSuffixName().toLowerCase().contains(suffixName)){
            String destPath = evaConfig.getUpload().getTempPath();
            if (StrUtil.isNotBlank(destPath) && !destPath.endsWith("/")){
                destPath += "/";
            }

            // 创建临时文件夹
            File tempFile = new File(destPath + newFileName);

            if (!tempFile.getParentFile().exists()){
                tempFile.getParentFile().mkdir();
            }
            // 存储图片
            try {
                file.transferTo(tempFile);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("图片保存错误："+e.getMessage());
                throw new BizException(BizCodeEnum.FILESAVE_ERROR);
            }
        } else {
            log.error("上传格式错误：");
            throw new BizException(BizCodeEnum.FILETYPE_NOT_SUPPORTED);
        }

        return response.success(MapUtil.of("pname", newFileName));
    }


}
