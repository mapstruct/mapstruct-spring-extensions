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
  private static final TypeDescriptor TYPE_DESCRIPTOR_TEST_CAR = TypeDescriptor.valueOf(Car.class);

  private static final TypeDescriptor TYPE_DESCRIPTOR_TEST_CARDTO = TypeDescriptor.valueOf(CarDto.class);

  private static final TypeDescriptor TYPE_DESCRIPTOR_JAVA_UTIL_LIST_TEST_CAR = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(Car.class));

  private static final TypeDescriptor TYPE_DESCRIPTOR_JAVA_UTIL_LIST_TEST_CARDTO = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(CarDto.class));

  private final ConversionService conversionService;

  public ConversionServiceAdapter(
          @Lazy final ConversionService conversionService) {
    this.conversionService = conversionService;
  }

  public CarDto toDto(final Car source) {
    return (CarDto) conversionService.convert(source, TYPE_DESCRIPTOR_TEST_CAR, TYPE_DESCRIPTOR_TEST_CARDTO);
  }

  public List<CarDto> mapListOfCarToListOfCarDto(final List<Car> source) {
    return (List<CarDto>) conversionService.convert(source, TYPE_DESCRIPTOR_JAVA_UTIL_LIST_TEST_CAR, TYPE_DESCRIPTOR_JAVA_UTIL_LIST_TEST_CARDTO);
  }
}
