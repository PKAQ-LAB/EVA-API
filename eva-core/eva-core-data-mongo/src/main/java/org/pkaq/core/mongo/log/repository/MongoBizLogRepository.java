package org.pkaq.core.mongo.log.repository;

import org.pkaq.core.mongo.log.entity.MongoBizLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

/**
 * MongoDB业务日志Repository
 * 所有查询方法均支持租户隔离
 *
 * @author PKAQ
 * @date 2026-03-10
 */
public interface MongoBizLogRepository extends MongoRepository<MongoBizLogEntity, String> {

    /**
     * 根据ID和租户查询
     *
     * @param id       日志ID
     * @param tenantId 租户ID
     * @return 日志实体
     */
    Optional<MongoBizLogEntity> findByIdAndTenantId(String id, long tenantId);

    /**
     * 根据租户查询所有日志
     *
     * @param tenantId 租户ID
     * @param pageable 分页排序参数
     * @return 分页结果
     */
    Page<MongoBizLogEntity> findByTenantId(long tenantId, Pageable pageable);

    /**
     * 根据租户和操作类型查询日志
     *
     * @param tenantId    租户ID
     * @param operateType 操作类型
     * @return 日志集合
     */
    List<MongoBizLogEntity> findByTenantIdAndOperateType(long tenantId, String operateType);

    /**
     * 根据租户和操作时间区间分页查询
     *
     * @param tenantId 租户ID
     * @param begin    开始时间
     * @param end      结束时间
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<MongoBizLogEntity> findByTenantIdAndOperateDatetimeBetween(long tenantId, String begin, String end, Pageable pageable);

    /**
     * 根据租户和操作时间区间查询
     *
     * @param tenantId 租户ID
     * @param begin    开始时间
     * @param end      结束时间
     * @return 日志集合
     */
    List<MongoBizLogEntity> findByTenantIdAndOperateDatetimeBetween(long tenantId, String begin, String end);

    /**
     * 根据租户查询某个时间之后的日志
     *
     * @param tenantId 租户ID
     * @param dateTime 时间点
     * @return 日志集合
     */
    List<MongoBizLogEntity> findByTenantIdAndOperateDatetimeGreaterThanEqual(long tenantId, String dateTime);

    /**
     * 根据租户删除某个时间之前的日志
     *
     * @param tenantId 租户ID
     * @param dateTime 时间点
     */
    void deleteByTenantIdAndOperateDatetimeLessThanEqual(long tenantId, String dateTime);

    /**
     * 根据租户删除某个时间区间的日志
     *
     * @param tenantId 租户ID
     * @param begin    开始时间
     * @param end      结束时间
     */
    void deleteByTenantIdAndOperateDatetimeBetween(long tenantId, String begin, String end);

    /**
     * 根据租户删除所有日志
     *
     * @param tenantId 租户ID
     */
    void deleteByTenantId(long tenantId);
}
