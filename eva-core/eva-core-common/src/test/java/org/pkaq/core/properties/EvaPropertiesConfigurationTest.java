package org.pkaq.core.properties;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * EVA 配置绑定测试。
 *
 * @author PKAQ
 */
class EvaPropertiesConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(EvaPropertiesConfiguration.class);

    /**
     * 验证 EVA 配置通过独立适配器完成绑定。
     */
    @Test
    void shouldBindEvaConfigurationThroughAdapter() {
        contextRunner.withPropertyValues(
                        "eva.mode=platform",
                        "eva.tenant.enable=true",
                        "eva.tenant.mode=schema",
                        "eva.tenant.prefix=customer_",
                        "eva.jwt.secret=01234567890123456789012345678901",
                        "eva.upload.type=ng")
                .run(context -> {
                    assertThat(context).hasSingleBean(EvaConfig.class);

                    EvaConfig config = context.getBean(EvaConfig.class);
                    assertThat(config.isPlatformMode()).isTrue();
                    assertThat(config.getTenant().isSchemaMode()).isTrue();
                    assertThat(config.getTenant().getPrefix()).isEqualTo("customer_");
                    assertThat(config.getJwt().getSecret()).isEqualTo("01234567890123456789012345678901");
                    assertThat(config.getUpload().getType()).isEqualTo("ng");
                });
    }

    /**
     * 验证业务配置模型可以脱离 Spring 独立使用。
     */
    @Test
    void shouldCreateEvaConfigurationWithoutSpring() {
        EvaConfig config = new EvaConfig();

        assertThat(config.isStandaloneMode()).isTrue();
        assertThat(config.getTenant()).isNotNull();
        assertThat(config.getJwt()).isNotNull();
        assertThat(config.getUpload()).isNotNull();
    }

    /**
     * 验证 JWT 密钥缺失时启动失败。
     */
    @Test
    void shouldRejectMissingJwtSecret() {
        contextRunner.run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure())
                    .hasMessage("eva.jwt.secret 必须通过配置文件或环境变量提供");
        });
    }

    /** 验证显式关闭认证后无需配置 JWT 密钥。 */
    @Test
    void shouldAllowExplicitAnonymousApplicationWithoutJwtSecret() {
        contextRunner.withPropertyValues("eva.auth.authentication.enabled=false")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    EvaConfig config = context.getBean(EvaConfig.class);
                    assertThat(config.getAuth().isAuthenticationEnabled()).isFalse();
                    assertThat(config.getAuth().isJwtEnabled()).isFalse();
                    assertThat(config.getAuth().isOpenApiEnabled()).isFalse();
                });
    }

    /** 验证资源权限不能在关闭认证的情况下启用。 */
    @Test
    void shouldRejectResourcePermissionWithoutAuthentication() {
        assertInvalidCapability("eva.resource-permission.enable=true",
                "eva.resource-permission.enable 开启时必须开启认证");
    }

    /** 验证数据权限不能在关闭认证的情况下启用。 */
    @Test
    void shouldRejectDataPermissionWithoutAuthentication() {
        assertInvalidCapability("eva.data-permission.enable=true",
                "eva.data-permission.enable 开启时必须开启认证");
    }

    /** 验证匿名请求不能启用需要可信身份的租户路由。 */
    @Test
    void shouldRejectTenantRoutingWithoutAuthentication() {
        assertInvalidCapability("eva.tenant.enable=true",
                "eva.tenant.enable 开启时必须开启认证");
    }

    /** 验证数据权限可以独立于接口资源权限启用。 */
    @Test
    void shouldAllowDataPermissionWithoutResourcePermission() {
        contextRunner.withPropertyValues(
                        "eva.jwt.secret=01234567890123456789012345678901",
                        "eva.resource-permission.enable=false",
                        "eva.data-permission.enable=true")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    EvaConfig config = context.getBean(EvaConfig.class);
                    assertThat(config.getDataPermission().isEnable()).isTrue();
                    assertThat(config.getResourcePermission().isEnable()).isFalse();
                });
    }

    private void assertInvalidCapability(String property, String expectedMessage) {
        contextRunner.withPropertyValues("eva.auth.authentication.enabled=false", property)
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure()).hasMessage(expectedMessage);
                });
    }

    /** 验证认证开启时不能关闭全部现有认证机制。 */
    @Test
    void shouldRejectAuthenticationWithoutMechanism() {
        contextRunner.withPropertyValues("eva.auth.jwt.enabled=false", "eva.auth.open-api.enabled=false")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasMessage("开启认证时必须启用 JWT 或 OpenAPI 认证机制");
                });
    }
}
