package uk.co.kleindelao.mapstruct.spring.converter;

import com.squareup.javapoet.ClassName;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public class ConversionServiceBridgeDescriptor {
    private ClassName bridgeClassName;
    private List<Pair<ClassName, ClassName>> fromToMappings;

    public ClassName getBridgeClassName() {
        return bridgeClassName;
    }

    public void setBridgeClassName(final ClassName bridgeClassName) {
        this.bridgeClassName = bridgeClassName;
    }

    public List<Pair<ClassName, ClassName>> getFromToMappings() {
        return fromToMappings;
    }

    public void setFromToMappings(final List<Pair<ClassName, ClassName>> fromToMappings) {
        this.fromToMappings = fromToMappings;
    }
}
