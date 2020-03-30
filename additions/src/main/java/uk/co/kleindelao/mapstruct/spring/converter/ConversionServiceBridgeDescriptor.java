package uk.co.kleindelao.mapstruct.spring.converter;

import com.squareup.javapoet.ClassName;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

@Value
@Builder
public class ConversionServiceBridgeDescriptor {
    ClassName bridgeClassName;
    @Singular
    List<Pair<ClassName, ClassName>> fromToMappings;
}
