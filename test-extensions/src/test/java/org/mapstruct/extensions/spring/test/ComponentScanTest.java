package org.mapstruct.extensions.spring.test;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mapstruct.extensions.spring.example.CarType.SPORTS;
import static org.mapstruct.extensions.spring.example.SeatMaterial.LEATHER;
import static org.mapstruct.extensions.spring.example.WheelPosition.*;

import java.util.List;
import net.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.Test;
import org.mapstruct.extensions.spring.converter.ConversionServiceAdapter;
import org.mapstruct.extensions.spring.example.*;
import org.mapstruct.extensions.spring.example.noconfig.MapperSpringConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.convert.ConversionService;

@SpringBootTest(classes = ConversionServiceAdapter.class)
@MapStructConverterScan(basePackageClasses = MapperSpringConfig.class)
class ComponentScanTest {
  private static final int TEST_WHEEL_DIAMETER = 18;
  @Autowired private ConversionService conversionService;

  @Test
  void shouldMapCar() {
    final var car = new Car();
    final var make = RandomString.make();
    car.setMake(make);
    final var type = SPORTS;
    car.setType(type);
    final var seatConfiguration = new SeatConfiguration();
    final var seatMaterial = LEATHER;
    seatConfiguration.setSeatMaterial(seatMaterial);
    final var numberOfSeats = 2;
    seatConfiguration.setNumberOfSeats(numberOfSeats);
    car.setSeatConfiguration(seatConfiguration);
    final var wheels = new Wheels();
    final var testWheels = List.of(
            createWheel(LEFT_FRONT),
            createWheel(RIGHT_FRONT),
            createWheel(LEFT_REAR),
            createWheel(RIGHT_REAR));
    wheels.setWheelsList(testWheels);
    car.setWheels(wheels);
    
    final var carDto = conversionService.convert(car, CarDto.class);

    then(carDto).isNotNull();
    then(carDto.getMake()).isEqualTo(make);
    then(carDto.getType()).isEqualTo(type.name());
    then(carDto.getSeats().getSeatCount()).isEqualTo(numberOfSeats);
    then(carDto.getSeats().getMaterial()).isEqualTo(seatMaterial.name());
    then(carDto.getWheels()).hasSameSizeAs(testWheels);
  }

  private static Wheel createWheel(WheelPosition position) {
    final var wheel = new Wheel();
    wheel.setDiameter(TEST_WHEEL_DIAMETER);
    wheel.setPosition(position);
    return wheel;
  }
}
