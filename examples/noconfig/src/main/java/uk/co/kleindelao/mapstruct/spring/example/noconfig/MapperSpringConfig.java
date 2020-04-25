package uk.co.kleindelao.mapstruct.spring.example.noconfig;

import org.mapstruct.MapperConfig;
import uk.co.kleindelao.mapstruct.spring.converter.ConversionServiceAdapter;

@MapperConfig(componentModel = "spring", uses = ConversionServiceAdapter.class)
public interface MapperSpringConfig {
}
