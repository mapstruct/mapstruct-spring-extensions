package org.mapstruct.extensions.spring.example.automap;

import org.mapstruct.extensions.spring.AutoMap;
import org.mapstruct.extensions.spring.AutoMapField;

@AutoMap(targetType = SeatConfiguration.class)
public class SeatConfigurationDto {

    @AutoMapField(target = "numberOfSeats")
    private int seatCount;
    @AutoMapField(target = "seatMaterial")
    private String material;

    public int getSeatCount() {
        return seatCount;
    }

    public void setSeatCount(final int seatCount) {
        this.seatCount = seatCount;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(final String material) {
        this.material = material;
    }
}
