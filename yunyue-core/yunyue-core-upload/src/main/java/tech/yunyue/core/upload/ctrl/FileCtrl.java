package tech.yunyue.core.upload.ctrl;

import cn.hutool.core.map.MapUtil;
import tech.yunyue.core.mvc.vo.Response;
import tech.yunyue.core.upload.util.FileProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件Ctrl
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/upload")
public class FileCtrl {

    private final FileProvider fileProvider;

    /**
     * 文件上传
     *
     * @param file
     * @param path
     * @return
     */
    @PostMapping("/file")
    public Response upload(MultipartFile file, String path) {
        String filePath = fileProvider.upload(file, path);
        return new Response().success(MapUtil.of("pname", filePath));
    }
}
