package io.nerv.core.upload.ctrl;

import cn.hutool.core.map.MapUtil;
import io.nerv.core.mvc.util.Response;
import io.nerv.core.upload.util.FileUploadProvider;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 文件上传Ctrl
 *
*/
@Slf4j
@RestController
@RequestMapping("/upload")
@Api("上传")
public class FileUploadCtrl {

    @Autowired
    private FileUploadProvider fileUploadProvider;


    @PostMapping("/file")
    @ApiOperation(value = "文件上传",response = Response.class)
    public Response upload(MultipartFile file) {
        String filePath = fileUploadProvider.upload(file);

        Response response = new Response();

        return response.success(MapUtil.of("pname", filePath));
    }


}
