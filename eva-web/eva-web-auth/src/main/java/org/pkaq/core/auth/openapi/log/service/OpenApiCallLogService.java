package org.pkaq.core.auth.openapi.log.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pkaq.core.auth.openapi.entity.AppCredentialEntity;
import org.pkaq.core.auth.openapi.log.bo.OpenApiCallLogQueryBo;
import org.pkaq.core.auth.openapi.log.entity.OpenApiCallLogEntity;
import org.pkaq.core.auth.openapi.log.mapper.OpenApiCallLogMapper;
import org.pkaq.core.auth.openapi.log.vo.OpenApiCallLogVo;
import org.pkaq.core.util.StrUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * OpenAPI调用日志服务。
 *
 * @author PKAQ
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OpenApiCallLogService {
    private static final int MAX_APP_KEY_LENGTH = 100;
    private static final int MAX_APP_NAME_LENGTH = 100;
    private static final int MAX_PATH_LENGTH = 500;
    private static final int MAX_METHOD_LENGTH = 16;
    private static final int MAX_IP_LENGTH = 64;
    private static final int MAX_ERROR_LENGTH = 500;

    private final OpenApiCallLogMapper openApiCallLogMapper;

    /**
     * 根据ID查询调用日志。
     *
     * @param id 日志ID
     * @return 调用日志
     */
    public OpenApiCallLogVo get(Long id) {
        return toVo(this.openApiCallLogMapper.selectById(id));
    }

    /**
     * 分页查询调用日志。
     *
     * @param queryBo 查询参数
     * @return 调用日志分页数据
     */
    public IPage<OpenApiCallLogVo> list(OpenApiCallLogQueryBo queryBo) {
        OpenApiCallLogQueryBo safeQueryBo = queryBo == null ? new OpenApiCallLogQueryBo() : queryBo;
        Page<OpenApiCallLogEntity> page = new Page<>(safeQueryBo.getPageNo(), safeQueryBo.getPageSize());
        return this.openApiCallLogMapper.selectPage(page, Wrappers.<OpenApiCallLogEntity>lambdaQuery()
                        .like(StrUtils.isNotBlank(safeQueryBo.getAppKey()),
                                OpenApiCallLogEntity::getAppKey, safeQueryBo.getAppKey())
                        .like(StrUtils.isNotBlank(safeQueryBo.getAppName()),
                                OpenApiCallLogEntity::getAppName, safeQueryBo.getAppName())
                        .like(StrUtils.isNotBlank(safeQueryBo.getRequestPath()),
                                OpenApiCallLogEntity::getRequestPath, safeQueryBo.getRequestPath())
                        .eq(safeQueryBo.getSuccess() != null,
                                OpenApiCallLogEntity::getSuccess, safeQueryBo.getSuccess())
                        .ge(safeQueryBo.getBegin() != null,
                                OpenApiCallLogEntity::getUtcCreate, safeQueryBo.getBegin())
                        .le(safeQueryBo.getEnd() != null,
                                OpenApiCallLogEntity::getUtcCreate, safeQueryBo.getEnd())
                        .orderByDesc(OpenApiCallLogEntity::getUtcCreate))
                .convert(this::toVo);
    }

    /**
     * 保存调用日志，失败时只记录告警，不影响接口主流程。
     *
     * @param request 请求对象
     * @param credential AppKey凭证
     * @param requestPath 请求路径
     * @param startTime 开始纳秒时间
     * @param statusCode 响应状态码
     * @param errorMsg 错误信息
     */
    public void save(HttpServletRequest request,
                     AppCredentialEntity credential,
                     String requestPath,
                     long startTime,
                     int statusCode,
                     String errorMsg) {
        try {
            OpenApiCallLogEntity entity = new OpenApiCallLogEntity();
            entity.setAppKey(limit(credential == null ? null : credential.getAppKey(), MAX_APP_KEY_LENGTH));
            entity.setAppName(limit(credential == null ? null : credential.getAppName(), MAX_APP_NAME_LENGTH));
            entity.setRequestPath(limit(requestPath, MAX_PATH_LENGTH));
            entity.setRequestMethod(limit(request.getMethod(), MAX_METHOD_LENGTH));
            entity.setStatusCode(statusCode);
            entity.setSuccess(statusCode >= 200 && statusCode < 400 && StrUtils.isBlank(errorMsg));
            entity.setSpendTime((System.nanoTime() - startTime) / 1_000_000L);
            entity.setIp(limit(resolveIp(request), MAX_IP_LENGTH));
            entity.setErrorMsg(limit(errorMsg, MAX_ERROR_LENGTH));
            entity.setUtcCreate(LocalDateTime.now());
            this.openApiCallLogMapper.insert(entity);
        } catch (Exception e) {
            log.warn("保存OpenAPI调用日志失败, path: {}", requestPath, e);
        }
    }

    private String resolveIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (StrUtils.isNotBlank(forwardedFor)) {
            return forwardedFor.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (StrUtils.isNotBlank(realIp)) {
            return realIp;
        }
        return request.getRemoteAddr();
    }

    private String limit(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private OpenApiCallLogVo toVo(OpenApiCallLogEntity entity) {
        if (entity == null) {
            return null;
        }
        OpenApiCallLogVo vo = new OpenApiCallLogVo();
        vo.setId(entity.getId());
        vo.setAppKey(entity.getAppKey());
        vo.setAppName(entity.getAppName());
        vo.setRequestPath(entity.getRequestPath());
        vo.setRequestMethod(entity.getRequestMethod());
        vo.setStatusCode(entity.getStatusCode());
        vo.setSuccess(entity.getSuccess());
        vo.setSpendTime(entity.getSpendTime());
        vo.setIp(entity.getIp());
        vo.setErrorMsg(entity.getErrorMsg());
        vo.setUtcCreate(entity.getUtcCreate());
        return vo;
    }
}
