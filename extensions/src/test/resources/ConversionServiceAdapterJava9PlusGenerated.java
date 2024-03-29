package org.mapstruct.extensions.spring.converter;

import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.stereotype.Component;
import test.Car;
import test.CarDto;

@Generated(
    value = "org.mapstruct.extensions.spring.converter.ConversionServiceAdapterGenerator",
    date = "2020-03-29T15:21:34.236Z")
@Component
public class ConversionServiceAdapter {
  private final ConversionService conversionService;

  public ConversionServiceAdapter(@Lazy final ConversionService conversionService) {
    this.conversionService = conversionService;
  }

  public CarDto toDto(final Car source) {
    return (CarDto) conversionService.convert(source, TypeDescriptor.valueOf(Car.class), TypeDescriptor.valueOf(CarDto.class));
  }

  public List<CarDto> mapListOfCarToListOfCarDto(final List<Car> source) {
    return (List<CarDto>) conversionService.convert(source, TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(Car.class)), TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(CarDto.class)));
  }
}
