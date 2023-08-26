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
 * ConfigurableConversionService}. A standard Spring Boot project will do this automatically and+
 * there should be no need to include this class explicitly. It is intended for tests and situations
 * where there is more than one {@link org.springframework.core.convert.ConversionService} bean in
 * the application context.
 */
@Configuration
public class ConverterRegistrationConfiguration {

  @Bean
  ConversionService basicConversionService(final List<Converter<?, ?>> converters) {
    final var conversionService = new DefaultConversionService();
    converters.forEach(conversionService::addConverter);
    return conversionService;
  }
}
