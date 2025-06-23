package org.pkaq.core.upload.ctrl;

import cn.hutool.core.map.MapUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pkaq.core.mvc.vo.Response;
import org.pkaq.core.upload.condition.DefaultNgCondition;
import org.pkaq.core.upload.provider.FileProvider;
import org.springframework.context.annotation.Conditional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传Ctrl
 * @author PKAQ
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/upload")
@Conditional(DefaultNgCondition.class)
public class FileUploadCtrl {

    private final FileProvider fileUploadProvider;

    /**
     * 文件上传
     * @param file
     * @param path
     * @return
     */
    @PostMapping("/file")
    public Response<Object> upload(MultipartFile file, String path) throws Exception {
        String filePath = fileUploadProvider.upload(file, path);
        return Response.success(MapUtil.of("pname", filePath));
    }
}
