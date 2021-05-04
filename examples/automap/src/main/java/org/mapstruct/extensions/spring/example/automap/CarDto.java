package org.mapstruct.extensions.spring.example.automap;

import org.mapstruct.extensions.spring.AutoMap;
import org.mapstruct.extensions.spring.AutoMapField;

import java.util.List;

@AutoMap(targetType = Car.class, uses = {
        WheelsMapper.class
})
public class CarDto {
    private String make;
    @AutoMapField(target = "seatConfiguration")
    private SeatConfigurationDto seats;
    private String type;
    private List<WheelDto> wheels;

    public String getMake() {
        return make;
    }

    public void setMake(final String make) {
        this.make = make;
    }

    public SeatConfigurationDto getSeats() {
        return seats;
    }

    public void setSeats(final SeatConfigurationDto seats) {
        this.seats = seats;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public List<WheelDto> getWheels() {
        return wheels;
    }

    public void setWheels(List<WheelDto> wheels) {
        this.wheels = wheels;
    }

}
