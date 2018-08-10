package org.pkaq.config;

import cn.hutool.core.date.DateUtil;
import io.shardingsphere.core.api.algorithm.sharding.PreciseShardingValue;
import io.shardingsphere.core.api.algorithm.sharding.standard.PreciseShardingAlgorithm;

import java.util.Collection;
import java.util.Date;

/**
 * 精准分片策略类
 * @author: S.PKAQ
 * @Datetime: 2018/8/10 0:15
 */
public class MonthTableShardingAlgorithm implements PreciseShardingAlgorithm<Date> {

    /**
     * 等值查询分表路由策略, 根据传入date返回响应以年月结尾的表
     * @param availableTargetNames 可用表名
     * @param shardingValue 分片条件
     * @return 符合分片条件的表名
     */
    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<Date> shardingValue) {

        String tableExt = DateUtil.format(shardingValue.getValue(), "yyyy_MM");

        for (String availableTableName : availableTargetNames) {

            System.out.println("------------->");
            System.out.println(tableExt);
            System.out.println(availableTableName);

            if (availableTableName.endsWith(tableExt)) {
                return availableTableName;
            }
        }
        return null;
    }
}
