package org.mapstruct.extensions.spring.converter;

import org.springframework.context.annotation.Lazy;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;
import test.Car;
import test.CarDto;

@Component
public class ConversionServiceAdapter {
  private final ConversionService conversionService;

  public ConversionServiceAdapter(@Lazy final ConversionService conversionService) {
    this.conversionService = conversionService;
  }

  public CarDto mapCarToCarDto(final Car source) {
    return conversionService.convert(source, CarDto.class);
  }
}
