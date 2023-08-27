package org.mapstruct.extensions.spring.example.custombeanwithregistration;

import org.mapstruct.MapperConfig;
import org.mapstruct.extensions.spring.SpringMapperConfig;

@MapperConfig(componentModel = "spring", uses = ConversionServiceAdapter.class)
@SpringMapperConfig(
    conversionServiceBeanName = "myConversionService",
    generateRegistrationClasses = true)
public interface MapperSpringConfig {}
