package uk.co.kleindelao.mapstruct.spring.example;

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

import static uk.co.kleindelao.mapstruct.spring.example.Assertions.assertThat;
import static uk.co.kleindelao.mapstruct.spring.example.CarType.OTHER;
import static uk.co.kleindelao.mapstruct.spring.example.SeatMaterial.LEATHER;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {ConversionServiceBridgeIntegrationTest.AdditionalBeanConfiguration.class})
public class ConversionServiceBridgeIntegrationTest {
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
  void shouldMapAllAttributes() {
    // Given
    final Car car =
        new Car()
            .setMake(TEST_MAKE)
            .setType(TEST_CAR_TYPE)
            .setSeatConfiguration(
                new SeatConfiguration()
                    .setSeatMaterial(TEST_SEAT_MATERIAL)
                    .setNumberOfSeats(TEST_NUMBER_OF_SEATS));

    // When
    final CarDto mappedCar = carMapper.convert(car);

    // Then
    assertThat(mappedCar).isNotNull().hasMake(TEST_MAKE).hasType(String.valueOf(TEST_CAR_TYPE));
    assertThat(mappedCar.getSeats())
        .isNotNull()
        .hasSeatCount(TEST_NUMBER_OF_SEATS)
        .hasMaterial(String.valueOf(TEST_SEAT_MATERIAL));
  }
}
