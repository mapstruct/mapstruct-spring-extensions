package uk.co.kleindelao.mapstruct.spring.example.packagename;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.core.convert.converter.Converter;
import uk.co.kleindelao.mapstruct.spring.example.SeatConfiguration;
import uk.co.kleindelao.mapstruct.spring.example.SeatConfigurationDto;

@Mapper(config = MapperSpringConfig.class)
public interface SeatConfigurationMapper extends Converter<SeatConfiguration, SeatConfigurationDto> {
    @Mapping(target = "seatCount", source = "numberOfSeats")
    @Mapping(target = "material", source = "seatMaterial")
    SeatConfigurationDto convert(SeatConfiguration seatConfiguration);
}
