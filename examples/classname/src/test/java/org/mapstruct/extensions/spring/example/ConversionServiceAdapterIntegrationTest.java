package org.mapstruct.extensions.spring.example;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mapstruct.extensions.spring.example.CarType.OTHER;
import static org.mapstruct.extensions.spring.example.SeatMaterial.LEATHER;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.extensions.spring.example.classname.CarMapper;
import org.mapstruct.extensions.spring.example.classname.SeatConfigurationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
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
  private static final org.mapstruct.extensions.spring.example.CarType TEST_CAR_TYPE = OTHER;
  protected static final int TEST_NUMBER_OF_SEATS = 5;
  protected static final org.mapstruct.extensions.spring.example.SeatMaterial TEST_SEAT_MATERIAL = LEATHER;

  @Autowired private CarMapper carMapper;
  @Autowired private SeatConfigurationMapper seatConfigurationMapper;
  @Autowired private ConfigurableConversionService conversionService;

  @ComponentScan("org.mapstruct.extensions.spring")
  @Component
  static class AdditionalBeanConfiguration {
    @Bean
    ConfigurableConversionService getConversionService() {
      return new DefaultConversionService();
    }
  }

  @BeforeEach
  void addMappersToConversionService() {
    conversionService.addConverter(carMapper);
    conversionService.addConverter(seatConfigurationMapper);
  }

  @Test
  void shouldKnowAllMappers() {
    then(conversionService.canConvert(org.mapstruct.extensions.spring.example.Car.class, org.mapstruct.extensions.spring.example.CarDto.class)).isTrue();
    then(conversionService.canConvert(org.mapstruct.extensions.spring.example.SeatConfiguration.class, org.mapstruct.extensions.spring.example.SeatConfigurationDto.class))
        .isTrue();
  }

  @Test
  void shouldMapAllAttributes() {
    // Given
    final org.mapstruct.extensions.spring.example.Car car = new org.mapstruct.extensions.spring.example.Car();
    car.setMake(TEST_MAKE);
    car.setType(TEST_CAR_TYPE);
    final org.mapstruct.extensions.spring.example.SeatConfiguration seatConfiguration = new org.mapstruct.extensions.spring.example.SeatConfiguration();
    seatConfiguration.setSeatMaterial(TEST_SEAT_MATERIAL);
    seatConfiguration.setNumberOfSeats(TEST_NUMBER_OF_SEATS);
    car.setSeatConfiguration(seatConfiguration);

    // When
    final org.mapstruct.extensions.spring.example.CarDto mappedCar = conversionService.convert(car, org.mapstruct.extensions.spring.example.CarDto.class);

    // Then
    then(mappedCar).isNotNull();
    then(mappedCar.getMake()).isEqualTo(TEST_MAKE);
    then(mappedCar.getType()).isEqualTo(String.valueOf(TEST_CAR_TYPE));
    final org.mapstruct.extensions.spring.example.SeatConfigurationDto mappedCarSeats = mappedCar.getSeats();
    then(mappedCarSeats).isNotNull();
    then(mappedCarSeats.getSeatCount()).isEqualTo(TEST_NUMBER_OF_SEATS);
    then(mappedCarSeats.getMaterial()).isEqualTo(String.valueOf(TEST_SEAT_MATERIAL));
  }
}
