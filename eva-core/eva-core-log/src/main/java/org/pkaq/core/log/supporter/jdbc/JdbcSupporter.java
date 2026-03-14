package org.pkaq.core.log.supporter.jdbc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pkaq.core.log.base.BizLogEntity;
import org.pkaq.core.log.base.LogSupporter;
import org.pkaq.core.log.condition.JdbcSupporterCondition;
import org.pkaq.core.mvc.bo.DateRangeBo;
import org.pkaq.core.mvc.vo.PageVo;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.pkaq.core.util.DatePatterns;
import org.pkaq.core.util.DateUtils;
import org.springframework.context.annotation.Conditional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 基于JDBC的日志持久化实现
 * 提供完整的增删查功能，支持分页、排序和租户隔离
 *
 * @author PKAQ
 */
@Slf4j
@Component
@Conditional(JdbcSupporterCondition.class)
@RequiredArgsConstructor
public class JdbcSupporter implements LogSupporter {

    private final JdbcTemplate jdbcTemplate;

    /** 租户过滤条件 */
    private static final String TENANT_FILTER = " AND tenant_id = ?";

    /** BizLogEntity 行映射器 */
    private static final RowMapper<BizLogEntity> ROW_MAPPER = (rs, rowNum) -> {
        BizLogEntity entity = new BizLogEntity();
        entity.setOperator(rs.getString("operator"));
        entity.setOperateType(rs.getString("operate_type"));
        entity.setOperateDatetime(rs.getString("operate_datetime"));
        entity.setSpendTime(rs.getString("spend_time"));
        entity.setDescription(rs.getString("description"));
        entity.setMCode(rs.getString("m_code"));
        entity.setBId(rs.getString("b_id"));
        entity.setClassName(rs.getString("class_name"));
        entity.setMethod(rs.getString("method"));
        entity.setParams(rs.getString("params"));
        entity.setResponse(rs.getString("response"));
        entity.setDevice(rs.getString("device"));
        entity.setVersion(rs.getString("version"));
        entity.setPostId(rs.getLong("post_id"));
        entity.setOrgId(rs.getLong("org_id"));
        entity.setCreateId(rs.getLong("create_id"));
        entity.setTenantId(rs.getLong("tenant_id"));
        return entity;
    };

