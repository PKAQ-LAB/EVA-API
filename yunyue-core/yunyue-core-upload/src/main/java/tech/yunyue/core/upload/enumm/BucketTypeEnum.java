package tech.yunyue.core.upload.enumm;

import cn.hutool.core.util.ReflectUtil;

/**
 * 枚举类型，标识不同的存储桶。没有具体的桶名称
 * @author 茂茂AdamEve
 */
public enum BucketTypeEnum {
    // 持久化存储桶: 用于业务保存后将临时桶的文件进行持久化存储
    STORAGE,
    // 临时存储桶: 临时存储桶用于存储临时文件，在用户上传文件后，业务提交前将数据暂存于此。该桶会对文件进行定时清理 (7天)
    TEMP,
    // 模板存储桶: 用于存储用户导入模板，按 级业务模块创建目录结构。一级业务下所有的模板放在一起
    TEMPLATE,
    // 报表存储桶: 用于存储报表文件，按 级业务模块创建目录结构。一级业务下所有的模板放在一起
    REPORT,
    // 归档桶: 业务数据为逻辑删除，该桶用于用户删除业务数据后的文件暂存便于数据的紧急恢复。该桶会对文件进行定时清理(30天)
    ARCHIVE;

    /**
     * 根据当前类型获取存储桶的名称。
     *
     * @param clazz 实际存储桶名称的枚举类
     * @param <T>   实现了 IBucket 接口的存储桶类型。
     * @return 存储桶的名称。
     */
    public <T extends IBucket> String getBucketName(Class<T> clazz) {
        // 获取枚举实例
        T enumInstance = clazz.getEnumConstants()[0];
        // 枚举方法
        var method = ReflectUtil.getMethodByName(clazz, IBucket.BUCKET_NAME_BY_TYPE_METHOD);
        return ReflectUtil.invoke(enumInstance, method, this);
    }
}

