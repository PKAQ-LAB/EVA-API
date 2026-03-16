package org.pkaq.core.auth.openapi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.pkaq.core.auth.openapi.entity.AppCredentialEntity;

/**
 * OpenAPI凭证Mapper
 *
 * @author PKAQ
 */
@Mapper
public interface AppCredentialMapper extends BaseMapper<AppCredentialEntity> {

    /**
     * 根据AppKey查询凭证
     *
     * @param appKey AppKey
     * @return AppCredential对象, 不存在返回null
     */
    @Select("SELECT * FROM api_app_credential WHERE app_key = #{appKey}")
    AppCredentialEntity findByAppKey(String appKey);

    /**
     * 检查AppKey是否存在
     *
     * @param appKey AppKey
     * @return true-存在, false-不存在
     */
    @Select("SELECT COUNT(*) > 0 FROM api_app_credential WHERE app_key = #{appKey}")
    boolean existsByAppKey(String appKey);
}
