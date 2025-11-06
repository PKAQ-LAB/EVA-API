package org.pkaq.core.mvc.convert;


import org.mapstruct.*;

/**
 * @author PKAQ
 */
@MapperConfig(componentModel = "spring",
        mappingInheritanceStrategy = MappingInheritanceStrategy.EXPLICIT,
        nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MapConvertConfig {
}
