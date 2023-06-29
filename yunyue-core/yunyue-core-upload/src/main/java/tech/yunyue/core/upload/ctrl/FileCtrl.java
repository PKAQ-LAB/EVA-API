package tech.yunyue.core.upload.ctrl;

import cn.hutool.core.map.MapUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import tech.yunyue.core.mvc.vo.Response;
import tech.yunyue.core.upload.util.FileProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;

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
     * @param path
     * @return
     */
    @PostMapping("/upload")
    public Response upload(MultipartFile file) throws Exception {
        String filePath = fileProvider.upload(file, "");
        return new Response().success(MapUtil.of("pname", filePath));
    }

    /**
     * 文件下载
     */
    @GetMapping("/download")
    public void download(HttpServletResponse response, String fileName) throws IOException {
        response.setCharacterEncoding("utf-8");
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        var name = fileName.substring(fileName.lastIndexOf("/")+1);;
        //非图片文件返回原文件名
        if(!fileName.startsWith(IMAGE+"/")){
            name = name.substring(name.lastIndexOf(":")+1);
        }
        response.addHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode(name, "UTF-8"));
        fileProvider.downLoad(fileName, response.getOutputStream());
    }
    /**
     * 文件预览 原图
     */
    @GetMapping("/preview")
    public Response<String> preview(String fileName){
        return new Response<String>().success(fileProvider.preview(false, fileName));
    }

    /**
     * 查看临时桶的图片
     */
    @GetMapping("/previewTemp")
    public Response<String> previewTemp(String fileName){
        return new Response<String>().success(fileProvider.previewTemp(fileName));
    }
}
