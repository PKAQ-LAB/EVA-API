package io.nerv.config;

import io.nerv.core.enums.BizCode;
import lombok.Getter;

@Getter
public enum PdosEnum implements BizCode {
    CATEGORY_CODE_DUPLICATE("商品分类编码已存在", "18101");

    /**
     * 名称
     */
    private String name;
    /**
     * 索引
     */
    private String index;

    PdosEnum(String name, String index) {
        this.name = name;
        this.index = index;
    }

    public static String getName(String index) {
        for (PdosEnum p : PdosEnum.values()) {
            if ( index.equals(p.getIndex())) {
                return p.name;
            }
        }
        return "-";
    }
}
