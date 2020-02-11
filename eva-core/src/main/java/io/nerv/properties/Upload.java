package io.nerv.properties;

import lombok.Data;

/**
 * 文件上传配置类
 */
@Data
public class Upload {

    // 上传存储临时路径
    private String tempPath;

    // 上传存储路径
    private String storagePath;

    // 后缀名集
    private String allowSuffixName;

    // DFS服务器地址
    private String serverUrl;

}
