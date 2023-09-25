package org.mapstruct.extensions.spring.example.custombeanwithconverterscan;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.core.convert.converter.Converter;
import org.mapstruct.extensions.spring.example.Car;
import org.mapstruct.extensions.spring.example.CarDto;

@Mapper(config = MapperSpringConfig.class)
public interface CarMapper extends Converter<Car, CarDto> {
    @Mapping(target = "seats", source = "seatConfiguration")
    CarDto convert(Car car);
}
