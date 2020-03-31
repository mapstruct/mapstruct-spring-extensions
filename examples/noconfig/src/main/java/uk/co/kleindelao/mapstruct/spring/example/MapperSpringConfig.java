package uk.co.kleindelao.mapstruct.spring.example;

import static org.mapstruct.ReportingPolicy.ERROR;

import org.mapstruct.MapperConfig;
import uk.co.kleindelao.mapstruct.spring.converter.ConversionServiceBridge;

@MapperConfig(componentModel = "spring", uses = ConversionServiceBridge.class, unmappedTargetPolicy = ERROR)
public interface MapperSpringConfig {
}
