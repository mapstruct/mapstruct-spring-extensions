package uk.co.kleindelao.mapstruct.spring.example;

import lombok.Data;

@Data
public class Car {
    private String make;
    private int numberOfSeats;
    private CarType type;
}
