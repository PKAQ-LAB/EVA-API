package org.pkaq.sys;


import lombok.Getter;
import org.pkaq.core.exception.BizAssert;

/**
 * @author PKAQ
 */

@Getter
public enum SysCodes implements BizAssert {
    /**
     * 用户相关 0x000-01
     **/
    ACCOUNT_ALREADY_EXIST("账户名已存在", "0x001-01000"),
    BAD_ORG_PASSWORD("原密码校验失败", "0x001-01001"),
    CANNOT_FIND_USER("权限不足或无法查询到该用户", "0x001-01002"),
    CHECKFIELD_ALREADY_EXIST("需要进行校验的字段为空", "0x001-01003"),
    CODE_ALREADY_EXIST("编码已存在", "0x001-01005"),
    ACCOUNT_OR_CODE_ALREADY_EXIST("账户名或者编码已存在", "0x001-01006"),
    USER_ACCOUNT_LIMIT("用户超出授权数量", "0x001-01007"),
    USER_ACCOUNT_ILLEGAL("非法的用户名", "0x001-01008"),
    READ_ONLY_RECORD("系统内置数据不可编辑", "0x001-01009"),

    /**
     * 租户相关 0100x-02
     **/
    TENANT_CODE_ALREADY_EXIST("编码已存在", "0x000-02000"),
    TENANT_NAME_ALREADY_EXIST("名称已存在", "0x000-02001"),
    TENANT_CODE_OR_NAME_ALREADY_EXIST("名称已存在", "0x000-02001"),
    /**
     * 租户相关 0100x-03
     **/
    MODULE_RESOURCE_USED("模块下存在已经被引用的资源，无法删除", "0x000-03001"),
    /**
     *
     */
    DICT_CODE_EXISTS("字典编码已经存在", "0x0000-10001"),
    FILE_TYPE_ERROR("文件上传格式有误，请选择支持的格式", "0x0001-00001"),
    MISS_CODE_OR_NAME("未接收到岗位管理编码或名称", "0x0001-0701-0001"),
    DUPLICATE_CODE_OR_NAME("岗位管理编码或名称已存在", "0x0001-0701-0002"),
    DELETE_LIMIT("超过最大限制,最多仅允许同时删除100条记录", "0x0001-0701-0003"),
    RECORD_NOT_FOUND("记录未找到或已删除", "0x0001-0701-0004"),
    UNABLE_TO_DELETE("该部门下已存在多个用户，无法删除", "0x0001-0701-0006"),
    DELETE_EXISTENCE_CHILD_NODE("删除记录中存在子节点,请检查数据", "0x0001-0701-0005"),
    EMAIL_ALREADY_EXIST("邮箱已存在", "0x0001-0701-0008"),
    TEL_ALREADY_EXIST("手机号已存在", "0x0001-0701-0009"),
    JOB_NUMBER_ALREADY_EXIST("工号已存在", "0x0001-0701-0010"),
    WARE_FILE_NOT_TYPE("文件类型不存在", "0x0001-0701-0011"),
    WARE_IMPORT_DOWNLOAD_ERROR("导出数据有误", "0x0001-0701-0012"),
    WARE_IMPORT_IS_EMPTY_ERROR("导入数据为空", "0x0001-0701-0014"),
    USER_IMPORT_DUPLICATE_CODE_OR_NAME("[{0}]用户账号、工号、手机号已存在", "0x0001-0701-0015"),
    USER_IMPORT_CODE_OR_NAME_NOT_EMITY("[{0}]用户账号、工号、手机号不能为空", "0x0001-0701-0016"),
    USER_IMPORT_NAME_NOT_EMITY("[{0}]用户姓名不能为空", "0x0001-0701-0017"),
    USER_IMPORT_ACCOUNT_NOT_EMITY("[{0}]用户账号不能为空", "0x0001-0701-0018"),
    USER_IMPORT_JOB_NUMBER_NOT_EMITY("[{0}]用户工号不能为空", "0x0001-0701-0019"),
    USER_IMPORT_POST_NOT_EMITY("[{0}]岗位编码不能为空", "0x0001-0701-0020"),
    USER_IMPORT_DEPT_NOT_EMITY("[{0}]部门编码不能为空", "0x0001-0701-0021"),
    USER_IMPORT_TEL_NOT_EMITY("[{0}]用户手机号不能为空", "0x0001-0701-0022"),

    USER_IMPORT_POST_NOT_FIND("[{0}]岗位编码不存在", "0x0001-0701-0023"),
    USER_IMPORT_DEPT_NOT_FIND("[{0}]部门编码不存在", "0x0001-0701-0024"),

    POST_IMPORT_TITLE_NOT_FIND("[{0}]岗位名称不存在", "0x0001-0702-0001"),
    POST_IMPORT_CODE_NOT_FIND("[{0}]岗位编码不存在", "0x0001-0702-0002"),
    POST_IMPORT_LEVE_NOT_FIND("[{0}]岗位职级不存在", "0x0001-0702-0003"),
    POST_IMPORT_DUPLICATE_CODE("[{0}]岗位编码已存在", "0x0001-0702-0004"),

    DEPT_IMPORT_TITLE_NOT_FIND("[{0}]部门名称不存在", "0x0001-0703-0001"),
    DEPT_IMPORT_CODE_NOT_FIND("[{0}]部门编码不存在", "0x0001-0703-0002"),

    DEPT_IMPORT_DUPLICATE_CODE("[{0}]部门编码已存在", "0x0001-0703-0003"),
    MODULE_RESOURCE_COED_EXIST("资源code已存在", "0x0001-0701-0010"),
    TENANT_COED_NAME_EXIST("租户code/名称已存在", "0x0001-0701-0011"),

    PATH_ALREADY_EXIST("模块路径已经存在", "4301"),

    CODE_EXIST("[{0}] 编码已经存在", "4303"),
    ROLE_CODE_EXIST("权限编码已经存在", "4304"),
    RESOURCE_USED("资源已经被引用，无法删除", "4305"),

    ORG_CODE_EXIST("组织(部门)编码已经存在", "4310"),

    ORG_TYPE_INVALID("[{0}]不能添加[{1}] ", "4313"),
    DICT_CODE_EXIST("字典编码已经存在", "4314"),
    ORG_TYPE_NO_EXIST("所属组织类型[{0}]没有定义，请稍后再试", "4315"),
    NO_CHANGE_ORG("不能修改所属组织", "4316");

    private final String msg;
    private final String code;
    private final String prefix = "sys";

    SysCodes(String msg, String code) {
        this.msg = msg;
        this.code = code;
    }
}
