package org.mapstruct.extensions.spring.converter;

import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Generated(
        value = "org.mapstruct.extensions.spring.converter.DelegatingConverterGenerator",
        date = "2020-03-29T15:21:34.236Z")
@Component
public class CarDtoToCarConverter implements Converter<CarDto, Car> {

    private CarMapper delegateMapper;

    public CarDtoToCarConverter(@Autowired final CarMapper delegateMapper) {
        this.delegateMapper = delegateMapper;
    }

    @Override
    public Car convert(final CarDto source) {
        return delegateMapper.convertInverse(source);
    }
}