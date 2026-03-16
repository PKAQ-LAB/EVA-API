package org.pkaq.core.auth.openapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pkaq.core.auth.openapi.entity.AppCredentialEntity;
import org.pkaq.core.auth.openapi.mapper.AppCredentialMapper;
import org.springframework.stereotype.Service;

/**
 * OpenAPI凭证服务
 *
 * @author PKAQ
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppKeyService {

    private final AppCredentialMapper appCredentialMapper;

    /**
     * 获取AppKey凭证
     *
     * @param appKey AppKey
     * @return AppCredential对象, 不存在返回null
     */
    public AppCredentialEntity getCredential(String appKey) {
        log.debug("获取AppKey凭证 - appKey: {}", appKey);

        AppCredentialEntity credential = appCredentialMapper.findByAppKey(appKey);
        if (credential != null) {
            log.debug("数据库加载凭证 - appKey: {}, appName: {}", appKey, credential.getAppName());
        } else {
            log.debug("数据库中未找到AppKey: {}", appKey);
        }

        return credential;
    }

    /**
     * 创建AppKey凭证
     *
     * @param credential AppCredential对象
     * @return 创建后的对象
     */
    public AppCredentialEntity createCredential(AppCredentialEntity credential) {
        log.info("创建AppKey凭证 - 应用: {}, appKey: {}", credential.getAppName(), credential.getAppKey());

        try {
            appCredentialMapper.insert(credential);
            log.info("凭证创建成功 - appKey: {}, id: {}", credential.getAppKey(), credential.getId());
            return credential;
        } catch (Exception e) {
            log.error("创建凭证失败 - 应用: {}", credential.getAppName(), e);
            throw e;
        }
    }

    /**
     * 更新AppKey凭证
     *
     * @param credential AppCredential对象
     * @return 是否更新成功
     */
    public boolean updateCredential(AppCredentialEntity credential) {
        log.info("更新AppKey凭证 - appKey: {}", credential.getAppKey());

        try {
            boolean success = appCredentialMapper.updateById(credential) > 0;
            if (success) {
                log.info("凭证更新成功 - appKey: {}", credential.getAppKey());
            } else {
                log.warn("凭证更新失败 - appKey: {}", credential.getAppKey());
            }

            return success;
        } catch (Exception e) {
            log.error("更新凭证异常 - appKey: {}", credential.getAppKey(), e);
            throw e;
        }
    }

    /**
     * 检查AppKey是否存在
     *
     * @param appKey AppKey
     * @return 是否存在
     */
    public boolean exists(String appKey) {
        return appCredentialMapper.existsByAppKey(appKey);
    }
}
