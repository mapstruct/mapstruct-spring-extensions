package org.mapstruct.extensions.spring.example.delegatingconverter;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.extensions.spring.DelegatingConverter;
import org.mapstruct.extensions.spring.example.Car;
import org.mapstruct.extensions.spring.example.CarDto;
import org.springframework.core.convert.converter.Converter;

@Mapper(config = MapperSpringConfig.class)
public interface CarMapper extends Converter<Car, CarDto> {
    @Mapping(target = "seats", source = "seatConfiguration")
    CarDto convert(Car car);

    @InheritInverseConfiguration
    @DelegatingConverter
    Car invertConvert(CarDto carDto);
}