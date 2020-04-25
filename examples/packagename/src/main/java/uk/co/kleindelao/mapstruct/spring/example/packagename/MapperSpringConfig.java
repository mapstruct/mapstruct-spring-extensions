package uk.co.kleindelao.mapstruct.spring.example.packagename;

import org.mapstruct.MapperConfig;
import uk.co.kleindelao.mapstruct.spring.example.adapter.ConversionServiceAdapter;
import uk.co.kleindelao.mapstruct.spring.SpringMapperConfig;

@MapperConfig(componentModel = "spring", uses = ConversionServiceAdapter.class)
@SpringMapperConfig(conversionServiceAdapterPackage ="uk.co.kleindelao.mapstruct.spring.example.adapter")
public interface MapperSpringConfig {
}
