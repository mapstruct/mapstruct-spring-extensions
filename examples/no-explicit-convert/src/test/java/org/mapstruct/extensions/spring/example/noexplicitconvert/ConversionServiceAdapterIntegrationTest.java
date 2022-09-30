package org.mapstruct.extensions.spring.example.noexplicitconvert;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.extensions.spring.converter.ConversionServiceAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.BDDAssertions.then;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {ConversionServiceAdapterIntegrationTest.AdditionalBeanConfiguration.class})
public class ConversionServiceAdapterIntegrationTest {
  private static final int TEST_ORIGIN_X = 5;
  private static final int TEST_ORIGIN_Y = 20;
  private static final int TEST_DESTINATION_X = 30;
  private static final int TEST_DESTINATION_Y = 23;

  @Autowired private SimplePointMapper pointMapper;
  @Autowired private SimpleLineMapper lineMapper;
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
    conversionService.addConverter(pointMapper);
    conversionService.addConverter(lineMapper);
  }

  @Test
  void shouldKnowAllMappers() {
    then(conversionService.canConvert(SimpleLine.class, LineDto.class)).isTrue();
    then(conversionService.canConvert(SimplePoint.class, PointDto.class)).isTrue();
  }

  @Test
  void shouldMapAllAttributes() {
    // Given
    final SimplePoint origin = origin();
    final SimplePoint destination = destination();
    final SimpleLine sourceLine = simpleLine(origin, destination);

    // When
    final LineDto mappedLine = conversionService.convert(sourceLine, LineDto.class);

    // Then
    then(mappedLine).isNotNull();
    then(mappedLine.getOrigin().getX()).isEqualTo(TEST_ORIGIN_X);
    then(mappedLine.getOrigin().getY()).isEqualTo(TEST_ORIGIN_Y);
    then(mappedLine.getDestination().getX()).isEqualTo(TEST_DESTINATION_X);
    then(mappedLine.getDestination().getY()).isEqualTo(TEST_DESTINATION_Y);
  }

  private static SimpleLine simpleLine(SimplePoint origin, SimplePoint destination) {
    final SimpleLine sourceLine = new SimpleLine();
    sourceLine.setOrigin(origin);
    sourceLine.setDestination(destination);
    return sourceLine;
  }

  private static SimplePoint destination() {
    return simplePoint(TEST_DESTINATION_X, TEST_DESTINATION_Y);
  }

  private static SimplePoint origin() {
    return simplePoint(TEST_ORIGIN_X, TEST_ORIGIN_Y);
  }

  private static SimplePoint simplePoint(int testOriginX, int testOriginY) {
    final SimplePoint origin = new SimplePoint();
    origin.setX(testOriginX);
    origin.setY(testOriginY);
    return origin;
  }

  @Test
  void shouldHaveMethodsForAllConverters() {
    // Given
    final var conversionServiceAdapter = new ConversionServiceAdapter(conversionService);
    final var origin = origin();
    final var destination = destination();
    final var sourceLine = simpleLine(origin, destination);

    // When
    final var pointDto = conversionServiceAdapter.mapSimplePointToPointDto(simplePoint(TEST_ORIGIN_X, TEST_DESTINATION_Y));
    final var lineDto = conversionServiceAdapter.mapSimpleLineToLineDto(sourceLine);

    // Then
    then(pointDto).isNotNull();
    then(pointDto.getX()).isEqualTo(TEST_ORIGIN_X);
    then(pointDto.getY()).isEqualTo(TEST_DESTINATION_Y);
    then(lineDto).isNotNull();
    then(lineDto.getOrigin().getX()).isEqualTo(TEST_ORIGIN_X);
    then(lineDto.getOrigin().getY()).isEqualTo(TEST_ORIGIN_Y);
    then(lineDto.getDestination().getX()).isEqualTo(TEST_DESTINATION_X);
    then(lineDto.getDestination().getY()).isEqualTo(TEST_DESTINATION_Y);
  }
}
