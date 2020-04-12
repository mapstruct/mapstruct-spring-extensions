package uk.co.kleindelao.mapstruct.spring.example.classname;

import org.mapstruct.MapperConfig;
import uk.co.kleindelao.mapstruct.spring.SpringMapperConfig;

@MapperConfig(componentModel = "spring", uses = MyBridge.class)
@SpringMapperConfig(conversionServiceBridgeClassName="MyBridge")
public interface MapperSpringConfig {
}
