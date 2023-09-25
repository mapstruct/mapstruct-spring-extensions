package org.mapstruct.extensions.spring.example.noexplicitconvert;

public class LineDto {
    private PointDto origin;
    private PointDto destination;

    public PointDto getOrigin() {
        return origin;
    }

    public void setOrigin(PointDto origin) {
        this.origin = origin;
    }

    public PointDto getDestination() {
        return destination;
    }

    public void setDestination(PointDto destination) {
        this.destination = destination;
    }
}
