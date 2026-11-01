package org.pkaq.sys.dict;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.Test;
import org.pkaq.sys.dict.entity.DictItemEntity;
import org.pkaq.sys.dict.mapper.DictItemMapper;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.FileSystemResource;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 仅合成PostgreSQL验证真实Mapper逻辑删除和生产配置方言绑定，不启动应用。
 *
 * @author PKAQ
 * @date 2026-11-01
 */
public class DictLogicalDeletePostgresTest {
    /** 允许已封缓存classpath直接运行同一回归，避免扩大构建与测试范围。 */
    public static void main(String[] args) throws Exception {
        new DictLogicalDeletePostgresTest().deletesLogicallyWithActualDevProperties();
        System.out.println("PASS: baseline 42883; dev logical delete; physical retention; group isolation");
    }

    @Test
    void deletesLogicallyWithActualDevProperties() throws Exception {
        // 实际profile含连接凭据，只绑定所需配置，不让YAML调试日志输出原文。
        if (org.slf4j.LoggerFactory.getILoggerFactory() instanceof ch.qos.logback.classic.LoggerContext logging) {
            logging.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).setLevel(ch.qos.logback.classic.Level.OFF);
        }
        Path source = Path.of(System.getProperty("eva.test.resources", "src/main/resources"));
        MybatisPlusProperties mysql = properties(source, null);
        MybatisPlusProperties dev = properties(source, "dev");
        assertEquals("UNIX_TIMESTAMP()", mysql.getGlobalConfig().getDbConfig().getLogicDeleteValue());
        assertEquals("UNIX_TIMESTAMP()", properties(source, "prod")
                .getGlobalConfig().getDbConfig().getLogicDeleteValue());
        assertEquals("UNIX_TIMESTAMP()", properties(source, "aliyun")
                .getGlobalConfig().getDbConfig().getLogicDeleteValue());
        assertEquals("CAST(FLOOR(EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)) AS bigint)",
                dev.getGlobalConfig().getDbConfig().getLogicDeleteValue());
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder()
                .setServerConfig("listen_addresses", "127.0.0.1").setPort(0).start();
             Connection connection = postgres.getPostgresDatabase().getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE SYS_DICT_ITEM (id bigint PRIMARY KEY, main_id bigint,"
                    + " deleted bigint NOT NULL DEFAULT 0, modify_id bigint, modify_by text, utc_modify timestamp)");
            statement.execute("INSERT INTO SYS_DICT_ITEM (id,main_id) VALUES (1,101),(2,101),(3,102)");
            SqlSessionFactory baseline = factory(postgres.getPostgresDatabase(), mysql);
            try (SqlSession session = baseline.openSession()) {
                Exception failure = assertThrows(Exception.class,
                        () -> session.getMapper(DictItemMapper.class).deleteById(1L));
                Throwable cause = failure;
                while (cause.getCause() != null) {
                    cause = cause.getCause();
                }
                assertTrue(cause instanceof SQLException);
                assertEquals("42883", ((SQLException) cause).getSQLState());
                session.rollback();
            }
            SqlSessionFactory fixed = factory(postgres.getPostgresDatabase(), dev);
            try (SqlSession session = fixed.openSession()) {
                DictItemMapper mapper = session.getMapper(DictItemMapper.class);
                // 对应差异保存遗漏的旧明细；事务中仍使用真实MP生成的逻辑删除。
                assertEquals(1, mapper.deleteById(1L));
                session.commit();
            }
            try (var result = statement.executeQuery("SELECT count(*), count(*) FILTER (WHERE deleted>0),"
                    + " count(*) FILTER (WHERE id IN (2,3) AND deleted=0) FROM SYS_DICT_ITEM")) {
                assertTrue(result.next());
                assertEquals(3, result.getInt(1));
                assertEquals(1, result.getInt(2));
                assertEquals(2, result.getInt(3));
            }
            try (SqlSession session = fixed.openSession()) {
                assertEquals(0, session.getMapper(DictItemMapper.class).deleteById(1L));
                session.rollback();
            }
        }
    }

    private MybatisPlusProperties properties(Path source, String profile) {
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        if (profile == null) {
            yaml.setResources(new FileSystemResource(source.resolve("application.yaml")));
        } else {
            yaml.setResources(new FileSystemResource(source.resolve("application.yaml")),
                    new FileSystemResource(source.resolve("application-" + profile + ".yaml")));
        }
        Properties values = yaml.getObject();
        return new Binder(ConfigurationPropertySources.from(new PropertiesPropertySource("actual-profile", values)))
                .bind("mybatis-plus", Bindable.of(MybatisPlusProperties.class)).get();
    }

    private SqlSessionFactory factory(DataSource dataSource, MybatisPlusProperties properties) {
        TableInfoHelper.remove(DictItemEntity.class);
        MybatisConfiguration config = new MybatisConfiguration();
        config.setDatabaseId("postgre");
        config.setEnvironment(new Environment("synthetic", new JdbcTransactionFactory(), dataSource));
        GlobalConfigUtils.setGlobalConfig(config, properties.getGlobalConfig());
        config.addMapper(DictItemMapper.class);
        return new MybatisSqlSessionFactoryBuilder().build(config);
    }
}
