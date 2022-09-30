package org.mapstruct.extensions.spring.example.noexplicitconvert;

import org.mapstruct.MapperConfig;
import org.mapstruct.extensions.spring.converter.ConversionServiceAdapter;

@MapperConfig(componentModel = "spring", uses= ConversionServiceAdapter.class)
public interface MapstructConfig {}
