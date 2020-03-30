package uk.co.kleindelao.mapstruct.spring.example;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SeatConfiguration {
    private int numberOfSeats;
    private SeatMaterial seatMaterial;
}
