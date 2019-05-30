package io.nerv.core.mvc.ctrl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import io.nerv.core.mvc.entity.ImageEntity;
import io.nerv.core.mvc.util.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Date;
import java.util.Random;


/**
 * 图片上传Ctrl
 *
*/
@RestController("/upload")
public class UploadImageCtrl{

    @Autowired
    private ImageEntity entity;

    @PostMapping("/image")
    public Response uploadImage(MultipartFile image){
        // 上传图片名
        String fileName = image.getOriginalFilename();
        // 后缀名
        String suffixName = "";
        // 新图片名
        String newFileName = "";
        if (StringUtils.isNotEmpty(fileName)){
            suffixName = fileName.substring(fileName.lastIndexOf(".") + 1);
            newFileName = new Date().getTime()+"_"+new Random().nextInt(1000) + "." + suffixName;
        } else {
            return new Response().failure(510);
        }
        // 判断上传图片是否符合格式
        if (entity.getAllowSuffixName().contains(suffixName)){
            // 创建临时文件夹
            File tempFileFolder = new File(entity.getTempPath());
            if (!tempFileFolder.exists() && !tempFileFolder.isDirectory()){
                tempFileFolder.mkdir();
            }
            // 存储图片
            try {
                image.transferTo(new File(tempFileFolder, newFileName));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            return new Response().failure(510);
        }
        return new Response().success(entity.getTempPath() + "/" + newFileName);
    }


}
