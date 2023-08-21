package org.mapstruct.extensions.spring.runtime;

import java.util.List;
import javax.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.ConfigurableConversionService;

/**
 * Registers all available {@link Converter Converters} with the injected {@link
 * ConfigurableConversionService}. Please note that a standard Spring Boot project will do this
 * automatically and there should be no need to include this class explicitly. It is intended for
 * tests and situations where there is more than one {@link
 * org.springframework.core.convert.ConversionService} bean in the application context.
 */
@Configuration
public class ConverterRegistrationConfiguration {
  // TODO: Create a generator for a ConverterScan annotation to be placed alongside the adapter
  // class.
  private final ConfigurableConversionService conversionService;
  private final List<Converter<?, ?>> converters;

  ConverterRegistrationConfiguration(
      final ConfigurableConversionService conversionService,
      final List<Converter<?, ?>> converters) {
    this.conversionService = conversionService;
    this.converters = converters;
  }

  @PostConstruct
  void registerConverters() {
    converters.forEach(conversionService::addConverter);
  }
}
