package uk.co.kleindelao.mapstruct.spring.converter;

import com.squareup.javapoet.ClassName;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public class ConversionServiceAdapterDescriptor {
    private ClassName adapterClassName;
    private List<Pair<ClassName, ClassName>> fromToMappings;

    public ClassName getAdapterClassName() {
        return adapterClassName;
    }

    public void setAdapterClassName(final ClassName adapterClassName) {
        this.adapterClassName = adapterClassName;
    }

    public List<Pair<ClassName, ClassName>> getFromToMappings() {
        return fromToMappings;
    }

    public void setFromToMappings(final List<Pair<ClassName, ClassName>> fromToMappings) {
        this.fromToMappings = fromToMappings;
    }
}
