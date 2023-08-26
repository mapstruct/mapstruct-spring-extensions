package org.mapstruct.extensions.spring.test;

import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;

import java.util.List;
import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;

class ConverterRegistrationConfigurationTest {
  @Test
  void shouldAddAllConverters() {
    final var converter1 = mock(Converter.class);
    final var converter2 = mock(Converter.class);
    final var configuration = new ConverterRegistrationConfiguration();

    try (final var mockedConstruction = mockConstruction(DefaultConversionService.class)) {
      final var conversionService =
          configuration.basicConversionService(List.of(converter1, converter2));

      BDDAssertions.then(conversionService).isInstanceOf(DefaultConversionService.class);
      BDDAssertions.then(mockedConstruction.constructed())
          .containsExactly((DefaultConversionService) conversionService);
      final var constructedService = mockedConstruction.constructed().iterator().next();
      then(constructedService).should().addConverter(converter1);
      then(constructedService).should().addConverter(converter2);
      then(constructedService).shouldHaveNoMoreInteractions();
    }
  }
}
