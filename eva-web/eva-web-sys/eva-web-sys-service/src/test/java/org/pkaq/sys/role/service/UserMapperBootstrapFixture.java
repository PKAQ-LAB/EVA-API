package org.pkaq.sys.role.service;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusPropertiesCustomizer;
import com.baomidou.mybatisplus.autoconfigure.SqlSessionFactoryBeanCustomizer;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.core.injector.ISqlInjector;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeHandler;
import org.pkaq.config.MybatisPlusConfig;
import org.pkaq.core.properties.EvaConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ClassUtils;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockingDetails;

/**
 * 仅启动生产MyBatis组件，使用合成数据源，不启动业务应用或外部服务。
 * @author PKAQ
 * @date 2026-10-27
 */
final class UserMapperBootstrapFixture implements AutoCloseable {
    enum Order {
        NORMAL, USER_FIRST, COMMON_FIRST, LEGACY_USER_FIRST
    }

    private final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
    final List<String> resources;
    final SqlSessionFactory factory;

    UserMapperBootstrapFixture(DataSource dataSource, EvaConfig config, Order order) throws Exception {
        assertTrue(mockingDetails(dataSource).isMock(), "只接受Mockito合成数据源");
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
        environment.getPropertySources().remove(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
        environment.getPropertySources().addFirst(new MapPropertySource("C-20261027-synthetic", Map.ofEntries(
                Map.entry("mybatis-plus.mapper-locations", "classpath*:/mapper/*.xml"),
                Map.entry("mybatis-plus.type-aliases-package", "org.pkaq.*.**.entity"),
                Map.entry("mybatis-plus.type-handlers-package", "org.pkaq.core.mybatis.enums"),
                Map.entry("mybatis-plus.configuration.map-underscore-to-camel-case", "true"),
                Map.entry("mybatis-plus.configuration.cache-enabled", "false"),
                Map.entry("mybatis-plus.global-config.db-config.id-type", "assign_id"),
                Map.entry("mybatis-plus.global-config.db-config.table-underline", "true"),
                Map.entry("mybatis-plus.global-config.db-config.where-strategy", "not_empty"),
                Map.entry("mybatis-plus.global-config.db-config.logic-delete-value", "UNIX_TIMESTAMP()"),
                Map.entry("mybatis-plus.global-config.db-config.logic-not-delete-value", "0"),
                Map.entry("mybatis-plus.global-config.banner", "false"),
                // 只关闭测试SQL日志，防止合成凭据进入失败日志；不改生产配置。
                Map.entry("mybatis-plus.configuration.log-impl",
                        "org.apache.ibatis.logging.nologging.NoLoggingImpl"))));
        context.setEnvironment(environment);
        context.registerBean(DataSource.class, () -> dataSource);
        context.registerBean(EvaConfig.class, () -> config);
        // 仅提供与WebBooter相同的根包元数据，由真实MP注册器扫描@Mapper接口。
        AutoConfigurationPackages.register(context, "org.pkaq");
        // 显式白名单注册，不启用组件扫描、ConfigData或其他自动配置。
        context.register(MybatisPlusConfig.class, MybatisPlusAutoConfiguration.class);
        Resource[] resolved = new PathMatchingResourcePatternResolver().getResources("classpath*:/mapper/*.xml");
        assertEquals(1L, Arrays.stream(resolved).filter(item -> "User.xml".equals(item.getFilename())).count());
        assertEquals(1L, Arrays.stream(resolved).filter(item -> "common.xml".equals(item.getFilename())).count());
        Resource[] selected = order == Order.NORMAL ? resolved : ordered(resolved, order);
        resources = Arrays.stream(selected).map(Resource::getDescription).toList();
        if (order != Order.NORMAL) {
            context.registerBean(SqlSessionFactoryBeanCustomizer.class,
                    () -> bean -> bean.setMapperLocations(selected));
        }
        context.addBeanFactoryPostProcessor(UserMapperBootstrapFixture::assertBeanDefinitions);
        try {
            context.refresh();
            assertEquals(1, context.getBeansOfType(DataSource.class).size());
            assertTrue(mockingDetails(context.getBean(DataSource.class)).isMock());
            assertTrue(context.getBean(DataSource.class) == dataSource);
            assertEquals(1, context.getBeansOfType(MybatisPlusInterceptor.class).size());
            assertEquals(1, context.getBeansOfType(DatabaseIdProvider.class).size());
            assertEquals(0, context.getBeansOfType(ConfigurationCustomizer.class).size());
            assertEquals(0, context.getBeansOfType(MybatisPlusPropertiesCustomizer.class).size());
            assertEquals(order == Order.NORMAL ? 0 : 1,
                    context.getBeansOfType(SqlSessionFactoryBeanCustomizer.class).size());
            assertEquals(0, context.getBeansOfType(MetaObjectHandler.class).size());
            assertEquals(0, context.getBeansOfType(ISqlInjector.class).size());
            assertEquals(0, context.getBeansOfType(TypeHandler.class).size());
            assertEquals(0, context.getBeansOfType(
                    org.springframework.scheduling.TaskScheduler.class).size());
            factory = context.getBean(SqlSessionFactory.class);
            assertEquals("postgre", factory.getConfiguration().getDatabaseId());
        } catch (RuntimeException | Error failure) {
            context.close();
            throw failure;
        }
    }

    static void assertBeanDefinitions(ConfigurableListableBeanFactory beans) {
        Set<String> classes = Set.of(
                "javax.sql.DataSource", EvaConfig.class.getName(), MybatisPlusConfig.class.getName(),
                MybatisPlusAutoConfiguration.class.getName(), SqlSessionFactoryBeanCustomizer.class.getName(),
                "com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties",
                "com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration"
                        + "$MapperScannerRegistrarNotFoundConfiguration",
                "org.mybatis.spring.mapper.MapperFactoryBean", "org.mybatis.spring.mapper.MapperScannerConfigurer",
                "org.springframework.boot.autoconfigure.AutoConfigurationPackages$BasePackages",
                "org.springframework.context.annotation.ConfigurationClassPostProcessor",
                "org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor",
                "org.springframework.context.annotation.CommonAnnotationBeanPostProcessor",
                "org.springframework.context.event.EventListenerMethodProcessor",
                "org.springframework.context.event.DefaultEventListenerFactory",
                "org.springframework.aop.framework.autoproxy.InfrastructureAdvisorAutoProxyCreator",
                "org.springframework.transaction.annotation.ProxyTransactionManagementConfiguration",
                "org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor",
                "org.springframework.boot.context.properties.ConfigurationPropertiesBinder"
                        + "$ConfigurationPropertiesBinderFactory",
                "org.springframework.boot.context.properties.BoundConfigurationProperties",
                "org.springframework.boot.validation.beanvalidation.MethodValidationExcludeFilter");
        Set<String> methods = Set.of("sqlSessionFactory", "sqlSessionTemplate",
                "mybatisPlusSpringApplicationContextAware", "mybatisPlusInterceptor", "getDatabaseIdProvider",
                "transactionAdvisor", "transactionAttributeSource", "transactionInterceptor",
                "transactionalEventListenerFactory");
        for (String name : beans.getBeanDefinitionNames()) {
            var definition = beans.getBeanDefinition(name);
            String beanClass = resolvedBeanClassName(name, definition, classes);
            if (beanClass != null) {
                assertTrue(classes.contains(beanClass), "隔离容器拒绝未许可BeanDefinition: " + name + ":" + beanClass);
            } else {
                String owner = definition.getFactoryBeanName();
                assertTrue(owner != null && beans.containsBeanDefinition(owner), "隔离工厂必须来自白名单配置");
                BeanDefinition ownerDefinition = beans.getBeanDefinition(owner);
                String ownerClass = resolvedBeanClassName(owner, ownerDefinition, classes);
                assertTrue(ownerClass != null && classes.contains(ownerClass));
                if (!ownerClass.equals(ownerDefinition.getBeanClassName())) {
                    assertTrue(Set.of("mybatisPlusInterceptor", "getDatabaseIdProvider")
                            .contains(definition.getFactoryMethodName()), "增强配置不得提供其它工厂方法");
                }
                assertTrue(methods.contains(definition.getFactoryMethodName()), "隔离容器拒绝未知@Bean方法: " + name);
            }
        }
    }

    private static String resolvedBeanClassName(String name, BeanDefinition definition, Set<String> classes) {
        String raw = definition.getBeanClassName();
        if (raw == null || classes.contains(raw) || !"mybatisPlusConfig".equals(name)) {
            return raw;
        }
        assertTrue(definition instanceof AbstractBeanDefinition, "增强配置必须有已解析的类定义");
        AbstractBeanDefinition resolved = (AbstractBeanDefinition) definition;
        assertTrue(resolved.hasBeanClass(), "不得为白名单校验解析或实例化未知类");
        Class<?> actual = resolved.getBeanClass();
        Class<?> expected = MybatisPlusConfig.class;
        assertSame(expected, ClassUtils.getUserClass(actual), "只接受显式注册配置的原类身份");
        assertSame(expected, actual.getSuperclass(), "增强类必须直接继承原配置");
        assertTrue(definition instanceof AnnotatedBeanDefinition, "必须保留原始注解注册元数据");
        AnnotatedBeanDefinition annotated = (AnnotatedBeanDefinition) definition;
        assertEquals(expected.getName(), annotated.getMetadata().getClassName());
        assertNull(definition.getFactoryBeanName(), "原配置定义不得来自工厂");
        assertNull(definition.getFactoryMethodName(), "原配置定义不得来自工厂方法");
        Configuration configuration = expected.getDeclaredAnnotation(Configuration.class);
        assertTrue(configuration != null && configuration.proxyBeanMethods(), "原配置必须启用配置方法代理");
        assertTrue(BeanFactoryAware.class.isAssignableFrom(actual), "增强类必须具有公开的BeanFactoryAware关系");
        return expected.getName();
    }

    private Resource[] ordered(Resource[] resolved, Order order) throws Exception {
        Resource user = Arrays.stream(resolved).filter(item -> "User.xml".equals(item.getFilename()))
                .findFirst().orElseThrow();
        Resource common = Arrays.stream(resolved).filter(item -> "common.xml".equals(item.getFilename()))
                .findFirst().orElseThrow();
        if (order == Order.LEGACY_USER_FIRST) {
            byte[] legacy = context.getResource("classpath:/fixtures/user-before-c.xml").getContentAsByteArray();
            user = new ByteArrayResource(legacy, "C-20261027-legacy/mapper/User.xml");
        }
        List<Resource> selected = new ArrayList<>();
        selected.add(order == Order.COMMON_FIRST ? common : user);
        selected.add(order == Order.COMMON_FIRST ? user : common);
        Arrays.stream(resolved).filter(item -> !List.of("User.xml", "common.xml").contains(item.getFilename()))
                .forEach(selected::add);
        assertEquals(resolved.length, selected.size());
        return selected.toArray(Resource[]::new);
    }

    /** 释放隔离容器，不操作真实应用。 */
    @Override
    public void close() {
        context.close();
    }
}
