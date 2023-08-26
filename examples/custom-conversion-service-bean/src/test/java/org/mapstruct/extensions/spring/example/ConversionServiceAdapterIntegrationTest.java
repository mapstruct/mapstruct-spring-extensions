package org.mapstruct.extensions.spring.example;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mapstruct.extensions.spring.example.CarType.OTHER;
import static org.mapstruct.extensions.spring.example.SeatMaterial.LEATHER;
import static org.mapstruct.extensions.spring.example.WheelPosition.RIGHT_FRONT;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.extensions.spring.example.custombean.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {ConversionServiceAdapterIntegrationTest.AdditionalBeanConfiguration.class})
public class ConversionServiceAdapterIntegrationTest {
  private static final String TEST_MAKE = "Volvo";
  private static final CarType TEST_CAR_TYPE = OTHER;
  private static final int TEST_NUMBER_OF_SEATS = 5;
  private static final SeatMaterial TEST_SEAT_MATERIAL = LEATHER;
  private static final int TEST_DIAMETER = 20;
  private static final WheelPosition TEST_WHEEL_POSITION = RIGHT_FRONT;

  @Autowired
  @Qualifier("myConversionService")
  private ConfigurableConversionService conversionService;

  @Component
  @ComponentScan(basePackageClasses = MapperSpringConfig.class)
  static class AdditionalBeanConfiguration {
    @Bean
    ConfigurableConversionService getConversionService() {
      return new DefaultConversionService();
    }

    @Bean
    ConfigurableConversionService myConversionService() {
      return new DefaultConversionService();
    }
  }

  @Test
  void shouldKnowAllMappers() {
    then(conversionService.canConvert(Car.class, CarDto.class)).isTrue();
    then(conversionService.canConvert(SeatConfiguration.class, SeatConfigurationDto.class)).isTrue();
    then(conversionService.canConvert(Wheel.class, WheelDto.class)).isTrue();
    then(conversionService.canConvert(Wheels.class, List.class)).isTrue();
    then(conversionService.canConvert(List.class, Wheels.class)).isTrue();
    then(conversionService.canConvert(
            TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(WheelDto.class)),
            TypeDescriptor.valueOf((Wheels.class))))
            .isTrue();
  }

  @Test
  void shouldMapAllAttributes() {
    // Given
    final Car car = new Car();
    car.setMake(TEST_MAKE);
    car.setType(TEST_CAR_TYPE);
    final SeatConfiguration seatConfiguration = new SeatConfiguration();
    seatConfiguration.setSeatMaterial(TEST_SEAT_MATERIAL);
    seatConfiguration.setNumberOfSeats(TEST_NUMBER_OF_SEATS);
    car.setSeatConfiguration(seatConfiguration);
    final Wheels wheels = new Wheels();
    final ArrayList<Wheel> wheelsList = new ArrayList<>();
    final Wheel wheel = new Wheel();
    wheel.setDiameter(TEST_DIAMETER);
    wheel.setPosition(TEST_WHEEL_POSITION);
    wheelsList.add(wheel);
    wheels.setWheelsList(wheelsList);
    car.setWheels(wheels);

    // When
    final CarDto mappedCar = conversionService.convert(car, CarDto.class);

    // Then
    then(mappedCar).isNotNull();
    then(mappedCar.getMake()).isEqualTo(TEST_MAKE);
    then(mappedCar.getType()).isEqualTo(String.valueOf(TEST_CAR_TYPE));
    final SeatConfigurationDto mappedCarSeats = mappedCar.getSeats();
    then(mappedCarSeats).isNotNull();
    then(mappedCarSeats.getSeatCount()).isEqualTo(TEST_NUMBER_OF_SEATS);
    then(mappedCarSeats.getMaterial()).isEqualTo(String.valueOf(TEST_SEAT_MATERIAL));
    final WheelDto expectedWheelDto = new WheelDto();
    expectedWheelDto.setPosition(String.valueOf(TEST_WHEEL_POSITION));
    expectedWheelDto.setDiameter(TEST_DIAMETER);
    then(mappedCar.getWheels()).hasSize(1).containsExactly(expectedWheelDto);
  }

  @Test
  void shouldMapGenericSourceType() {
    // Given
    final WheelDto dto = new WheelDto();
    dto.setPosition(String.valueOf(TEST_WHEEL_POSITION));
    dto.setDiameter(TEST_DIAMETER);
    final List<WheelDto> dtoList = new ArrayList<>();
    dtoList.add(dto);

    // When
    final Wheels convertedWheels = conversionService.convert(dtoList, Wheels.class);

    // Then
    final Wheel expectedWheel = new Wheel();
    expectedWheel.setPosition(TEST_WHEEL_POSITION);
    expectedWheel.setDiameter(TEST_DIAMETER);
    then(convertedWheels).isNotNull().hasSize(1).containsExactly(expectedWheel);
  }
}