    private static final String INSERT_SQL = """
            INSERT INTO log_biz (id, operator, operate_type, operate_datetime, spend_time,
            description, m_code, b_id, class_name, method, params, response, device, version,
            post_id, org_id, create_id, tenant_id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    @Override
    public void save(BizLogEntity entity) {
        long id = generateId();
        jdbcTemplate.update(INSERT_SQL, id,
                entity.getOperator(), entity.getOperateType(), entity.getOperateDatetime(),
                entity.getSpendTime(), entity.getDescription(), entity.getMCode(), entity.getBId(),
                entity.getClassName(), entity.getMethod(), entity.getParams(), entity.getResponse(),
                entity.getDevice(), entity.getVersion(),
                entity.getPostId(), entity.getOrgId(), entity.getCreateId(), entity.getTenantId());
    }

    @Override
    public BizLogEntity get(String id) {
        long tenantId = ThreadUserHelper.getTenantId();
        String sql = "SELECT * FROM log_biz WHERE id = ?" + TENANT_FILTER;
        List<BizLogEntity> result = jdbcTemplate.query(sql, ROW_MAPPER, Long.valueOf(id), tenantId);
        return result.isEmpty() ? null : result.getFirst();
    }

    /**
     * 分页查询日志列表
     * 按操作时间降序排列，支持日期范围过滤、分页和租户隔离
     *
     * @param dateRangeBo 日期范围
     * @param pageNo      页码
     * @param pageSize    每页条数
     * @return 分页结果
     */
    @Override
    public PageVo<BizLogEntity> list(DateRangeBo dateRangeBo, int pageNo, int pageSize) {
        long tenantId = ThreadUserHelper.getTenantId();
        String beginStr = DateUtils.format(dateRangeBo.getBegin(), DatePatterns.NORM_DATE_PATTERN);
        String endStr = DateUtils.format(dateRangeBo.getEnd(), DatePatterns.NORM_DATE_PATTERN);

        // 查询总数
        String countSql = "SELECT COUNT(*) FROM log_biz WHERE operate_datetime >= ? AND operate_datetime <= ?" + TENANT_FILTER;
        Long total = jdbcTemplate.queryForObject(countSql, Long.class, beginStr, endStr, tenantId);

        // 分页查询，按操作时间降序排列
        int offset = (pageNo - 1) * pageSize;
        String querySql = "SELECT * FROM log_biz WHERE operate_datetime >= ? AND operate_datetime <= ?" + TENANT_FILTER + " ORDER BY operate_datetime DESC LIMIT ? OFFSET ?";
        List<BizLogEntity> records = jdbcTemplate.query(querySql, ROW_MAPPER, beginStr, endStr, tenantId, pageSize, offset);

        PageVo<BizLogEntity> pageVo = new PageVo<>();
        pageVo.setRecords(records);
        pageVo.setTotal(null != total ? total : 0);
        pageVo.setSize(pageSize);
        pageVo.setCurrent(pageNo);
        return pageVo;
    }

    @Override
    public List<BizLogEntity> getLog() {
        long tenantId = ThreadUserHelper.getTenantId();
        String sql = "SELECT * FROM log_biz WHERE tenant_id = ? ORDER BY operate_datetime DESC";
        return jdbcTemplate.query(sql, ROW_MAPPER, tenantId);
    }

    @Override
    public List<BizLogEntity> getLogByType(String type) {
        long tenantId = ThreadUserHelper.getTenantId();
        String sql = "SELECT * FROM log_biz WHERE operate_type = ?" + TENANT_FILTER + " ORDER BY operate_datetime DESC";
        return jdbcTemplate.query(sql, ROW_MAPPER, type, tenantId);
    }

    @Override
    public List<BizLogEntity> getLogAfter(Date dateTime) {
        long tenantId = ThreadUserHelper.getTenantId();
        String dateStr = DateUtils.format(dateTime, DatePatterns.NORM_DATETIME_PATTERN);
        String sql = "SELECT * FROM log_biz WHERE operate_datetime >= ?" + TENANT_FILTER + " ORDER BY operate_datetime DESC";
        return jdbcTemplate.query(sql, ROW_MAPPER, dateStr, tenantId);
    }

    @Override
    public List<BizLogEntity> getLogBetween(Date begin, Date end) {
        long tenantId = ThreadUserHelper.getTenantId();
        String beginStr = DateUtils.format(begin, DatePatterns.NORM_DATETIME_PATTERN);
        String endStr = DateUtils.format(end, DatePatterns.NORM_DATETIME_PATTERN);
        String sql = "SELECT * FROM log_biz WHERE operate_datetime >= ? AND operate_datetime <= ?" + TENANT_FILTER + " ORDER BY operate_datetime DESC";
        return jdbcTemplate.query(sql, ROW_MAPPER, beginStr, endStr, tenantId);
    }

    @Override
    public void cleanAll() {
        long tenantId = ThreadUserHelper.getTenantId();
        jdbcTemplate.update("DELETE FROM log_biz WHERE tenant_id = ?", tenantId);
    }

    @Override
    public void cleanBefore(Date dateTime) {
        long tenantId = ThreadUserHelper.getTenantId();
        String dateStr = DateUtils.format(dateTime, DatePatterns.NORM_DATETIME_PATTERN);
        jdbcTemplate.update("DELETE FROM log_biz WHERE operate_datetime <= ?" + TENANT_FILTER, dateStr, tenantId);
    }

    @Override
    public void cleanBetween(Date begin, Date end) {
        long tenantId = ThreadUserHelper.getTenantId();
        String beginStr = DateUtils.format(begin, DatePatterns.NORM_DATETIME_PATTERN);
        String endStr = DateUtils.format(end, DatePatterns.NORM_DATETIME_PATTERN);
        jdbcTemplate.update("DELETE FROM log_biz WHERE operate_datetime >= ? AND operate_datetime <= ?" + TENANT_FILTER, beginStr, endStr, tenantId);
    }

    @Override
    public void print() {
        // JDBC模式不支持控制台打印
    }

    /**
     * 简单ID生成，生产环境建议使用MyBatis或MongoDB实现
     *
     * @return 生成的ID
     */
    private long generateId() {
        return Math.abs(UUID.randomUUID().getMostSignificantBits());
    }
}
