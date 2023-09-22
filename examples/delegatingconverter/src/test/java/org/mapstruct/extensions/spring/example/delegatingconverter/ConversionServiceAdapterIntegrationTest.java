package org.mapstruct.extensions.spring.example.delegatingconverter;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mapstruct.extensions.spring.example.CarType.OTHER;
import static org.mapstruct.extensions.spring.example.SeatMaterial.LEATHER;
import static org.mapstruct.extensions.spring.example.WheelPosition.RIGHT_FRONT;

import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.extensions.spring.example.*;
import org.mapstruct.extensions.spring.test.ConverterScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class ConversionServiceAdapterIntegrationTest {
  private static final String TEST_MAKE = "Volvo";
  private static final CarType TEST_CAR_TYPE = OTHER;
  private static final int TEST_NUMBER_OF_SEATS = 5;
  private static final SeatMaterial TEST_SEAT_MATERIAL = LEATHER;
  private static final int TEST_DIAMETER = 20;
  private static final WheelPosition TEST_WHEEL_POSITION = RIGHT_FRONT;

  @Autowired private ConversionService conversionService;

  @Configuration
  @ConverterScan(basePackageClasses = MapperSpringConfig.class)
  static class ScanConfiguration{}

  @Test
  void shouldKnowDelegatingMappers() {
    then(conversionService.canConvert(CarDto.class, Car.class)).isTrue();
    then(conversionService.canConvert(SeatConfigurationDto.class, SeatConfiguration.class))
        .isTrue();
    then(conversionService.canConvert(WheelDto.class, Wheel.class)).isTrue();
  }

  @Test
  void shouldMapAllAttributes() {
    // Given
    final var carDto = new CarDto();
    carDto.setMake(TEST_MAKE);
    carDto.setType(TEST_CAR_TYPE.name());
    final var seatConfigurationDto = new SeatConfigurationDto();
    seatConfigurationDto.setMaterial(TEST_SEAT_MATERIAL.name());
    seatConfigurationDto.setSeatCount(TEST_NUMBER_OF_SEATS);
    carDto.setSeats(seatConfigurationDto);
    final var wheelsList = new ArrayList<WheelDto>();
    final var wheel = new WheelDto();
    wheel.setDiameter(TEST_DIAMETER);
    wheel.setPosition(TEST_WHEEL_POSITION.name());
    wheelsList.add(wheel);
    carDto.setWheels(wheelsList);

    // When
    final var mappedCar = conversionService.convert(carDto, Car.class);

    // Then
    then(mappedCar).isNotNull();
    then(mappedCar.getMake()).isEqualTo(TEST_MAKE);
    then(mappedCar.getType()).isEqualTo(TEST_CAR_TYPE);
    final var mappedCarSeatConfiguration = mappedCar.getSeatConfiguration();
    then(mappedCarSeatConfiguration).isNotNull();
    then(mappedCarSeatConfiguration.getNumberOfSeats()).isEqualTo(TEST_NUMBER_OF_SEATS);
    then(mappedCarSeatConfiguration.getSeatMaterial()).isEqualTo(TEST_SEAT_MATERIAL);
    final var expectedWheel = new Wheel();
    expectedWheel.setPosition(TEST_WHEEL_POSITION);
    expectedWheel.setDiameter(TEST_DIAMETER);
    then(mappedCar.getWheels()).hasSize(1).containsExactly(expectedWheel);
  }
}
