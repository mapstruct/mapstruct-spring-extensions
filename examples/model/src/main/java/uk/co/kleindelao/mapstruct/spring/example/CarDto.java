package uk.co.kleindelao.mapstruct.spring.example;

public class CarDto {
    private String make;
    private SeatConfigurationDto seats;
    private String type;

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
}
