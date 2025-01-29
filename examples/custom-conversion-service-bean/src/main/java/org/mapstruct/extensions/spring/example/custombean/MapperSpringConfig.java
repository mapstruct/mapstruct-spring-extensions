package org.mapstruct.extensions.spring.example.custombean;

import org.mapstruct.MapperConfig;
import org.mapstruct.extensions.spring.SpringMapperConfig;

@MapperConfig(componentModel = "spring", uses = ConversionServiceAdapter.class)
@SpringMapperConfig(conversionServiceBeanName = "myConversionService", generateConverterScan = true)
public interface MapperSpringConfig {
}
