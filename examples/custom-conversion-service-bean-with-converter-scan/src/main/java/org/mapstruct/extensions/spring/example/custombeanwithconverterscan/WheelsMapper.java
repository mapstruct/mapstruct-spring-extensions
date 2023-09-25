package org.mapstruct.extensions.spring.example.custombeanwithconverterscan;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.extensions.spring.example.Wheel;
import org.mapstruct.extensions.spring.example.WheelDto;
import org.mapstruct.extensions.spring.example.Wheels;
import org.springframework.core.convert.converter.Converter;

@Mapper(config = MapperSpringConfig.class, imports = Wheel.class)
public interface WheelsMapper extends Converter<Wheels, List<WheelDto>> {
    @Override
    List<WheelDto> convert(Wheels source);
}
