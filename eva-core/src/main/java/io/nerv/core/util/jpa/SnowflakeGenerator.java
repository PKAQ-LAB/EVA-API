package io.nerv.core.util.jpa;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.UUIDGenerator;

import java.io.Serializable;

/**
 * 自定义雪花ID生成器
 */
public class SnowflakeGenerator extends UUIDGenerator {

    private Snowflake snowflake = IdUtil.getSnowflake(1, 1);

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object){
        Object id = snowflake.nextIdStr();
        if (id != null) {
            return (Serializable) id;
        }
        return super.generate(session, object);
    }
}
