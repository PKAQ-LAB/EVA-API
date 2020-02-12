package io.nerv.core.upload.ctrl;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import io.nerv.core.enums.BizCodeEnum;
import io.nerv.core.exception.BizException;
import io.nerv.core.mvc.util.Response;
import io.nerv.core.upload.util.FileUploadProvider;
import io.nerv.properties.EvaConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * 图片上传Ctrl
 *
*/
@Slf4j
@RestController
@RequestMapping("/upload")
public class FileUploadCtrl {

    @Autowired
    private EvaConfig evaConfig;

    @Autowired
    private FileUploadProvider fileUploadProvider;

    private Snowflake snowflake = IdUtil.createSnowflake(1, 1);

    @PostMapping("/image")
    public Response uploadImage(MultipartFile file) throws IOException {
        String filePath = fileUploadProvider.upload(file);

        Response response = new Response();

        return response.success(MapUtil.of("pname", filePath));
    }


}
