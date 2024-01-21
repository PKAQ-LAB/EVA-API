package tech.yunyue.core.upload.enumm;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public enum OSSBucketEnum implements IBucket {
    // oss存储
    STORAGE_OSS(BucketTypeEnum.STORAGE,"storage-18109"),
    // 临时存储桶: 临时存储桶用于存储临时文件，在用户上传文件后，业务提交前将数据暂存于此。该桶会对文件进行定时清理 (7天)
    TEMP_OSS(BucketTypeEnum.TEMP,"temp-18109"),
    // 模板存储桶: 用于存储用户导入模板，按 级业务模块创建目录结构。一级业务下所有的模板放在一起
    TEMPLATE_OSS(BucketTypeEnum.TEMPLATE,"template-18109"),
    // 报表存储桶: 用于存储报表文件，按 级业务模块创建目录结构。一级业务下所有的模板放在一起
    REPORT_OSS(BucketTypeEnum.REPORT,"report-18109"),
    // 归档桶: 业务数据为逻辑删除，该桶用于用户删除业务数据后的文件暂存便于数据的紧急恢复。该桶会对文件进行定时清理(30天)
    ARCHIVE_OSS(BucketTypeEnum.ARCHIVE,"archive-18109");

    String bucketName;
    BucketTypeEnum bucketEnum;

    private static final Map<BucketTypeEnum, OSSBucketEnum> map = Arrays.stream(OSSBucketEnum.values()).collect(Collectors.toMap(OSSBucketEnum::getBucketEnum, e -> e));


    OSSBucketEnum(BucketTypeEnum bucketEnum, String name) {
        this.bucketName = name;
        this.bucketEnum = bucketEnum;
    }

    @Override
    public String getBucketNameByType(BucketTypeEnum iBucketEnum){
        return map.get(iBucketEnum).getBucketName();
    }
}
