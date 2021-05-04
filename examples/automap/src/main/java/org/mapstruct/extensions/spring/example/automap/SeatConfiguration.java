package org.mapstruct.extensions.spring.example.automap;

public class SeatConfiguration {
    private int numberOfSeats;
    private SeatMaterial seatMaterial;

    public SeatMaterial getSeatMaterial() {
        return seatMaterial;
    }

    public void setSeatMaterial(final SeatMaterial seatMaterial) {
        this.seatMaterial = seatMaterial;
    }

    public int getNumberOfSeats() {
        return numberOfSeats;
    }

    public void setNumberOfSeats(final int numberOfSeats) {
        this.numberOfSeats = numberOfSeats;
    }
}
