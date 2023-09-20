package org.mapstruct.extensions.spring.converter;

import javax.annotation.Generated;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;

@Generated("org.mapstruct.extensions.spring.converter.DelegatingConverterGenerator")
@Mapper(config = MapperSpringConfig.class)
public abstract class CarDtoToCarConverter implements Converter<CarDto, Car> {

    @Autowired
    private CarMapper delegateMapper;

    @Override
    public Car convert(final CarDto source) {
        return delegateMapper.convertInverse(source);
    }
}