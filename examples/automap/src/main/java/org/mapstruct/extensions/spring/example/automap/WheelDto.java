package org.mapstruct.extensions.spring.example.automap;

import org.mapstruct.extensions.spring.AutoMap;

import java.util.Objects;

@AutoMap(targetType = Wheel.class)
public class WheelDto {
    private String position;
    private int diameter;

    public void setDiameter(int diameter) {
        this.diameter = diameter;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public int getDiameter() {
        return diameter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WheelDto wheelDto = (WheelDto) o;
        return diameter == wheelDto.diameter && Objects.equals(position, wheelDto.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, diameter);
    }
}
