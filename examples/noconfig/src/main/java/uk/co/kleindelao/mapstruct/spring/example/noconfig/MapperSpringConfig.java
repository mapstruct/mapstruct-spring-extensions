package uk.co.kleindelao.mapstruct.spring.example.noconfig;

import org.mapstruct.MapperConfig;
import uk.co.kleindelao.mapstruct.spring.converter.ConversionServiceBridge;

@MapperConfig(componentModel = "spring", uses = ConversionServiceBridge.class)
public interface MapperSpringConfig {
}
