package org.mapstruct.extensions.spring.test;

import java.util.List;
import javax.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.ConfigurableConversionService;

@Configuration
class ConversionServiceTestConfiguration {
  private final ConfigurableConversionService conversionService;
  private final List<Converter<?, ?>> converters;

  ConversionServiceTestConfiguration(
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
