package org.pkaq.core.auth.adapter.mybatis.audit.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pkaq.core.auth.audit.consts.LoginAuditConsts;
import org.pkaq.core.auth.spi.ILoginAudit;
import org.pkaq.core.auth.audit.bo.LoginLogQueryBo;
import org.pkaq.core.auth.adapter.mybatis.audit.entity.LoginLogEntity;
import org.pkaq.core.auth.adapter.mybatis.audit.mapper.LoginLogMapper;
import org.pkaq.core.auth.audit.vo.LoginLogVo;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.tenant.TenantLogContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class LoginLogService implements ILoginAudit {
    /** 登录文件只保留技术标识和结果，不输出密码、令牌或设备指纹。 */
    private static final Logger FILE_LOG = LoggerFactory.getLogger("eva.audit.login");

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

    private final ClientInfoResolver clientInfoResolver;

    private final LoginLogStorage storage;

    private final LoginLogReadAccess readAccess;

    /**
     * 根据ID获取登录日志详情
     *
     * @param id 登录日志ID
     * @return 登录日志详情
     */
    public LoginLogVo get(Long id) {
        return readAccess.read("/monitor/log/login/get/" + id, null,
                () -> toVo(loginLogMapper.selectHistoryDetail(id)));
    }

    /**
     * 分页查询登录日志
     *
     * @param queryBo 查询参数
     * @return 登录日志分页列表
     */
    public IPage<LoginLogVo> list(LoginLogQueryBo queryBo) {
        LoginLogQueryBo safeQueryBo = queryBo == null ? new LoginLogQueryBo() : queryBo;
        return readAccess.read("/monitor/log/login/list", safeQueryBo.getTargetTenantId(),
                () -> loginLogMapper.selectHistoryPage(
                        new Page<>(safeQueryBo.getPageNo(), safeQueryBo.getPageSize()), safeQueryBo).convert(this::toVo));
    }

    /**
     * 保存登录成功日志
     *
     * @param request 请求对象
     * @param tenantId 可信租户标识
     * @param userId 登录用户标识
     * @param account 登录账号
     * @param sessionId 登录会话标识
     */
    @Override
    public void saveSuccess(HttpServletRequest request, Long tenantId, Long userId, String account, String sessionId) {
        LoginLogEntity entity = buildBaseLog(request);
        entity.setTenantId(tenantId);
        entity.setUserId(userId);
        entity.setAccount(limit(account, MAX_ACCOUNT_LENGTH));
        entity.setSessionId(limit(sessionId, MAX_DEVICE_LENGTH));
        entity.setSuccess(Boolean.TRUE);
        TenantLogContext.runWithTenant(tenantId,
                () -> FILE_LOG.info("login success tenantId={} userId={}", tenantId, userId));
        write(() -> {
            entity.setRiskFlags(limit(resolveRiskFlags(entity), MAX_RISK_FLAGS_LENGTH));
            save(entity);
        });
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
        TenantLogContext.runWithTenant(entity.getTenantId(),
                () -> FILE_LOG.info("login failure tenantId={}", entity.getTenantId()));
        write(() -> save(entity));
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
        LoginLogEntity entity = buildBaseLog(request);
        entity.setTenantId(tenantId);
        entity.setUserId(userId);
        entity.setAccount(limit(account, MAX_ACCOUNT_LENGTH));
        entity.setSessionId(limit(sessionId, MAX_DEVICE_LENGTH));
        entity.setLoginType(LOGIN_TYPE_LOGOUT);
        entity.setSuccess(Boolean.TRUE);
        entity.setLogoutAt(LocalDateTime.now());
        entity.setLogoutReason(limit(logoutReason, MAX_DEVICE_LENGTH));
        TenantLogContext.runWithTenant(tenantId,
                () -> FILE_LOG.info("logout tenantId={} userId={}", tenantId, userId));
        write(() -> {
            doCloseSession(tenantId, userId, sessionId, logoutReason);
            save(entity);
        });
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
        write(() -> doCloseSession(tenantId, userId, sessionId, logoutReason));
    }

    private void doCloseSession(Long tenantId, Long userId, String sessionId, String logoutReason) {
        if (StrUtils.isBlank(sessionId)) {
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
        loginLogMapper.update(null, wrapper);
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
        write(() -> loginLogMapper.update(null, wrapper));
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
        write(() -> loginLogMapper.update(null, wrapper));
    }

    private void save(LoginLogEntity entity) {
        this.loginLogMapper.insert(entity);
    }

    private void write(Runnable action) {
        try {
            // 必须在事务代理外捕获，包含提交阶段异常，避免审计失败影响认证或业务事务。
            storage.write(action);
        } catch (Exception exception) {
            log.warn("登录审计独立事务失败", exception);
        }
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
        Object account = request.getAttribute(LoginAuditConsts.LOGIN_ACCOUNT_ATTRIBUTE);
        if (account instanceof String accountValue && StrUtils.isNotBlank(accountValue)) {
            return accountValue;
        }
        return null;
    }

    private Long resolveTenantId(HttpServletRequest request) {
        Object tenantId = request.getAttribute(LoginAuditConsts.LOGIN_TENANT_ID_ATTRIBUTE);
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
        Long knownDeviceCount = loginLogMapper.selectRiskDeviceCount(
                entity.getTenantId(), entity.getUserId(), entity.getDeviceFingerprint());
        if (knownDeviceCount == null || 0L == knownDeviceCount) {
            riskFlags.add("NEW_DEVICE");
        }
        LoginLogEntity lastLogin = loginLogMapper.selectRiskLastLogin(entity.getTenantId(), entity.getUserId());
        if (lastLogin != null && !Objects.equals(lastLogin.getIp(), entity.getIp())) {
            riskFlags.add("IP_CHANGED");
        }
        return String.join(",", riskFlags);
    }
}
