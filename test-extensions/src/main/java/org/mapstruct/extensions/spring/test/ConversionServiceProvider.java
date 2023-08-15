package org.mapstruct.extensions.spring.test;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.convert.support.DefaultConversionService;

@Configuration
public class ConversionServiceProvider {
    @Bean
    ConfigurableConversionService basicConversionService() {
        return new DefaultConversionService();
    }
}
