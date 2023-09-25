package org.mapstruct.extensions.spring.example.noexplicitconvert;

import org.mapstruct.Mapper;
import org.springframework.core.convert.converter.Converter;

@Mapper(config = MapstructConfig.class)
public interface SimplePointMapper extends Converter<SimplePoint, PointDto> {
}
