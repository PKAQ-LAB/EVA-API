package io.nerv.core.mvc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Data
@Component
@PropertySource(value = "classpath:spy.properties")
public class ImageEntity {

    // 图片上传存储临时路径
    @Value("${tempPath}")
    private String tempPath;

    // 图片上传存储路径
    @Value("${storagePath}")
    private String storagePath;

    // 图片后缀名集
    @Value("${allowSuffixName}")
    private String allowSuffixName;

}
