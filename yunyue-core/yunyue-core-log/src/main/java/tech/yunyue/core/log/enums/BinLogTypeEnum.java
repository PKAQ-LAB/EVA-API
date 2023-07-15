package tech.yunyue.core.log.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

/**
 * 需要处理的binlog日志类型 目前只处理新增和修改
 */
@Getter
@AllArgsConstructor
public enum BinLogTypeEnum {
    INSERT("insert"),
    UPDATE("update");

    //类型
    private String type;

    public static Optional<BinLogTypeEnum> getEnumByType(String type) {
        return Arrays.stream(BinLogTypeEnum.values()).filter(e-> e.getType().equals(type)).findFirst();
    }

}
