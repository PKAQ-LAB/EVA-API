package tech.yunyue.core.properties;

import lombok.Data;
import org.springframework.util.unit.DataSize;

import java.util.Map;
import java.util.concurrent.TimeUnit;

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

    // 不同后缀文件的大小限制
    private Map<String, DataSize> suffixMaxSize;

    // 图片缩略长宽
    private int scaleWidth = 300;
    private int scaleHeight = 300;

    // 预览链接失效时间
    private int duration = 1;
    // 预览链接失效时间单位
    private TimeUnit timeUnit = TimeUnit.MINUTES;

    private MinIO minIo;

    @Data
    public static class MinIO {
        private String url;
        private String access;
        private String secret;
    }

    private Oss oss;

    @Data
    public static class Oss {
        private String url;
        private String access;
        private String secret;
    }
}
