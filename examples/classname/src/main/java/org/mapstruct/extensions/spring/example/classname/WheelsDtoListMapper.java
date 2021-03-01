package org.mapstruct.extensions.spring.example.classname;

import org.mapstruct.Mapper;
import org.mapstruct.extensions.spring.example.WheelDto;
import org.mapstruct.extensions.spring.example.Wheels;
import org.springframework.core.convert.converter.Converter;

import java.util.List;

@Mapper(config = MapperSpringConfig.class)
public interface WheelsDtoListMapper extends Converter<List<WheelDto>, Wheels> {
    @Override
    Wheels convert(List<WheelDto> source);
}
