package org.mapstruct.extensions.spring.converter;

import com.squareup.javapoet.ClassName;
import java.util.List;

import com.squareup.javapoet.TypeName;
import org.apache.commons.lang3.tuple.Pair;

public class ConversionServiceAdapterDescriptor {
    private ClassName adapterClassName;
    private String conversionServiceBeanName;
    private List<Pair<TypeName, TypeName>> fromToMappings;
    private boolean lazyAnnotatedConversionServiceBean;

    public ClassName getAdapterClassName() {
        return adapterClassName;
    }

    public void setAdapterClassName(final ClassName adapterClassName) {
        this.adapterClassName = adapterClassName;
    }

    public String getConversionServiceBeanName() {
        return conversionServiceBeanName;
    }

    public void setConversionServiceBeanName(String conversionServiceBeanName) {
        this.conversionServiceBeanName = conversionServiceBeanName;
    }

    public List<Pair<TypeName, TypeName>> getFromToMappings() {
        return fromToMappings;
    }

    public void setFromToMappings(final List<Pair<TypeName, TypeName>> fromToMappings) {
        this.fromToMappings = fromToMappings;
    }

    public boolean isLazyAnnotatedConversionServiceBean() {
        return lazyAnnotatedConversionServiceBean;
    }

    public void setLazyAnnotatedConversionServiceBean(boolean lazyAnnotatedConversionServiceBean) {
        this.lazyAnnotatedConversionServiceBean = lazyAnnotatedConversionServiceBean;
    }
}
