package tech.yunyue.core.upload.enumm;

/**
 * 桶名接口
 *
 * @author 茂茂AdamEve
 */
public interface IBucket {
    String getBucketName();

    // 根据存储类型返回对应的存储桶的方法名 通过反射调用该方法
    String BUCKET_NAME_BY_TYPE_METHOD = "getBucketNameByType";
    // 根据存储类型返回对应的存储桶的方法
    String getBucketNameByType(BucketTypeEnum typeEnum);
}
