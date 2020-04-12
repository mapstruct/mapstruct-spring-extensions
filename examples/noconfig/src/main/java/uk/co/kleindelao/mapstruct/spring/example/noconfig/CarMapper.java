package uk.co.kleindelao.mapstruct.spring.example.noconfig;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.core.convert.converter.Converter;
import uk.co.kleindelao.mapstruct.spring.example.Car;
import uk.co.kleindelao.mapstruct.spring.example.CarDto;

@Mapper(config = MapperSpringConfig.class)
public interface CarMapper extends Converter<Car, CarDto> {
    @Mapping(target = "seats", source = "seatConfiguration")
    CarDto convert(Car car);
}
