package uk.co.kleindelao.mapstruct.spring.example;

import static org.assertj.core.api.BDDAssertions.then;
import static uk.co.kleindelao.mapstruct.spring.example.CarType.OTHER;
import static uk.co.kleindelao.mapstruct.spring.example.SeatMaterial.LEATHER;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.co.kleindelao.mapstruct.spring.example.packagename.CarMapper;
import uk.co.kleindelao.mapstruct.spring.example.packagename.SeatConfigurationMapper;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {ConversionServiceAdapterIntegrationTest.AdditionalBeanConfiguration.class})
public class ConversionServiceAdapterIntegrationTest {
  private static final String TEST_MAKE = "Volvo";
  private static final CarType TEST_CAR_TYPE = OTHER;
  protected static final int TEST_NUMBER_OF_SEATS = 5;
  protected static final SeatMaterial TEST_SEAT_MATERIAL = LEATHER;

  @Autowired private CarMapper carMapper;
  @Autowired private SeatConfigurationMapper seatConfigurationMapper;
  @Autowired private ConfigurableConversionService conversionService;

  @ComponentScan("uk.co.kleindelao.mapstruct.spring")
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
    then(conversionService.canConvert(Car.class, CarDto.class)).isTrue();
    then(conversionService.canConvert(SeatConfiguration.class, SeatConfigurationDto.class))
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
  }
}
