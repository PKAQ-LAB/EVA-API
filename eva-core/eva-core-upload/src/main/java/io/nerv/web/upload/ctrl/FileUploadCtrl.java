package io.nerv.web.upload.ctrl;

import cn.hutool.core.map.MapUtil;
import io.nerv.core.mvc.vo.Response;
import io.nerv.web.upload.util.FileUploadProvider;
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
public class FileUploadCtrl {

    @Autowired
    private FileUploadProvider fileUploadProvider;

    /**
     * 文件上传
     * @param file
     * @param path
     * @return
     */
    @PostMapping("/file")
    public Response upload(MultipartFile file, String path) {
        String filePath = fileUploadProvider.upload(file, path);
        return new Response().success(MapUtil.of("pname", filePath));
    }
}
