package org.mapstruct.extensions.spring.example.externalconversions;

import org.mapstruct.Mapper;
import org.springframework.core.convert.converter.Converter;

@Mapper(config = MapstructConfig.class, uses = ConversionServiceAdapter.class)
public interface LocaleInfoDtoMapper extends Converter<LocaleInfoDto, LocaleInfo> {
    @Override
    LocaleInfo convert(LocaleInfoDto source);
}
