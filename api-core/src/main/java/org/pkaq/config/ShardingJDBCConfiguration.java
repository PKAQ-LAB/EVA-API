package org.pkaq.config;

import com.baomidou.mybatisplus.spring.MybatisSqlSessionFactoryBean;
import io.shardingsphere.core.api.ShardingDataSourceFactory;
import io.shardingsphere.core.api.config.ShardingRuleConfiguration;
import io.shardingsphere.core.api.config.TableRuleConfiguration;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * shardingjdbc 配置类
 * Datetime: 2016-11-25 11:44
 * @author PKAQ
 */

@Slf4j
@Configuration
@AutoConfigureAfter(DataSource.class)
public class ShardingJDBCConfiguration {

    private final DataSource dataSource;

    private DataSource shardingDataSource;

    @Autowired
    public ShardingJDBCConfiguration(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void shardingDataSource() throws SQLException {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        //分表策略
        shardingRuleConfig.getTableRuleConfigs().add(getOrderTableRuleConfiguration());
        //绑定表规则列表
        shardingRuleConfig.getBindingTableGroups().add("T_ORDER${0..2}");
        this.shardingDataSource = ShardingDataSourceFactory.createDataSource(createDataSourceMap(), shardingRuleConfig,  new HashMap<>(1), new Properties());
    }
    /**
     * 分表策略
     * @return
     */
    private TableRuleConfiguration getOrderTableRuleConfiguration() {
        TableRuleConfiguration result = new TableRuleConfiguration();
        //逻辑表名称
        result.setLogicTable("T_ORDER");
        //源名 + 表名
        result.setActualDataNodes("ds0.T_ORDER${0..2}");
        //自增列名称
        result.setKeyGeneratorColumnName("id");
        return result;
    }

    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>(1);
        result.put("ds0", dataSource);
        return result;
    }

    /**
     * 设置数据源为sharding jdbc
     * @return
     */
    @Bean
    public MybatisSqlSessionFactoryBean mybatisSqlSessionFactoryBean() {
        MybatisSqlSessionFactoryBean mysqlplus = new MybatisSqlSessionFactoryBean();
        mysqlplus.setDataSource(this.getShardingDataSource());
        return mysqlplus;
    }

    public DataSource getShardingDataSource(){
        return this.shardingDataSource;
    }
}
