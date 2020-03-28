package uk.co.kleindelao.mapstruct.spring.example;

import lombok.Data;

@Data
public class CarDto {
    private String make;
    private int seatCount;
    private String type;
}
