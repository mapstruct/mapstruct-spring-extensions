package org.mapstruct.extensions.spring.example.noexplicitconvert;

public class SimpleLine {
    private SimplePoint origin;
    private SimplePoint destination;

    public SimplePoint getOrigin() {
        return origin;
    }

    public void setOrigin(SimplePoint origin) {
        this.origin = origin;
    }

    public SimplePoint getDestination() {
        return destination;
    }

    public void setDestination(SimplePoint destination) {
        this.destination = destination;
    }
}
