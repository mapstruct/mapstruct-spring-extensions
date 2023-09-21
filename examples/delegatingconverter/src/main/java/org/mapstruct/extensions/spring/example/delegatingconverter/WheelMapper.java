package org.mapstruct.extensions.spring.example.delegatingconverter;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.extensions.spring.AdapterMethodName;
import org.mapstruct.extensions.spring.DelegatingConverter;
import org.mapstruct.extensions.spring.example.Wheel;
import org.mapstruct.extensions.spring.example.WheelDto;
import org.springframework.core.convert.converter.Converter;

@Mapper(config = MapperSpringConfig.class)
@AdapterMethodName("toDto")
public interface WheelMapper extends Converter<Wheel, WheelDto> {
    @Override
    WheelDto convert(Wheel source);

    @InheritInverseConfiguration
    @DelegatingConverter
    Wheel invertConvert(WheelDto wheelDto);
}
