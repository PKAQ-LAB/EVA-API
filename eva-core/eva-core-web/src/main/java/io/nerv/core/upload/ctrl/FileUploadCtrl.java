package io.nerv.core.upload.ctrl;

import cn.hutool.core.map.MapUtil;
import io.nerv.core.mvc.vo.Response;
import io.nerv.core.upload.util.FileUploadProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传Ctrl
 *
*/
@Slf4j
@RestController
@RequestMapping("/upload")
@Tag(name = "上传")
public class FileUploadCtrl {

    @Autowired
    private FileUploadProvider fileUploadProvider;

    @PostMapping("/file")
    @Operation(description = "文件上传")
    public Response upload(MultipartFile file, String path) {
        String filePath = fileUploadProvider.upload(file, path);
        return new Response().success(MapUtil.of("pname", filePath));
    }
}
