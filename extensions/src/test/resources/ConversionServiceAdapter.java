package org.mapstruct.extensions.spring.converter;

import javax.annotation.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;
import test.Car;
import test.CarDto;

@Generated(
        value = "org.mapstruct.extensions.spring.converter.ConversionServiceAdapterGenerator",
        date = "2020-03-29T15:21:34.236Z"
)
@Component
public class ConversionServiceAdapter {
    @Autowired
    private ConversionService conversionService;

    public CarDto mapCarToCarDto(final Car source) {
        return conversionService.convert(source, CarDto.class);
    }
}
