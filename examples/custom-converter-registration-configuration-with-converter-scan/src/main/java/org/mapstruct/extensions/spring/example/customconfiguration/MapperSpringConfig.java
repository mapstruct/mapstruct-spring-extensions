package org.mapstruct.extensions.spring.example.customconfiguration;

import org.mapstruct.MapperConfig;
import org.mapstruct.extensions.spring.SpringMapperConfig;

@MapperConfig(componentModel = "spring", uses = ConversionServiceAdapter.class)
@SpringMapperConfig(
        converterRegistrationConfigurationClassName = "MyConfiguration",
//        conversionServiceBeanName = "conversionService",
        generateConverterScan = true)
public interface MapperSpringConfig {
}
