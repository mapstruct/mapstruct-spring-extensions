package org.mapstruct.extensions.spring.test;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.convert.support.DefaultConversionService;

/**
 * Registers all available {@link Converter Converters} with the injected {@link
 * ConfigurableConversionService}. A standard Spring Boot project will do this automatically, so
 * most production code will have no need to include this class explicitly. It is intended for tests
 * and situations where the application context contains more than one {@link
 * org.springframework.core.convert.ConversionService} beans.
 */
@Configuration
class ConverterRegistrationConfiguration {

  @Bean
  ConversionService basicConversionService(final List<Converter<?, ?>> converters) {
    final var conversionService = new DefaultConversionService();
    converters.forEach(conversionService::addConverter);
    return conversionService;
  }
}
