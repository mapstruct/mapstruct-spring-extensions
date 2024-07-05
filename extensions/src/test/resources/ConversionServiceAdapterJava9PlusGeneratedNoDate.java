package org.mapstruct.extensions.spring.converter;

import java.util.List;
import javax.annotation.processing.Generated;
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

  private final TypeDescriptor typeDescriptor_test_Car = TypeDescriptor.valueOf(Car.class);

  private final TypeDescriptor typeDescriptor_test_CarDto = TypeDescriptor.valueOf(CarDto.class);

  private final TypeDescriptor typeDescriptor_java_util_List_test_Car_ = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(Car.class));

  private final TypeDescriptor typeDescriptor_java_util_List_test_CarDto_ = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(CarDto.class));

  public ConversionServiceAdapter(
          @Lazy final ConversionService conversionService) {
    this.conversionService = conversionService;
  }

  public CarDto toDto(final Car source) {
    return (CarDto) conversionService.convert(source, typeDescriptor_test_Car, typeDescriptor_test_CarDto);
  }

  public List<CarDto> mapListOfCarToListOfCarDto(final List<Car> source) {
    return (List<CarDto>) conversionService.convert(source, typeDescriptor_java_util_List_test_Car_, typeDescriptor_java_util_List_test_CarDto_);
  }
}
