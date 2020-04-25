package uk.co.kleindelao.mapstruct.spring.example.packageandclass;

import org.mapstruct.MapperConfig;
import uk.co.kleindelao.mapstruct.spring.SpringMapperConfig;
import uk.co.kleindelao.mapstruct.spring.example.adapter.MyAdapter;

@MapperConfig(componentModel = "spring", uses = MyAdapter.class)
@SpringMapperConfig(conversionServiceAdapterPackage ="uk.co.kleindelao.mapstruct.spring.example.adapter",
        conversionServiceAdapterClassName ="MyAdapter")
public interface MapperSpringConfig {
}
