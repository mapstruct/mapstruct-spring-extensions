package org.mapstruct.extensions.spring.converter;

import java.util.List;
import javax.annotation.Generated;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.stereotype.Component;
import test.Car;
import test.CarDto;

@Generated("org.mapstruct.extensions.spring.converter.ConversionServiceAdapterGenerator")
@Component
public class ConversionServiceAdapter {
  private final ConversionService conversionService;

  public ConversionServiceAdapter(@Lazy final ConversionService conversionService) {
    this.conversionService = conversionService;
  }

  public CarDto mapCarToCarDto(final Car source) {
    return (CarDto) conversionService.convert(source, TypeDescriptor.valueOf(Car.class), TypeDescriptor.valueOf(CarDto.class));
  }

  public List<CarDto> mapListOfCarToListOfCarDto(final List<Car> source) {
    return (List<CarDto>) conversionService.convert(source, TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(Car.class)), TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(CarDto.class)));
  }
}
