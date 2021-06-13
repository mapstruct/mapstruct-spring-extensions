package org.mapstruct.extensions.spring.example.boot.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.extensions.spring.example.SeatConfiguration;
import org.mapstruct.extensions.spring.example.SeatConfigurationDto;
import org.springframework.core.convert.converter.Converter;

@Mapper(config = MapperSpringConfig.class)
public interface SeatConfigurationMapper extends Converter<SeatConfiguration, SeatConfigurationDto> {
    @Mapping(target = "seatCount", source = "numberOfSeats")
    @Mapping(target = "material", source = "seatMaterial")
    SeatConfigurationDto convert(SeatConfiguration seatConfiguration);
}
