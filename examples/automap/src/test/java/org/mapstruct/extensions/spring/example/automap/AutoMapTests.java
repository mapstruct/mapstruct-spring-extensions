package org.mapstruct.extensions.spring.example.automap;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.extensions.spring.IObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mapstruct.extensions.spring.example.automap.CarType.OTHER;
import static org.mapstruct.extensions.spring.example.automap.SeatMaterial.LEATHER;
import static org.mapstruct.extensions.spring.example.automap.WheelPosition.RIGHT_FRONT;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {AutoMapTests.AutoMapTestConfiguration.class})
public class AutoMapTests {

    private static final String TEST_MAKE = "Volvo";
    private static final CarType TEST_CAR_TYPE = OTHER;
    private static final int TEST_NUMBER_OF_SEATS = 5;
    private static final SeatMaterial TEST_SEAT_MATERIAL = LEATHER;
    private static final int TEST_DIAMETER = 20;
    private static final WheelPosition TEST_WHEEL_POSITION = RIGHT_FRONT;
    @Autowired
    private IObjectMapper mapper;

    @ComponentScan("org.mapstruct.extensions.spring")
    @Component
    static class AutoMapTestConfiguration {
    }

    @Test
    public void shouldMap(){

        // Given
        final Car car = new Car();
        car.setMake(TEST_MAKE);
        car.setType(TEST_CAR_TYPE);
        final SeatConfiguration seatConfiguration = new SeatConfiguration();
        seatConfiguration.setSeatMaterial(TEST_SEAT_MATERIAL);
        seatConfiguration.setNumberOfSeats(TEST_NUMBER_OF_SEATS);
        car.setSeatConfiguration(seatConfiguration);
        final Wheels wheels = new Wheels();
       final ArrayList<Wheel> wheelsList = new ArrayList<>();
       final Wheel wheel = new Wheel();
       wheel.setDiameter(TEST_DIAMETER);
        wheel.setPosition(TEST_WHEEL_POSITION);
        wheelsList.add(wheel);
        wheels.setWheelsList(wheelsList);
        car.setWheels(wheels);

        // When
        final CarDto mappedCar = mapper.map(car, CarDto.class);

        // Then
        then(mappedCar).isNotNull();
        then(mappedCar.getMake()).isEqualTo(TEST_MAKE);
        then(mappedCar.getType()).isEqualTo(String.valueOf(TEST_CAR_TYPE));
        final SeatConfigurationDto mappedCarSeats = mappedCar.getSeats();
        then(mappedCarSeats).isNotNull();
        then(mappedCarSeats.getSeatCount()).isEqualTo(TEST_NUMBER_OF_SEATS);
        then(mappedCarSeats.getMaterial()).isEqualTo(String.valueOf(TEST_SEAT_MATERIAL));
        final WheelDto expectedWheelDto = new WheelDto();
        expectedWheelDto.setPosition(String.valueOf(TEST_WHEEL_POSITION));
        expectedWheelDto.setDiameter(TEST_DIAMETER);
        then(mappedCar.getWheels()).hasSize(1).containsExactly(expectedWheelDto);
    }
}
