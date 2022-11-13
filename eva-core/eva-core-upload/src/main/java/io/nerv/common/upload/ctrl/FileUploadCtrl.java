package io.nerv.common.upload.ctrl;

import cn.hutool.core.map.MapUtil;
import io.nerv.common.mvc.vo.Response;
import io.nerv.common.upload.util.FileUploadProvider;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@RequestMapping("/upload")
public class FileUploadCtrl {

    private final FileUploadProvider fileUploadProvider;

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
