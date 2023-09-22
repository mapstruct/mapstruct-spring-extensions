package org.mapstruct.extensions.spring.converter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

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