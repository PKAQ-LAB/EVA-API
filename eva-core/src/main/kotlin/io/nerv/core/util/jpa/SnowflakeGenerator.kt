package io.nerv.core.util.jpa

import cn.hutool.core.util.IdUtil
import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.id.UUIDGenerator
import java.io.Serializable

/**
 * 自定义雪花ID生成器
 */
class SnowflakeGenerator : UUIDGenerator() {
    private val snowflake = IdUtil.getSnowflake(1, 1)
    override fun generate(session: SharedSessionContractImplementor, `object`: Any): Serializable {
        val id: Any? = snowflake.nextIdStr()
        return if (id != null) {
            id as Serializable
        } else super.generate(session, `object`)
    }
}