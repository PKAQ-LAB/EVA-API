package org.pkaq.core.properties;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * EVA 独立配置文件加载测试。
 *
 * @author PKAQ
 */
class EvaConfigDataIntegrationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withInitializer(new ConfigDataApplicationContextInitializer())
            .withUserConfiguration(EvaPropertiesConfiguration.class)
            .withPropertyValues("spring.config.import=classpath:config/eva/eva.yaml");

    /**
     * 验证开发环境加载公共配置和开发覆盖配置。
     */
    @Test
    void shouldLoadDevelopmentEvaConfiguration() {
        contextRunner.withPropertyValues(
                        "spring.profiles.active=dev",
                        "EVA_MINIO_URL=http://test-minio")
                .run(context -> {
                    EvaConfig config = context.getBean(EvaConfig.class);

                    assertThat(config.isStandaloneMode()).isTrue();
                    assertThat(config.getJwt().getSecret())
                            .isEqualTo("local-development-only-secret-change-me");
                    assertThat(config.getUpload().getType()).isEqualTo("ng");
                    assertThat(config.getUpload().getMinIo().getUrl()).isEqualTo("http://test-minio");
                    assertThat(config.getCookie().isSecure()).isFalse();
                    assertThat(config.getAuth().isOpenApiEnabled()).isTrue();
                    assertThat(config.getAuth().matchOpenApiPath("/apr/example")).isTrue();
                    assertThat(config.getDataPermission().isEnable()).isTrue();
                });
    }

    /**
     * 验证生产环境加载生产覆盖配置并要求外部 JWT 密钥。
     */
    @Test
    void shouldLoadProductionEvaConfiguration() {
        contextRunner.withPropertyValues(
                        "spring.profiles.active=prod",
                        "EVA_JWT_SECRET=01234567890123456789012345678901")
                .run(context -> {
                    EvaConfig config = context.getBean(EvaConfig.class);

                    assertThat(config.getJwt().getSecret())
                            .isEqualTo("01234567890123456789012345678901");
                    assertThat(config.getCookie().isSecure()).isTrue();
                    assertThat(config.getCookie().getMaxAge()).isEqualTo(3600);
                    assertThat(config.getUpload().getStoragePath()).isEqualTo("E://evapic/storage");
                });
    }
}
