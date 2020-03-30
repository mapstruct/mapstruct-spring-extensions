package uk.co.kleindelao.mapstruct.spring.example;

import org.mapstruct.MapperConfig;
import uk.co.kleindelao.mapstruct.spring.converter.ConversionServiceBridge;

import static org.mapstruct.ReportingPolicy.ERROR;

@MapperConfig(componentModel = "spring", uses = ConversionServiceBridge.class, unmappedTargetPolicy = ERROR)
public interface MapperSpringConfig {
}
