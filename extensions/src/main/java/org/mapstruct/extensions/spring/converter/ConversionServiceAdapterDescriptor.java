package org.mapstruct.extensions.spring.converter;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public class ConversionServiceAdapterDescriptor {
  private ClassName adapterClassName;
  private String conversionServiceBeanName;
  private List<Pair<TypeName, TypeName>> fromToMappings;
  private boolean lazyAnnotatedConversionServiceBean;

  public ClassName getAdapterClassName() {
    return adapterClassName;
  }

  public ConversionServiceAdapterDescriptor adapterClassName(final ClassName adapterClassName) {
    this.adapterClassName = adapterClassName;
    return this;
  }

  public String getConversionServiceBeanName() {
    return conversionServiceBeanName;
  }

  public ConversionServiceAdapterDescriptor conversionServiceBeanName(
      final String conversionServiceBeanName) {
    this.conversionServiceBeanName = conversionServiceBeanName;
    return this;
  }

  public List<Pair<TypeName, TypeName>> getFromToMappings() {
    return fromToMappings;
  }

  public ConversionServiceAdapterDescriptor fromToMappings(
      final List<Pair<TypeName, TypeName>> fromToMappings) {
    this.fromToMappings = fromToMappings;
    return this;
  }

  public boolean isLazyAnnotatedConversionServiceBean() {
    return lazyAnnotatedConversionServiceBean;
  }

  public ConversionServiceAdapterDescriptor lazyAnnotatedConversionServiceBean(
      final boolean lazyAnnotatedConversionServiceBean) {
    this.lazyAnnotatedConversionServiceBean = lazyAnnotatedConversionServiceBean;
    return this;
  }
}
