package org.mapstruct.extensions.spring.example.externalconversions;

import static java.util.Locale.GERMANY;
import static org.assertj.core.api.BDDAssertions.then;

import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.extensions.spring.test.ConverterRegistrationConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {
      ConverterRegistrationConfiguration.class,
      LocaleInfoDtoMapperImpl.class,
      ConversionServiceAdapter.class
    })
public class ConversionServiceAdapterIntegrationTest {
  @Autowired private ConversionService conversionService;

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
