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
    public static final String LOGIN_TENANT_ID_ATTRIBUTE = "LOGIN_TENANT_ID";

    private static final String LOGIN_TYPE_PASSWORD = "PASSWORD";
    private static final String LOGIN_TYPE_LOGOUT = "LOGOUT";

    private static final int MAX_ACCOUNT_LENGTH = 100;

    private static final int MAX_FAIL_REASON_LENGTH = 400;

    private static final int MAX_IP_LENGTH = 64;

    private static final int MAX_USER_AGENT_LENGTH = 500;

    private static final int MAX_DEVICE_LENGTH = 64;

    private final LoginLogMapper loginLogMapper;

    private final EvaConfig evaConfig;

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
    public void saveLogout(HttpServletRequest request) {
        ThreadUser currentUser = ThreadUserHelper.getCurrentUserOrNull();
        if (currentUser == null) {
            return;
        }
        LoginLogEntity entity = buildBaseLog(request);
        entity.setTenantId(currentUser.getTenantId());
        entity.setUserId(currentUser.getUserId());
        entity.setAccount(limit(currentUser.getAccount(), MAX_ACCOUNT_LENGTH));
        entity.setLoginType(LOGIN_TYPE_LOGOUT);
        entity.setSuccess(Boolean.TRUE);
        save(entity);
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

    private Long resolveTenantId(HttpServletRequest request) {
        Object tenantId = request.getAttribute(LOGIN_TENANT_ID_ATTRIBUTE);
        if (tenantId instanceof Long value && value >= 0L) {
            return value;
        }
        return 0L;
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
