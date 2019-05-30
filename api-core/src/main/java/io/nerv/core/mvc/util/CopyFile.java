package io.nerv.core.mvc.util;

import io.nerv.core.mvc.entity.ImageEntity;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@PropertySource(value = "classpath:spy.properties")
public class CopyFile {

    @Autowired
    private ImageEntity entity;

    // 声明当前类的静态对象
    public static CopyFile copyFile;

    // 声明当前类的静态对象
    @PostConstruct
    public void init() {
        copyFile = this;
    }

    /**
     * 复制temp子文件夹至storage目录下
     */
    public static Response copyFile(String tempPath){
        // temp文件夹
        File tempFile = new File(tempPath);
        // 创建storage文件夹
        String dataFolder = new SimpleDateFormat("yyyy-MM-dd").format(new Date()).toString();
        File storageFile = new File(copyFile.entity.getStoragePath() + "/" + dataFolder);
        if (!storageFile.exists() && !storageFile.isDirectory()){
            storageFile.mkdir();
        }
        try {
            // 复制文件到目标文件夹
            FileUtils.copyDirectory(tempFile, storageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Response().success();
    }
}
