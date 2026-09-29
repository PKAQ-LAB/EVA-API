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
}
