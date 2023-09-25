package org.mapstruct.extensions.spring.example.boot.config;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;

@Configuration
public class ConverterConfig {
  private final ListableBeanFactory beanFactory;

  public ConverterConfig(ListableBeanFactory beanFactory) {
    this.beanFactory = beanFactory;
  }

  /**
   * Without spring-boot-starter-web, no custom mappers will be added automatically; Therefore we
   * register all our {@link org.springframework.core.convert.converter.Converter Converters}
   * manually.
   */
  @Bean
  public ConversionService conversionService() {
    LoggerFactory.getLogger(ConverterConfig.class).info("ConversionService bean init");
    final FormattingConversionService service = new DefaultFormattingConversionService();
    ApplicationConversionService.addBeans(service, this.beanFactory);
    return service;
  }
}
