package org.mapstruct.extensions.spring.example.boot.mappers;

import org.mapstruct.MapperConfig;
import org.mapstruct.extensions.spring.SpringMapperConfig;
import org.mapstruct.extensions.spring.example.boot.ConversionServiceAdapter;

@MapperConfig(componentModel = "spring", uses = ConversionServiceAdapter.class)
@SpringMapperConfig(conversionServiceAdapterPackage = "org.mapstruct.extensions.spring.example.boot")
public interface MapperSpringConfig {
}
