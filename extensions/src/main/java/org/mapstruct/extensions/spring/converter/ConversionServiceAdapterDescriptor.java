package org.mapstruct.extensions.spring.converter;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import org.apache.commons.lang3.tuple.Pair;

import javax.lang.model.util.Elements;
import java.util.List;

public class ConversionServiceAdapterDescriptor {
  private ClassName adapterClassName;
  private String conversionServiceBeanName;
  private List<Pair<TypeName, TypeName>> fromToMappings;
  private boolean lazyAnnotatedConversionServiceBean;

  public Elements getElementUtils() {
    return elementUtils;
  }

  public ConversionServiceAdapterDescriptor elementUtils(Elements elementUtils) {
    this.elementUtils = elementUtils;
    return this;
  }

  private Elements elementUtils;
  public boolean isSourceVersionAtLeast9() {
    return sourceVersionAtLeast9;
  }

  public ConversionServiceAdapterDescriptor sourceVersionAtLeast9(boolean sourceVersionAtLeast9) {
    this.sourceVersionAtLeast9 = sourceVersionAtLeast9;
    return this;
  }

  private boolean sourceVersionAtLeast9;

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
