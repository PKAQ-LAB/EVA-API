package org.pkaq.core.auth.log.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pkaq.core.auth.domain.JwtUserDetail;
import org.pkaq.core.auth.log.bo.LoginLogQueryBo;
import org.pkaq.core.auth.log.entity.LoginLogEntity;
import org.pkaq.core.auth.log.mapper.LoginLogMapper;
import org.pkaq.core.auth.log.vo.LoginLogVo;
import org.pkaq.core.util.StrUtils;
import org.pkaq.web.core.utils.RequestUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 登录日志服务
 *
 * @author PKAQ
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LoginLogService {

    public static final String LOGIN_ACCOUNT_ATTRIBUTE = "LOGIN_ACCOUNT";

    private static final String LOGIN_TYPE_PASSWORD = "PASSWORD";

    private static final int MAX_ACCOUNT_LENGTH = 100;

    private static final int MAX_FAIL_REASON_LENGTH = 400;

    private static final int MAX_IP_LENGTH = 64;

    private static final int MAX_USER_AGENT_LENGTH = 500;

    private static final int MAX_DEVICE_LENGTH = 64;

    private final LoginLogMapper loginLogMapper;

    /**
     * 根据ID获取登录日志详情
     *
     * @param id 登录日志ID
     * @return 登录日志详情
     */
    public LoginLogVo get(Long id) {
        return toVo(this.loginLogMapper.selectById(id));
    }

    /**
     * 分页查询登录日志
     *
     * @param queryBo 查询参数
     * @return 登录日志分页列表
     */
    public IPage<LoginLogVo> list(LoginLogQueryBo queryBo) {
        LoginLogQueryBo safeQueryBo = queryBo == null ? new LoginLogQueryBo() : queryBo;
        Page<LoginLogEntity> page = new Page<>(safeQueryBo.getPageNo(), safeQueryBo.getPageSize());
        return this.loginLogMapper.selectPage(page, Wrappers.<LoginLogEntity>lambdaQuery()
                        .like(StrUtils.isNotBlank(safeQueryBo.getAccount()),
                                LoginLogEntity::getAccount, safeQueryBo.getAccount())
                        .eq(safeQueryBo.getSuccess() != null, LoginLogEntity::getSuccess, safeQueryBo.getSuccess())
                        .ge(safeQueryBo.getBegin() != null, LoginLogEntity::getUtcCreate, safeQueryBo.getBegin())
                        .le(safeQueryBo.getEnd() != null, LoginLogEntity::getUtcCreate, safeQueryBo.getEnd())
                        .orderByDesc(LoginLogEntity::getUtcCreate))
                .convert(this::toVo);
    }

    /**
     * 保存登录成功日志
     *
     * @param request 请求对象
     * @param user 登录用户
     */
    public void saveSuccess(HttpServletRequest request, JwtUserDetail user) {
        LoginLogEntity entity = buildBaseLog(request);
        entity.setTenantId(user.getTenantId());
        entity.setUserId(user.getId());
        entity.setAccount(limit(user.getAccount(), MAX_ACCOUNT_LENGTH));
        entity.setSuccess(Boolean.TRUE);
        save(entity);
    }

    /**
     * 保存登录失败日志
     *
     * @param request 请求对象
     * @param failReason 失败原因
     */
    public void saveFailure(HttpServletRequest request, String failReason) {
        LoginLogEntity entity = buildBaseLog(request);
        entity.setAccount(limit(resolveAccount(request), MAX_ACCOUNT_LENGTH));
        entity.setSuccess(Boolean.FALSE);
        entity.setFailReason(limit(failReason, MAX_FAIL_REASON_LENGTH));
        save(entity);
    }

    private void save(LoginLogEntity entity) {
        try {
            this.loginLogMapper.insert(entity);
        } catch (Exception e) {
            log.warn("保存登录日志失败, account: {}, success: {}", entity.getAccount(), entity.getSuccess(), e);
        }
    }

    private LoginLogEntity buildBaseLog(HttpServletRequest request) {
        LoginLogEntity entity = new LoginLogEntity();
        entity.setLoginType(LOGIN_TYPE_PASSWORD);
        entity.setIp(limit(resolveIp(request), MAX_IP_LENGTH));
        entity.setUserAgent(limit(request.getHeader("User-Agent"), MAX_USER_AGENT_LENGTH));
        entity.setDevice(limit(RequestUtil.getDeivce(request), MAX_DEVICE_LENGTH));
        entity.setVersion(limit(RequestUtil.getVersion(request), MAX_DEVICE_LENGTH));
        entity.setUtcCreate(LocalDateTime.now());
        return entity;
    }

    private String resolveAccount(HttpServletRequest request) {
        Object account = request.getAttribute(LOGIN_ACCOUNT_ATTRIBUTE);
        if (account instanceof String accountValue && StrUtils.isNotBlank(accountValue)) {
            return accountValue;
        }
        return null;
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

    private LoginLogVo toVo(LoginLogEntity entity) {
        if (entity == null) {
            return null;
        }
        LoginLogVo vo = new LoginLogVo();
        vo.setId(entity.getId());
        vo.setTenantId(entity.getTenantId());
        vo.setUserId(entity.getUserId());
        vo.setAccount(entity.getAccount());
        vo.setLoginType(entity.getLoginType());
        vo.setSuccess(entity.getSuccess());
        vo.setFailReason(entity.getFailReason());
        vo.setIp(entity.getIp());
        vo.setUserAgent(entity.getUserAgent());
        vo.setDevice(entity.getDevice());
        vo.setVersion(entity.getVersion());
        vo.setUtcCreate(entity.getUtcCreate());
        return vo;
    }
}
