package tech.yunyue.core.upload.ctrl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.map.MapUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import tech.yunyue.core.mvc.vo.Response;
import tech.yunyue.core.upload.enumm.MinIOBucketEnum;
import tech.yunyue.core.upload.util.FileProvider;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文件Ctrl
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/file")
public class FileCtrl {

    private final FileProvider fileProvider;
    private static final String IMAGE = "images";


    /**
     * 文件上传
     *
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public Response upload(MultipartFile file) throws Exception {
        String filePath = fileProvider.upload(file);
        Map<String, String> result = MapUtil.of("pname", filePath);

        // 是图片就返回预览链接
        boolean isPic = FileProvider.SUFFIXSTR.contains(FileUtil.extName(filePath));
        if (isPic) {
            result.put("previewUrl", fileProvider.preview(filePath, MinIOBucketEnum.TEMP));
        }
        return new Response().success(result);
    }

    /**
     * 文件下载
     */
    @GetMapping("/download")
    public void download(HttpServletResponse response, String fileName) throws IOException {
        response.setCharacterEncoding("utf-8");
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        var name = fileName.substring(fileName.lastIndexOf("/") + 1);

        //非图片文件返回原文件名
        if (!fileName.startsWith(IMAGE + "/")) {
            name = name.substring(name.lastIndexOf(":") + 1);
        }
        response.addHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode(name, "UTF-8"));
        fileProvider.downLoad(fileName, response.getOutputStream());
    }


    /**
     * 文件预览
     */
    @GetMapping("/preview")
    public Response<String> preview(String fileName) {
        // 前端传来的fileName可能是缩略图的预览url,需要处理一下得到真正的文件名  http://127.0.0.1:9000/storage/images/202309/11/thumbnail_1701044951689003008.jpg?X-Amz-Algorithm=AW...
        fileName = fileProvider.parsePreviewUrlToFileName(fileName);
        return new Response<String>().success(fileProvider.preview(fileName));
    }

}
