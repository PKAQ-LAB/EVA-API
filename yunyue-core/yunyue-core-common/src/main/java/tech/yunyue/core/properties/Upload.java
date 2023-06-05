package tech.yunyue.core.properties;

import lombok.Data;

/**
 * 文件上传配置类
 */
@Data
public class Upload {
    // 使用的存储类型
    private String type;

    // 上传存储临时路径
    private String tempPath = "temp";

    // 上传存储路径
    private String storagePath = "storage";

    // 后缀名集
    private String allowSuffixName;

    // DFS服务器地址
    private String serverUrl;

    // MinIO服务器地址
    private String minioUrl;

    //MinIO服务器的Access key
    private String minioAccess;

    //MinIO服务器的secret Key
    private String minioSecret;
}
