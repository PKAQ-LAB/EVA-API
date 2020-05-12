package io.nerv.core.enums

import lombok.AllArgsConstructor
import lombok.Getter

/**
 * @author: S.PKAQ
 */
enum class StatusEnumm(var value: String, var code: String)  : BizCode {
    /**
     * 可用
     */
    ENABLE("正常", "0001"),
    UNABLE("不可用", "0000");

    override fun getName(): String? {
        return this.value
    }

    override fun getIndex(): String? {
        return this.code
    }
}