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
    CAN_NOT_FIND_RECORD("无法找到指定记录 [{0}]", "0x000-02001"),
    CHILD_EXIST("[{0}] 存在子节点，无法删除", "4302"),
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
    TOKEN_NOT_VERIFY("Token校验异常", "4000"),
    PARAM_ERROR("请求参数错误", "4006"),
    PARAM_LOST("请求参数丢失", "4007"),
    PARAM_TYPEERROR("参数类型错误 ", "4008"),
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
    FILEIO_ERROR("文件读取时发生错误", "4306"),
    FILENAME_ERROR("文件名称错误", "4307"),
    FILESAVE_ERROR("文件保存失败", "4308"),
    FILETYPE_NOT_SUPPORTED("不支持的文件格式", "4309"),
    FILE_SIZE_EXCEEDS_LIMIT("文件大小超出限制", "4310"),

    /**
     * 微信 440x ~ 442x
     **/
    WEIXIN_JSTICKET_ERROR("获取jsapi_ticket失败", "4401"),
    WEIXIN_PAYSIGN_ERROR("支付回调验签失败", "4402");

    /**
     * 名称
     */
    private final String msg ;
    /**
     * 索引
     */
    private final String code;

    private final String prefix = "common";
}
