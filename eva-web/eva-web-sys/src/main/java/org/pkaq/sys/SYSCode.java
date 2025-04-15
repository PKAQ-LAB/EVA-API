package org.pkaq.sys;


import org.pkaq.core.exception.BizAssert;

/**
 * @author dmz
 */
public enum SYSCode implements BizAssert {
    FILE_TYPE_ERROR("文件上传格式有误，请选择支持的格式", "00000-00001"),
    MISS_CODE_OR_NAME("未接收到岗位管理编码或名称", "0x001-0701-0001"),
    DUPLICATE_CODE_OR_NAME("岗位管理编码或名称已存在", "0x001-0701-0002"),
    DELETE_LIMIT("超过最大限制,最多仅允许同时删除100条记录", "0x001-0701-0003"),
    RECORD_NOT_FOUND("记录未找到或已删除", "0x001-0701-0004"),
    UNABLE_TO_DELETE("该部门下已存在多个用户，无法删除", "0x001-0701-0006"),
    DELETE_EXISTENCE_CHILD_NODE("删除记录中存在子节点,请检查数据", "0x001-0701-0005"),
    ACCOUNT_ALREADY_EXIST("账号名已存在", "0x001-0701-0007"),
    EMAIL_ALREADY_EXIST("邮箱已存在", "0x001-0701-0008"),
    TEL_ALREADY_EXIST("手机号已存在", "0x001-0701-0009"),
    JOB_NUMBER_ALREADY_EXIST("工号已存在", "0x001-0701-0010"),
    WARE_FILE_NOT_TYPE("文件类型不存在", "0x001-0701-0011"),
    WARE_IMPORT_DOWNLOAD_ERROR("导出数据有误", "0x001-0701-0012"),
    WARE_IMPORT_IS_EMPTY_ERROR("导入数据为空", "0x001-0701-0014"),
    USER_IMPORT_DUPLICATE_CODE_OR_NAME("[{0}]用户账号、工号、手机号已存在", "0x001-0701-0015"),
    USER_IMPORT_CODE_OR_NAME_NOT_EMITY("[{0}]用户账号、工号、手机号不能为空", "0x001-0701-0016"),
    USER_IMPORT_NAME_NOT_EMITY("[{0}]用户姓名不能为空", "0x001-0701-0017"),
    USER_IMPORT_ACCOUNT_NOT_EMITY("[{0}]用户账号不能为空", "0x001-0701-0018"),
    USER_IMPORT_JOB_NUMBER_NOT_EMITY("[{0}]用户工号不能为空", "0x001-0701-0019"),
    USER_IMPORT_POST_NOT_EMITY("[{0}]岗位编码不能为空", "0x001-0701-0020"),
    USER_IMPORT_DEPT_NOT_EMITY("[{0}]部门编码不能为空", "0x001-0701-0021"),
    USER_IMPORT_TEL_NOT_EMITY("[{0}]用户手机号不能为空", "0x001-0701-0022"),

    USER_IMPORT_POST_NOT_FIND("[{0}]岗位编码不存在", "0x001-0701-0023"),
    USER_IMPORT_DEPT_NOT_FIND("[{0}]部门编码不存在", "0x001-0701-0024"),

    POST_IMPORT_TITLE_NOT_FIND("[{0}]岗位名称不存在", "0x001-0702-0001"),
    POST_IMPORT_CODE_NOT_FIND("[{0}]岗位编码不存在", "0x001-0702-0002"),
    POST_IMPORT_LEVE_NOT_FIND("[{0}]岗位职级不存在", "0x001-0702-0003"),
    POST_IMPORT_DUPLICATE_CODE("[{0}]岗位编码已存在", "0x001-0702-0004"),


    DEPT_IMPORT_TITLE_NOT_FIND("[{0}]部门名称不存在", "0x001-0703-0001"),
    DEPT_IMPORT_CODE_NOT_FIND("[{0}]部门编码不存在", "0x001-0703-0002"),

    DEPT_IMPORT_DUPLICATE_CODE("[{0}]部门编码已存在", "0x001-0703-0003"),
    MODULE_RESOURCE_COED_EXIST("资源code已存在", "0x001-0701-0010"),
    TENANT_COED_NAME_EXIST("租户code/名称已存在", "0x001-0701-0011"),
    TENANT_AUTH_COUNT_MORE("用户超出授权数量", "0x001-0701-0012");

    private String msg;
    private String code;

    private SYSCode(String msg, String code) {
        this.msg = msg;
        this.code = code;
    }

    public String getMsg() {
        return this.msg;
    }

    public String getCode() {
        return this.code;
    }
}
