package org.pkaq.core.auth.log.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.pkaq.core.util.StrUtils;
import org.pkaq.web.core.client.ClientInfo;
import org.pkaq.web.core.client.ClientInfoResolver;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    public static final String LOGIN_TENANT_ID_ATTRIBUTE = "LOGIN_TENANT_ID";

    private static final String LOGIN_TYPE_PASSWORD = "PASSWORD";
    private static final String LOGIN_TYPE_LOGOUT = "LOGOUT";

    private static final int MAX_ACCOUNT_LENGTH = 100;

    private static final int MAX_FAIL_REASON_LENGTH = 400;

    private static final int MAX_IP_LENGTH = 64;

    private static final int MAX_USER_AGENT_LENGTH = 500;

    private static final int MAX_DEVICE_LENGTH = 64;

    private static final int MAX_MODEL_LENGTH = 128;

    private static final int MAX_RISK_FLAGS_LENGTH = 200;

    private final LoginLogMapper loginLogMapper;

    private final EvaConfig evaConfig;

    private final ClientInfoResolver clientInfoResolver;

    /**
     * 根据ID获取登录日志详情
     *
     * @param id 登录日志ID
     * @return 登录日志详情
     */
    public LoginLogVo get(Long id) {
        var wrapper = Wrappers.<LoginLogEntity>lambdaQuery()
                .eq(LoginLogEntity::getId, id);
        applyTenantFilter(wrapper, null);
        return toVo(this.loginLogMapper.selectOne(wrapper));
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
        var wrapper = Wrappers.<LoginLogEntity>lambdaQuery()
                        .like(StrUtils.isNotBlank(safeQueryBo.getAccount()),
                                LoginLogEntity::getAccount, safeQueryBo.getAccount())
                        .eq(safeQueryBo.getSuccess() != null, LoginLogEntity::getSuccess, safeQueryBo.getSuccess())
                        .eq(StrUtils.isNotBlank(safeQueryBo.getSessionId()),
                                LoginLogEntity::getSessionId, safeQueryBo.getSessionId())
                        .eq(StrUtils.isNotBlank(safeQueryBo.getIp()), LoginLogEntity::getIp, safeQueryBo.getIp())
                        .like(StrUtils.isNotBlank(safeQueryBo.getRiskFlag()),
                                LoginLogEntity::getRiskFlags, safeQueryBo.getRiskFlag())
                        .ge(safeQueryBo.getBegin() != null, LoginLogEntity::getUtcCreate, safeQueryBo.getBegin())
                        .le(safeQueryBo.getEnd() != null, LoginLogEntity::getUtcCreate, safeQueryBo.getEnd());
        applyTenantFilter(wrapper, safeQueryBo.getTargetTenantId());
        wrapper.orderByDesc(LoginLogEntity::getUtcCreate);
        return this.loginLogMapper.selectPage(page, wrapper)
                .convert(this::toVo);
    }

    /**
     * 保存登录成功日志
     *
     * @param request 请求对象
     * @param user 登录用户
     */
    public void saveSuccess(HttpServletRequest request, JwtUserDetail user, String sessionId) {
        LoginLogEntity entity = buildBaseLog(request);
        entity.setTenantId(user.getTenantId());
        entity.setUserId(user.getId());
        entity.setAccount(limit(user.getAccount(), MAX_ACCOUNT_LENGTH));
        entity.setSessionId(limit(sessionId, MAX_DEVICE_LENGTH));
        entity.setSuccess(Boolean.TRUE);
        try {
            entity.setRiskFlags(limit(resolveRiskFlags(entity), MAX_RISK_FLAGS_LENGTH));
        } catch (Exception exception) {
            log.warn("识别登录风险失败, tenantId: {}, userId: {}", user.getTenantId(), user.getId(), exception);
        }
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
        entity.setTenantId(resolveTenantId(request));
        entity.setAccount(limit(resolveAccount(request), MAX_ACCOUNT_LENGTH));
        entity.setSuccess(Boolean.FALSE);
        entity.setFailReason(limit(failReason, MAX_FAIL_REASON_LENGTH));
        save(entity);
    }

    /**
     * 保存主动退出日志。
     *
     * @param request 请求对象
     */
    public void saveLogout(HttpServletRequest request, String sessionId, String logoutReason) {
        ThreadUser currentUser = ThreadUserHelper.getCurrentUserOrNull();
        if (currentUser == null) {
            return;
        }
        saveLogout(request, currentUser.getTenantId(), currentUser.getUserId(), currentUser.getAccount(),
                sessionId, logoutReason);
    }

    /**
     * 保存带可信Token身份的主动退出日志。
     */
    public void saveLogout(HttpServletRequest request,
                           Long tenantId,
                           Long userId,
                           String account,
                           String sessionId,
                           String logoutReason) {
        if (tenantId == null || userId == null || userId <= 0L) {
            return;
        }
        closeSession(tenantId, userId, sessionId, logoutReason);
        LoginLogEntity entity = buildBaseLog(request);
        entity.setTenantId(tenantId);
        entity.setUserId(userId);
        entity.setAccount(limit(account, MAX_ACCOUNT_LENGTH));
        entity.setSessionId(limit(sessionId, MAX_DEVICE_LENGTH));
        entity.setLoginType(LOGIN_TYPE_LOGOUT);
        entity.setSuccess(Boolean.TRUE);
        entity.setLogoutAt(LocalDateTime.now());
        entity.setLogoutReason(limit(logoutReason, MAX_DEVICE_LENGTH));
        save(entity);
    }

    /**
     * 兼容未提供会话标识的注销调用。
     */
    public void saveLogout(HttpServletRequest request) {
        saveLogout(request, null, "USER_LOGOUT");
    }

    /**
     * 关闭指定登录会话。
     */
    public void closeSession(Long tenantId, Long userId, String sessionId, String logoutReason) {
        if (tenantId == null || userId == null || StrUtils.isBlank(sessionId)) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        var wrapper = Wrappers.<LoginLogEntity>lambdaUpdate()
                .eq(LoginLogEntity::getTenantId, tenantId)
                .eq(LoginLogEntity::getUserId, userId)
                .eq(LoginLogEntity::getSessionId, sessionId)
                .eq(LoginLogEntity::getLoginType, LOGIN_TYPE_PASSWORD)
                .isNull(LoginLogEntity::getLogoutAt)
                .set(LoginLogEntity::getLastActiveAt, now)
                .set(LoginLogEntity::getLogoutAt, now)
                .set(LoginLogEntity::getLogoutReason, limit(logoutReason, MAX_DEVICE_LENGTH));
        try {
            loginLogMapper.update(null, wrapper);
        } catch (Exception exception) {
            log.warn("更新登录会话退出状态失败, tenantId: {}, userId: {}, sessionId: {}",
                    tenantId, userId, sessionId, exception);
        }
    }

    /**
     * refresh token轮换后更新登录日志中的会话标识。
     */
    public void rotateSession(Long tenantId, Long userId, String oldSessionId, String newSessionId) {
        if (tenantId == null || userId == null || StrUtils.isBlank(oldSessionId)
                || StrUtils.isBlank(newSessionId)) {
            return;
        }
        var wrapper = Wrappers.<LoginLogEntity>lambdaUpdate()
                .eq(LoginLogEntity::getTenantId, tenantId)
                .eq(LoginLogEntity::getUserId, userId)
                .eq(LoginLogEntity::getSessionId, oldSessionId)
                .eq(LoginLogEntity::getLoginType, LOGIN_TYPE_PASSWORD)
                .isNull(LoginLogEntity::getLogoutAt)
                .set(LoginLogEntity::getSessionId, newSessionId)
                .set(LoginLogEntity::getLastActiveAt, LocalDateTime.now());
        try {
            loginLogMapper.update(null, wrapper);
        } catch (Exception exception) {
            log.warn("更新登录会话轮换标识失败, tenantId: {}, userId: {}", tenantId, userId, exception);
        }
    }

    /**
     * 关闭用户全部活动会话。
     */
    public void closeUserSessions(Long tenantId, Long userId, String logoutReason) {
        if (tenantId == null || userId == null) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        var wrapper = Wrappers.<LoginLogEntity>lambdaUpdate()
                .eq(LoginLogEntity::getTenantId, tenantId)
                .eq(LoginLogEntity::getUserId, userId)
                .eq(LoginLogEntity::getLoginType, LOGIN_TYPE_PASSWORD)
                .isNull(LoginLogEntity::getLogoutAt)
                .set(LoginLogEntity::getLastActiveAt, now)
                .set(LoginLogEntity::getLogoutAt, now)
                .set(LoginLogEntity::getLogoutReason, limit(logoutReason, MAX_DEVICE_LENGTH));
        try {
            loginLogMapper.update(null, wrapper);
        } catch (Exception exception) {
            log.warn("关闭用户全部登录会话失败, tenantId: {}, userId: {}", tenantId, userId, exception);
        }
    }

    private void save(LoginLogEntity entity) {
        try {
            this.loginLogMapper.insert(entity);
        } catch (Exception e) {
            log.warn("保存登录日志失败, account: {}, success: {}", entity.getAccount(), entity.getSuccess(), e);
        }
    }

    private void applyTenantFilter(LambdaQueryWrapper<LoginLogEntity> wrapper,
                                   Long targetTenantId) {
        if (this.evaConfig.isStandaloneMode()) {
            wrapper.eq(LoginLogEntity::getTenantId, 0L);
            return;
        }

        ThreadUser currentUser = ThreadUserHelper.getCurrentUserOrNull();
        if (this.evaConfig.isPlatformMode() && currentUser != null && ThreadUserHelper.isAdmin()) {
            if (targetTenantId != null) {
                if (targetTenantId < 0L) {
                    throw new IllegalArgumentException("目标租户ID不能为负数");
                }
                wrapper.eq(LoginLogEntity::getTenantId, targetTenantId);
            }
            return;
        }

        long tenantId = currentUser == null ? -1L : currentUser.getTenantId();
        wrapper.eq(LoginLogEntity::getTenantId, tenantId);
    }

    private LoginLogEntity buildBaseLog(HttpServletRequest request) {
        ClientInfo clientInfo = clientInfoResolver.resolve(request);
        LoginLogEntity entity = new LoginLogEntity();
        entity.setLoginType(LOGIN_TYPE_PASSWORD);
        entity.setIp(limit(clientInfo.ip(), MAX_IP_LENGTH));
        entity.setUserAgent(limit(clientInfo.userAgent(), MAX_USER_AGENT_LENGTH));
        entity.setDevice(limit(clientInfo.deviceType(), MAX_DEVICE_LENGTH));
        entity.setVersion(limit(clientInfo.clientVersion(), MAX_DEVICE_LENGTH));
        entity.setDeviceType(limit(clientInfo.deviceType(), MAX_DEVICE_LENGTH));
        entity.setDeviceModel(limit(clientInfo.deviceModel(), MAX_MODEL_LENGTH));
        entity.setOsName(limit(clientInfo.osName(), MAX_DEVICE_LENGTH));
        entity.setOsVersion(limit(clientInfo.osVersion(), MAX_DEVICE_LENGTH));
        entity.setBrowserName(limit(clientInfo.browserName(), MAX_DEVICE_LENGTH));
        entity.setBrowserVersion(limit(clientInfo.browserVersion(), MAX_DEVICE_LENGTH));
        entity.setDeviceFingerprint(clientInfo.fingerprint());
        entity.setLastActiveAt(LocalDateTime.now());
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

    private Long resolveTenantId(HttpServletRequest request) {
        Object tenantId = request.getAttribute(LOGIN_TENANT_ID_ATTRIBUTE);
        if (tenantId instanceof Long value && value >= 0L) {
            return value;
        }
        return 0L;
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
        vo.setSessionId(entity.getSessionId());
        vo.setDeviceType(entity.getDeviceType());
        vo.setDeviceModel(entity.getDeviceModel());
        vo.setOsName(entity.getOsName());
        vo.setOsVersion(entity.getOsVersion());
        vo.setBrowserName(entity.getBrowserName());
        vo.setBrowserVersion(entity.getBrowserVersion());
        vo.setDeviceFingerprint(entity.getDeviceFingerprint());
        vo.setRiskFlags(entity.getRiskFlags());
        vo.setLastActiveAt(entity.getLastActiveAt());
        vo.setLogoutAt(entity.getLogoutAt());
        vo.setLogoutReason(entity.getLogoutReason());
        vo.setUtcCreate(entity.getUtcCreate());
        return vo;
    }

    private String resolveRiskFlags(LoginLogEntity entity) {
        List<String> riskFlags = new ArrayList<>();
        if ("UNKNOWN".equals(entity.getUserAgent())) {
            riskFlags.add("UNKNOWN_CLIENT");
        }
        if (entity.getUserId() == null) {
            return String.join(",", riskFlags);
        }
        Long knownDeviceCount = loginLogMapper.selectCount(Wrappers.<LoginLogEntity>lambdaQuery()
                .eq(LoginLogEntity::getTenantId, entity.getTenantId())
                .eq(LoginLogEntity::getUserId, entity.getUserId())
                .eq(LoginLogEntity::getSuccess, Boolean.TRUE)
                .eq(LoginLogEntity::getDeviceFingerprint, entity.getDeviceFingerprint()));
        if (knownDeviceCount == null || 0L == knownDeviceCount) {
            riskFlags.add("NEW_DEVICE");
        }
        LoginLogEntity lastLogin = loginLogMapper.selectOne(Wrappers.<LoginLogEntity>lambdaQuery()
                .eq(LoginLogEntity::getTenantId, entity.getTenantId())
                .eq(LoginLogEntity::getUserId, entity.getUserId())
                .eq(LoginLogEntity::getSuccess, Boolean.TRUE)
                .eq(LoginLogEntity::getLoginType, LOGIN_TYPE_PASSWORD)
                .orderByDesc(LoginLogEntity::getUtcCreate)
                .last("LIMIT 1"));
        if (lastLogin != null && !Objects.equals(lastLogin.getIp(), entity.getIp())) {
            riskFlags.add("IP_CHANGED");
        }
        return String.join(",", riskFlags);
    }
}
