package org.pkaq.core.mvc.convert;


import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

/**
 * @author PKAQ
 */
@MapperConfig(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MapConvertConfig {

}
