package tech.yunyue.core.upload.enumm;

import lombok.Getter;

@Getter
public enum MinIOBucketEnum {

    // 持久化存储桶: 用于业务保存后将临时桶的文件进行持久化存储
    STORAGE("storage"),
    // 临时存储桶: 临时存储桶用于存储临时文件，在用户上传文件后，业务提交前将数据暂存于此。该桶会对文件进行定时清理 (7天)
    TEMP("temp"),
    // 模板存储桶: 用于存储用户导入模板，按 级业务模块创建目录结构。一级业务下所有的模板放在一起
    TEMPLATE("template"),
    // 报表存储桶: 用于存储报表文件，按 级业务模块创建目录结构。一级业务下所有的模板放在一起
    REPORT("report"),
    // 归档桶: 业务数据为逻辑删除，该桶用于用户删除业务数据后的文件暂存便于数据的紧急恢复。该桶会对文件进行定时清理(30天)
    ARCHIVE("archive");
    String bucketName;

    MinIOBucketEnum(String name) {
        this.bucketName = name;
    }
}
