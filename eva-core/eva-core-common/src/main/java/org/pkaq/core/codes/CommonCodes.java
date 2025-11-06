package org.pkaq.core.codes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.pkaq.core.exception.BizAssert;

/**
 * 状态码
 * 错误统一为 4xxx
 *
 * @author PKAQ
 */
@Getter
@AllArgsConstructor
public enum CommonCodes implements BizAssert {
    SERVER_ERROR("服务器发生错误,请联系管理员", "0x000-00000"),
    SERVER_ERROR_CONVERT("服务器发生错误,请联系管理员", "0x000-00001"),
    SERVER_ERROR_ENUM_UNKNOWN_CODE("服务器发生错误,请联系管理员", "0x000-00003"),
//    Enum type not initialized in UniversalEnumTypeHandler
    SERVER_ERROR_ENUM_NOTINIT("服务器发生错误,请联系管理员", "0x000-00004"),
//    No enum constant
    SERVER_ERROR_ENUM_404("服务器发生错误,请联系管理员", "0x000-00005"),
//    Invalid len
    SERVER_ERROR_BCR_LEN("服务器发生错误,请联系管理员", "0x000-00006"),
    //Invalid maxolen
    SERVER_ERROR_BCR_MAX("服务器发生错误,请联系管理员", "0x000-00007"),
//    Bad number of rounds
    SERVER_ERROR_BCR_BNOR("服务器发生错误,请联系管理员", "0x000-00008"),
//    Bad salt length
    SERVER_ERROR_BCR_BSL("服务器发生错误,请联系管理员", "0x000-00009"),
//    Invalid salt version
    SERVER_ERROR_BCR_ISV("服务器发生错误,请联系管理员", "0x000-00010"),
//    Invalid salt revision
    SERVER_ERROR_BCR_ISR("服务器发生错误,请联系管理员", "0x000-00011"),
//    Missing salt rounds
    SERVER_ERROR_BCR_MSR("服务器发生错误,请联系管理员", "0x000-00012"),
//    rounds exceeds maximum (30)
    SERVER_ERROR_BCR_REM("服务器发生错误,请联系管理员", "0x000-00013"),
//    log_rounds exceeds maximum (30)
    SERVER_ERROR_BCR_LREM("服务器发生错误,请联系管理员", "0x000-00014"),
//    NodeId must be between %d and %d
    SERVER_ERROR_IDGET_LREM("服务器发生错误,请联系管理员", "0x000-00015"),
//    Invalid System Clock!
    SERVER_ERROR_IDGET_ISC("服务器发生错误,请联系管理员", "0x000-00016"),



    CAN_NOT_FIND_RECORD("无法找到指定记录 [{0}]", "0x000-02001"),
    CHILD_EXIST("[{0}] 存在子节点，无法删除", "0x000-02002"),
    PARENT_NOT_AVAILABLE("父节点为禁用状态，无法启用", "0x000-02"),
    NULL_ID("所需记录ID为空值", "0x001"),
    NULL_PARAM_ID("所需记录 [ {0} ] 为空值", "0x002"),

    /**
     * 请求成功 000x
     **/
    OPERATE_SUCCESS("操作成功", "0000"),
    LOGIN_SUCCESS("登录成功，欢迎回来", "0001"),
    SAVE_SUCCESS("数据保存成功", "0002"),
    LOGIN_SUCCESS_WELCOME("登录成功，欢迎回来 [{0}]", "0003"),
    LOGINOUT_SUCCESS("已经成功退出登录", "0005"),

    LICENSE_INSTALLED("License 安装成功", "0010"),
    LICENSE_VERIFIED("License 验证成功", "0011"),
    LICENSE_REFRESH("License 更新成功", "0012"),
    LICENSE_IPMAC_VERIFIED("IP、MAC地址验证通过", "0013"),
    LOGIN_WARNING_CONTINUE("该用户已登录", "0014"),

    /**
     * 请求失败 400x
     **/
    PARAM_LOST("请求参数丢失", "0x0400-00001"),
    TOKEN_NOT_VERIFY("Token校验异常", "4000"),
    PARAM_ERROR("请求参数错误", "0x0400-00002"),
    DUPLICATE_CODE_ERROR("编码校验失败,存在重复编码", "0x0400-00003"),
    PARAM_TYPE_ERROR("参数类型错误 ", "4008"),
    PARAM_LENGTH("参数长度错误", "4009"),

    REQUEST_METHOD_ERROR("服务器不支持当前请求的方法", "4011"),
    REQUEST_TOO_MORE("当前请求过于频繁，请稍后再试", "4012"),
    REQUEST_MEDIA_ERROR("服务器不支持当前请求的类型", "4015"),

    /**
     * 权限相关 420x
     **/
    PERMISSION_DENY("请求资源所需的权限不足", "4200"),
    BLACK_IP_DENY("当前请求ip[{0}]在黑名单中，拒绝访问", "4201"),
    MID_DENY("模块信息校验不通过，拒绝访问", "4202"),

    /**
     * license相关 421x
     **/
    LICENSE_BADIP("IP 地址验证不通过", "4210"),
    LICENSE_BADMAC("MAC 地址验证不通过", "4211"),
    LICENSE_LICENSEHASEXPIRED("证书已经过期", "4213"),
    LICENSE_LICENSEVERIFYFAILED("证书验证失败", "4214"),


    CAN_NOT_INSERT_HISTORY("无法插入历史快照，[{0}]没有使用@TableName/@HistoryLog指定表名", "4319"),
    HISTORY_LOG_CAN_NOT_NULL("历史快照类不能为空", "4320"),

    /**
     * 上传相关 430x ~ 432x
     **/
    FILEIO_ERROR("文件读取时发生错误", "0x0500-00001"),
    FILENAME_ERROR("文件名称错误", "0x0500-00002"),
    FILESAVE_ERROR("文件保存失败", "0x0500-00003"),
    FILEDEL_ERROR("文件删除失败", "0x0500-00004"),
    FILE_SRCORDESTNULL_ERROR("源文件或目标文件缺失", "0x0500-00005"),
    FILETYPE_NOT_SUPPORTED("不支持的文件格式", "0x0500-00006"),
    FILE_SIZE_EXCEEDS_LIMIT("文件大小超出限制", "0x0500-00007"),
    FILE_CANNOT_READPIC("无法读取图片数据，可能不是有效的图像格式", "0x0500-00008"),

    /**
     * 微信 440x ~ 442x
     **/
    WEIXIN_JSTICKET_ERROR("获取jsapi_ticket失败", "4401"),
    WEIXIN_PAYSIGN_ERROR("支付回调验签失败", "4402");

    /**
     * 名称
     */
    private final String msg;
    /**
     * 索引
     */
    private final String code;

    private final String prefix = "common";
}
