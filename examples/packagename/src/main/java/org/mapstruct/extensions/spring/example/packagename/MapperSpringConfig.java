package org.mapstruct.extensions.spring.example.packagename;

import org.mapstruct.MapperConfig;
import org.mapstruct.extensions.spring.example.adapter.ConversionServiceAdapter;
import org.mapstruct.extensions.spring.SpringMapperConfig;

@MapperConfig(componentModel = "spring", uses = ConversionServiceAdapter.class)
@SpringMapperConfig(conversionServiceAdapterPackage ="org.mapstruct.extensions.spring.example.adapter")
public interface MapperSpringConfig {
}
