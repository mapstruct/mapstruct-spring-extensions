package org.mapstruct.extensions.spring.runtime;

import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.ConfigurableConversionService;

class ConverterRegistrationConfigurationTest {
  @Test
  void shouldAddAllConverters() {
    final var converter1 = mock(Converter.class);
    final var converter2 = mock(Converter.class);
    final var conversionService = mock(ConfigurableConversionService.class);
    final var configuration =
        new ConverterRegistrationConfiguration(conversionService, List.of(converter1, converter2));

    configuration.registerConverters();

    then(conversionService).should().addConverter(converter1);
    then(conversionService).should().addConverter(converter2);
    then(conversionService).shouldHaveNoMoreInteractions();
  }
}
