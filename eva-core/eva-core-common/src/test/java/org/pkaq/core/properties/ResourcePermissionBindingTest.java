package org.pkaq.core.properties;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.env.MapPropertySource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 验证精确资源权限键通过环境覆盖绑定到响应实际读取的配置getter。
 *
 * @author PKAQ
 * @date 2026-10-28
 */
class ResourcePermissionBindingTest {
    @Test
    void bindsEffectiveResourceEnable() {
        for (boolean enabled : new boolean[] {true, false}) {
            new ApplicationContextRunner()
                    .withUserConfiguration(EvaPropertiesConfiguration.class)
                    .withPropertyValues("eva.auth.jwt.enabled=false", "eva.auth.open-api.enabled=true",
                            "eva.resource-permission.enable=" + !enabled,
                            "eva.resource-permission.strict=" + !enabled)
                    .withInitializer(context -> context.getEnvironment().getPropertySources().addFirst(
                            new MapPropertySource("resource-permission-override",
                                    Map.of("eva.resource-permission.enable", enabled))))
                    .run(context -> {
                        assertThat(context).hasNotFailed();
                        EvaConfig config = context.getBean(EvaConfig.class);
                        assertThat(config.getResourcePermission().isEnable()).isEqualTo(enabled);
                        assertThat(config.getResourcePermission().isStrict()).isEqualTo(!enabled);
                    });
        }
    }
}
