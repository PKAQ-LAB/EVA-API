package io.nerv.core.upload.util;

import cn.hutool.core.io.resource.InputStreamResource;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.nerv.core.exception.ImageUploadException;
import io.nerv.core.upload.condition.FastDfsCondition;
import io.nerv.properties.EvaConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件上传工具类 - 使用fastdfs
 */
@Slf4j
@Component
@Conditional(FastDfsCondition.class)
public class DfsFileUploadUtil implements FileUploadProvider {

    private Snowflake snowflake = IdUtil.createSnowflake(SNOW, FLAKE);

    // 删除接口
    private final static String DELETE_API = "/delete";

    // 上传接口
    private final static String UPLOAD_API = "/upload";

    @Autowired
    private EvaConfig evaConfig;

    private final static long SNOW = 16;

    private final static long FLAKE = 18;

    /**
     * 文件上传 默认上传到配置的tmp目录
     * @param file
     * @return
     */
    @Override
    public String upload(MultipartFile file) throws IOException {
        // 上传文件名
        String fileName = file.getOriginalFilename();
        // 后缀名
        String suffixName = "";
        // 存储后返回的信息
        String newFileName = "";

        String file_path = "";

        if (StrUtil.isNotEmpty(fileName)){
            suffixName = fileName.substring(fileName.lastIndexOf(".") + 1);
            newFileName = snowflake.nextIdStr() + "." + suffixName;
        } else {
            log.error("文件名错误：");
            throw new ImageUploadException("文件名错误");
        }
        // 判断上传文件是否符合格式
        if (evaConfig.getUpload().getAllowSuffixName().contains(suffixName)){
            InputStreamResource isr = new InputStreamResource(file.getInputStream(), newFileName);

            Map<String, Object> paramMap = new HashMap<>(3);
            //文件
            paramMap.put("file", isr);
            //输出
            paramMap.put("output","json");
            //自定义路径
//            String curDate = DateUtil.format(new Date(), DatePattern.PURE_DATE_FORMAT);
//            paramMap.put("path","tmp/"+curDate);
            paramMap.put("path", suffixName);

            JSONObject jsonObject = JSON.parseObject(this.transfer(paramMap));

            file_path = jsonObject.getString("path");
        } else {
            log.error("上传格式错误：");
            throw new ImageUploadException("上传格式错误");
        }

        return file_path;
    }


    /**
     * 将文件从缓存目录移动到storage目录
     * @param filenames
     * @return
     */
    @Override
    public List<String> storage(String... filenames){ return null;}

    /**
     * 将文件从缓存目录移动到storage目录并生成缩略图
     * @param filenames
     */
    @Override
    public void storageWithThumbnail(float scale, String... filenames){}

    /**
     * 生成缩略图
     * @param file
     * @param scale
     */
    @Override
    public void thumbnail(File file, float scale){}

    /**
     * 生成缩略图
     * @param file
     * @param dest
     * @param scale
     */
    @Override
    public void thumbnail(File file, File dest, float scale){}

    /**
     * 从持久目录删除文件以及缩略图
     * @param path
     */
    @Override
    public void delFromStorage(String path){
       HttpUtil.post(evaConfig.getUpload().getServerUrl()+ this.DELETE_API, path);
    }

    /**
     * 传输到go-fastdfs
     * @return
     */
    public String transfer(Map<String, Object> map){
        return HttpUtil.post(evaConfig.getUpload().getServerUrl() + this.UPLOAD_API, map);
    }

}
