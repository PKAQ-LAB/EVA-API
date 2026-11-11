package org.pkaq.core.log.config;

import ch.qos.logback.classic.LoggerContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.pkaq.core.tenant.TenantLogContext;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.logging.LoggingInitializationContext;
import org.springframework.boot.logging.logback.LogbackLoggingSystem;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 使用真实日志配置验证文件分流，不启动应用或连接数据库。
 *
 * @author PKAQ
 * @date 2026-11-11
 */
class TenantLogFilesTest {
    /** 本次测试独立文件目录。 */
    @TempDir
    Path directory;

    /** 实际 Spring Boot 日志初始化器。 */
    private LogbackLoggingSystem logging;

    /**
     * 释放文件句柄，避免测试间共享日志路由。
     */
    @AfterEach
    void cleanLogging() {
        MDC.clear();
        if (logging != null) {
            ((LoggerContext) LoggerFactory.getILoggerFactory()).stop();
            logging.cleanUp();
        }
    }

    /**
     * 验证两个租户、平台和四种类型实际输出到带日期的不同文件。
     */
    @Test
    void writesSeparateDatedFiles() throws Exception {
        initialize(true);
        TenantLogContext.runWithTenant(11L, () -> {
            LoggerFactory.getLogger("eva.audit.login").info("tenant-eleven");
            LoggerFactory.getLogger("eva.audit.operation").info("operation-eleven");
            LoggerFactory.getLogger("app").error("error-eleven", new IllegalStateException("test-stack"));
        });
        TenantLogContext.runWithTenant(22L,
                () -> LoggerFactory.getLogger("eva.audit.login").info("tenant-twenty-two"));
        LoggerFactory.getLogger("app").info("platform-only");
        assertTrue(Files.readString(file("tenant-11/login")).contains("tenant-eleven"));
        assertFalse(Files.readString(file("tenant-11/login")).contains("tenant-twenty-two"));
        assertTrue(Files.readString(file("tenant-22/login")).contains("tenant-twenty-two"));
        assertTrue(Files.readString(file("tenant-11/operation")).contains("operation-eleven"));
        assertTrue(Files.readString(file("tenant-11/error")).contains("test-stack"));
        assertTrue(Files.readString(file("platform/application")).contains("platform-only"));
    }

    /**
     * 关闭租户模式时不分租户，非法目录值也不能越界。
     */
    @Test
    void standaloneDoesNotSplitTenants() throws Exception {
        initialize(false);
        TenantLogContext.runWithTenant(11L,
                () -> LoggerFactory.getLogger("eva.audit.login").info("standalone"));
        assertTrue(Files.readString(file("standalone/login")).contains("standalone"));
        assertFalse(Files.exists(directory.resolve("tenant-11")));
    }

    /**
     * 异步线程池复用时日志归属不串租户，认证作用域不会被跨线程绑定。
     */
    @Test
    void asyncCapturesTenantAndRestoresContext() throws Exception {
        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) new LogAsyncConfig().logTaskExecutor();
        try {
            ThreadUser user = new ThreadUser();
            user.setTenantId(11L);
            ThreadUserHelper.runWithUser(user, () -> {
                try {
                    assertEquals("11", executor.submit(TenantLogContext::getTenantId).get(5, TimeUnit.SECONDS));
                } catch (Exception exception) {
                    throw new IllegalStateException(exception);
                }
            });
            assertNull(executor.submit(TenantLogContext::getTenantId).get(5, TimeUnit.SECONDS));
            assertNull(executor.submit(ThreadUserHelper::getCurrentUserOrNull).get(5, TimeUnit.SECONDS));
            MDC.put(TenantLogContext.KEY, "99");
            TenantLogContext.runWithTenant(22L, () -> assertEquals("22", TenantLogContext.getTenantId()));
            assertEquals("99", MDC.get(TenantLogContext.KEY));
        } finally {
            executor.shutdown();
        }
    }

    /**
     * 非法路径只能进入平台目录。
     */
    @Test
    void invalidTenantCannotBecomePath() throws Exception {
        initialize(true);
        MDC.put(TenantLogContext.KEY, "../../escape");
        LoggerFactory.getLogger("app").info("invalid-tenant");
        assertTrue(Files.readString(file("platform/application")).contains("invalid-tenant"));
    }

    /**
     * 从宿主相同的 XML 初始化真实日志系统。
     */
    private void initialize(boolean enabled) {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("eva.tenant.enable", Boolean.toString(enabled))
                .withProperty("logging.file.path", directory.toString());
        logging = new LogbackLoggingSystem(getClass().getClassLoader());
        logging.beforeInitialize();
        logging.initialize(new LoggingInitializationContext(environment), "classpath:eva-logback-spring.xml", null);
    }

    /**
     * 与日志配置使用相同的日期时区。
     */
    private Path file(String route) {
        return directory.resolve(route).resolve(LocalDate.now(ZoneId.of("Asia/Shanghai")) + ".log");
    }
}
