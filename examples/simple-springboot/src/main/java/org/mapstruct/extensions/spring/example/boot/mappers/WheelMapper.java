package org.mapstruct.extensions.spring.example.boot.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.extensions.spring.example.Wheel;
import org.mapstruct.extensions.spring.example.WheelDto;
import org.mapstruct.extensions.spring.example.boot.mappers.MapperSpringConfig;
import org.springframework.core.convert.converter.Converter;

@Mapper(config = MapperSpringConfig.class)
public interface WheelMapper extends Converter<Wheel, WheelDto> {
    @Override
    WheelDto convert(Wheel source);
}
