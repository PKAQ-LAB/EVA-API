package io.nerv.config

import org.hibernate.validator.HibernateValidator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.validation.Validation
import javax.validation.Validator

/**
 * hibernate validator参数校验配置
 */
@Configuration
open class ValidatorConfiguration {
    @Bean
    open fun validator(): Validator {
        val validatorFactory = Validation.byProvider(HibernateValidator::class.java)
                .configure()
                .failFast(true)
                .buildValidatorFactory()
        return validatorFactory.validator
    }
}