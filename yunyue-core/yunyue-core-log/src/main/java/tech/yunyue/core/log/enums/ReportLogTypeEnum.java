package tech.yunyue.core.log.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tech.yunyue.core.enums.BizCode;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 报表日志操作类型
 */
@Getter
@AllArgsConstructor
public enum ReportLogTypeEnum implements BizCode {
    EXPORT("0000", "导出"),
    EXPORT_EXCEL("0001", "导出EXCEL"),
    EXPORT_WORD("0002", "导出WORD"),
    EXPORT_PDF("0003", "导出PDF"),
    PRINT("0004", "导出"),
    PRINT_NOW("0005", "导出当前页"),
    PRINT_ALL("0006", "导出全部页");

    /**
     * 索引
     */
    private String code;
    /**
     * 名称
     */
    private String msg;

    private static Map<String, ReportLogTypeEnum> codeMap = Arrays.stream(ReportLogTypeEnum.values()).collect(Collectors.toMap(ReportLogTypeEnum::getCode, e -> e));
    public static ReportLogTypeEnum getByCode(String code) {
        return codeMap.get(code);
    }

}
