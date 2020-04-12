package uk.co.kleindelao.mapstruct.spring.example.packagename;

import org.mapstruct.MapperConfig;
import uk.co.kleindelao.mapstruct.spring.example.bridge.ConversionServiceBridge;
import uk.co.kleindelao.mapstruct.spring.SpringMapperConfig;

@MapperConfig(componentModel = "spring", uses = ConversionServiceBridge.class)
@SpringMapperConfig(conversionServiceBridgePackage="uk.co.kleindelao.mapstruct.spring.example.bridge")
public interface MapperSpringConfig {
}
