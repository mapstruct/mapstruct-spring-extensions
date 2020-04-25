package org.mapstruct.extensions.spring.example.classname;

import org.mapstruct.MapperConfig;
import org.mapstruct.extensions.spring.SpringMapperConfig;

@MapperConfig(componentModel = "spring", uses = MyAdapter.class)
@SpringMapperConfig(conversionServiceAdapterClassName ="MyAdapter")
public interface MapperSpringConfig {
}
