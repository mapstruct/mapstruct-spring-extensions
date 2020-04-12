package uk.co.kleindelao.mapstruct.spring.example.packageandclass;

import org.mapstruct.MapperConfig;
import uk.co.kleindelao.mapstruct.spring.SpringMapperConfig;
import uk.co.kleindelao.mapstruct.spring.example.bridge.MyBridge;

@MapperConfig(componentModel = "spring", uses = MyBridge.class)
@SpringMapperConfig(conversionServiceBridgePackage="uk.co.kleindelao.mapstruct.spring.example.bridge", conversionServiceBridgeClassName="MyBridge")
public interface MapperSpringConfig {
}
