package uk.co.kleindelao.mapstruct.spring.example;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.core.convert.converter.Converter;

@Mapper(config = MapperSpringConfig.class)
public interface CarMapper extends Converter<Car, CarDto> {
    @Mapping(source = "numberOfSeats", target = "seatCount")
    CarDto convert(Car car);
}
