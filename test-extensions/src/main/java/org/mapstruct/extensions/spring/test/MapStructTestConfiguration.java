package org.mapstruct.extensions.spring.test;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;

@Configuration
public class MapStructTestConfiguration {
    @Bean
    ConversionService basicConversionService(final List<Converter<?,?>> converters) {
        var conversionService = new DefaultConversionService();
        converters.forEach(conversionService::addConverter);

        return conversionService;
    }
}
