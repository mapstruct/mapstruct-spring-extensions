package org.mapstruct.extensions.spring.example.boot.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.extensions.spring.example.Wheel;
import org.mapstruct.extensions.spring.example.WheelDto;
import org.mapstruct.extensions.spring.example.Wheels;
import org.mapstruct.extensions.spring.example.boot.mappers.MapperSpringConfig;
import org.springframework.core.convert.converter.Converter;

import java.util.List;

@Mapper(config = MapperSpringConfig.class, imports = Wheel.class)
public interface WheelsMapper extends Converter<Wheels, List<WheelDto>> {
    @Override
    List<WheelDto> convert(Wheels source);
}
