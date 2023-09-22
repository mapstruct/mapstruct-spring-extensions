package org.mapstruct.extensions.spring.example.delegatingconverter;

import org.mapstruct.MapperConfig;
import org.mapstruct.extensions.spring.SpringMapperConfig;

@MapperConfig(componentModel = "spring", uses = ConversionServiceAdapter.class)
@SpringMapperConfig
public interface MapperSpringConfig {}
