package io.nerv.properties;

import lombok.Data;

/**
 * 文件上传配置类
 */
@Data
public class Upload {

    // 图片上传存储临时路径
    private String tempPath;

    // 图片上传存储路径
    private String storagePath;

    // 图片后缀名集
    private String allowSuffixName;

}
