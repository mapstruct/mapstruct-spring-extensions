package org.mapstruct.extensions.spring.example.automap;

import org.mapstruct.Mapper;
import org.mapstruct.extensions.spring.BaseAutoMapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WheelsMapper extends BaseAutoMapper<List<WheelDto>,Wheels> {

}
