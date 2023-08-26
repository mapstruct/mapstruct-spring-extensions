package org.mapstruct.extensions.spring.converter;

import java.util.List;
import javax.annotation.Generated;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.ConfigurableConversionService;

@Generated(
        value = "org.mapstruct.extensions.spring.converter.ConverterRegistrationConfigurationGenerator",
        date = "2020-03-29T15:21:34.236Z")
@Configuration
public class ConverterRegistrationConfiguration {
  private final ConfigurableConversionService myConversionService;
  private final List<Converter<?, ?>> converters;

  public ConverterRegistrationConfiguration(
      @Qualifier("myConversionService") final ConfigurableConversionService myConversionService,
      final List<Converter<?, ?>> converters) {
    this.myConversionService = myConversionService;
    this.converters = converters;
  }
  
  @PostConstruct
  void registerConverters() {
    converters.forEach(myConversionService::addConverter);
  }
}
