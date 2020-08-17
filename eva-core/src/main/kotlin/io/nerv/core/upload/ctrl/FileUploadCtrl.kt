package io.nerv.core.upload.ctrl

import cn.hutool.core.map.MapUtil
import io.nerv.core.mvc.util.Response
import io.nerv.core.upload.util.FileUploadProvider
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import lombok.extern.slf4j.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

/**
 * 文件上传Ctrl
 *
 */
@RestController
@RequestMapping("/upload")
@Api("上传")
class FileUploadCtrl {
    @Autowired
    var fileUploadProvider: FileUploadProvider? = null

    @PostMapping("/file")
    @ApiOperation(value = "文件上传", response = Response::class)
    fun upload(file: MultipartFile, path: String?): Response {
        val filePath = fileUploadProvider!!.upload(file, path)
        return Response().success(MapUtil.of("pname", filePath))
    }
}