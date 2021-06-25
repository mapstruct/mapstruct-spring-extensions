package org.mapstruct.extensions.spring.example.externalconversions;

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

import java.util.Locale;

import static java.util.Locale.GERMANY;
import static org.assertj.core.api.BDDAssertions.then;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {ConversionServiceAdapterIntegrationTest.AdditionalBeanConfiguration.class})
public class ConversionServiceAdapterIntegrationTest {
  @Autowired private LocaleInfoDtoMapper localeMapper;
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
    conversionService.addConverter(localeMapper);
  }

  @Test
  void shouldKnowAllMappers() {
    then(conversionService.canConvert(LocaleInfoDto.class, LocaleInfo.class)).isTrue();
    then(conversionService.canConvert(String.class, Locale.class)).isTrue();
  }

  @Test
  void shouldMapUsingExternalConversions() {
    // Given
    final Locale expectedLocale = GERMANY;
    final LocaleInfoDto dto = new LocaleInfoDto();
    dto.setLocale(expectedLocale.toString());

    // When
    final LocaleInfo localeInfo = conversionService.convert(dto, LocaleInfo.class);

    // Then
    then(localeInfo).isNotNull().extracting(LocaleInfo::getLocale).isEqualTo(expectedLocale);
  }
}
