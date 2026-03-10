package org.pkaq.core.mongo.log.repository;

import org.pkaq.core.mongo.log.entity.MongoBizLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 业务日志 MongoDB Repository
 *
 * @author PKAQ
 * @date 2026-03-10
 */
@Repository
public interface MongoBizLogRepository extends MongoRepository<MongoBizLogEntity, String> {

    /**
     * 根据操作类型查询
     */
    List<MongoBizLogEntity> findByOperateType(String operateType);

    /**
     * 根据操作时间区间查询（分页）
     */
    Page<MongoBizLogEntity> findByOperateDatetimeBetweenOrderByOperateDatetimeDesc(
            String begin, String end, Pageable pageable);

    /**
     * 根据操作时间区间查询
     */
    List<MongoBizLogEntity> findByOperateDatetimeBetween(String begin, String end);

    /**
     * 查询某个时间之后的日志
     */
    List<MongoBizLogEntity> findByOperateDatetimeGreaterThanEqual(String dateTime);

    /**
     * 删除某个时间之前的日志
     */
    void deleteByOperateDatetimeLessThanEqual(String dateTime);

    /**
     * 删除某个时间区间的日志
     */
    void deleteByOperateDatetimeBetween(String begin, String end);
}
