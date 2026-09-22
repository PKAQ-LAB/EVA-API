package org.pkaq.core.mybatis.log;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pkaq.core.log.base.BizLogEntity;
import org.pkaq.core.log.base.BusinessLogRepository;
import org.pkaq.core.log.bo.LogQueryBo;
import org.pkaq.core.mvc.vo.PageVo;
import org.pkaq.core.mybatis.log.entity.MybatisBizLogEntity;
import org.pkaq.core.mybatis.log.mapper.BusinessLogMapper;
import org.pkaq.core.util.BeanUtils;
import org.pkaq.core.util.DatePatterns;
import org.pkaq.core.util.DateUtils;
import org.pkaq.core.util.json.JsonUtil;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.pkaq.core.properties.EvaConfig;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * PostgreSQL 业务日志存储实现。
 *
 * @author PKAQ
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PostgresBusinessLogRepository implements BusinessLogRepository {

    private final BusinessLogMapper businessLogMapper;
    private final EvaConfig evaConfig;

    @Override
    public void save(BizLogEntity bizLogEntity) {
        if (bizLogEntity == null) {
            return;
        }
        try {
            MybatisBizLogEntity mybatisBizLogEntity = new MybatisBizLogEntity();
            BeanUtils.copyProperties(bizLogEntity, mybatisBizLogEntity);
            this.businessLogMapper.insert(mybatisBizLogEntity);
        } catch (Exception exception) {
            // 审计日志写入失败不得破坏主业务，异常仍写入应用日志供监控告警。
            log.error("保存业务日志失败, tenantId: {}, userId: {}",
                    bizLogEntity.getTenantId(), bizLogEntity.getUserId(), exception);
        }
    }

    @Override
    public String get(String id) {
        try {
            MybatisBizLogEntity entity = this.businessLogMapper.selectLogById(
                    Long.valueOf(id), currentTenantId(null));
            return null != entity ? JsonUtil.toJson(entity) : "";
        } catch (NumberFormatException exception) {
            return "";
        }
    }

    @Override
    public Object list(LogQueryBo queryBo) {
        LogQueryBo safeQuery = queryBo == null ? new LogQueryBo() : queryBo;
        Date begin = safeQuery.getBegin();
        Date end = safeQuery.getEnd();
        if (null == begin) {
            begin = DateUtils.addDay(new Date(), -7);
        }
        if (null == end) {
            end = new Date();
        }

        String beginText = DateUtils.format(begin, DatePatterns.NORM_DATETIME_PATTERN);
        String endText = DateUtils.format(end, DatePatterns.NORM_DATETIME_PATTERN);
        boolean includeArchived = !Boolean.FALSE.equals(safeQuery.getIncludeArchived());
        long pageNo = safeQuery.getPageNo();
        long pageSize = safeQuery.getPageSize();
        long offset = (pageNo - 1) * pageSize;
        Long tenantId = currentTenantId(safeQuery.getTargetTenantId());

        long total = this.businessLogMapper.countLogs(tenantId, beginText, endText,
                safeQuery.getOperator(), safeQuery.getOperateType(), safeQuery.getMCode(),
                safeQuery.getBId(), includeArchived);
        List<MybatisBizLogEntity> records = this.businessLogMapper.selectLogPage(
                tenantId, beginText, endText, safeQuery.getOperator(), safeQuery.getOperateType(),
                safeQuery.getMCode(), safeQuery.getBId(), includeArchived, offset, pageSize);

        PageVo<MybatisBizLogEntity> page = new PageVo<>();
        page.setRecords(records);
        page.setTotal(total);
        page.setCurrent(pageNo);
        page.setSize(pageSize);
        return page;
    }

    private Long currentTenantId(Long targetTenantId) {
        if (evaConfig.isStandaloneMode()) {
            return 0L;
        }
        ThreadUser currentUser = ThreadUserHelper.getCurrentUserOrNull();
        if (evaConfig.isPlatformMode() && currentUser != null && ThreadUserHelper.isAdmin()) {
            if (targetTenantId != null && targetTenantId < 0L) {
                throw new IllegalArgumentException("目标租户ID不能为负数");
            }
            return targetTenantId;
        }
        return currentUser == null ? -1L : currentUser.getTenantId();
    }
}
