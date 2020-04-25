package uk.co.kleindelao.mapstruct.spring.example.classname;

import org.mapstruct.MapperConfig;
import uk.co.kleindelao.mapstruct.spring.SpringMapperConfig;

@MapperConfig(componentModel = "spring", uses = MyAdapter.class)
@SpringMapperConfig(conversionServiceAdapterClassName ="MyAdapter")
public interface MapperSpringConfig {
}
