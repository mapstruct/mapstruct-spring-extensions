package org.mapstruct.extensions.spring.example.boot;

import org.mapstruct.extensions.spring.converter.ConversionServiceAdapter;
import org.mapstruct.extensions.spring.example.Car;
import org.mapstruct.extensions.spring.example.CarDto;
import org.mapstruct.extensions.spring.example.SeatConfiguration;
import org.mapstruct.extensions.spring.example.SeatMaterial;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Start {
    public static void main(String[] args) {
        SpringApplication.run(Start.class, args);
    }

    private static final Logger log = LoggerFactory.getLogger(Start.class);

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx, ConversionServiceAdapter adapter) {
        return args -> {
            final SeatConfiguration seatConf = new SeatConfiguration();
            seatConf.setSeatMaterial(SeatMaterial.FABRIC);
            final Car car = new Car();
            car.setMake("make");
            car.setSeatConfiguration(seatConf);

            log.info("map start");
            final CarDto carDto = adapter.mapCarToCarDto(car);
            log.info(carDto.toString());
        };
    }
}
