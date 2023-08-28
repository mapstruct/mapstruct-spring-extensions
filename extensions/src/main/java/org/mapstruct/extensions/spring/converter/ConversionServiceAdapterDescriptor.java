package org.mapstruct.extensions.spring.converter;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import com.squareup.javapoet.ClassName;
import java.util.List;

public class ConversionServiceAdapterDescriptor {
  private ClassName adapterClassName;
  private String conversionServiceBeanName;
  private List<FromToMapping> fromToMappings;
  private boolean lazyAnnotatedConversionServiceBean;

  private boolean generateConverterScan;

  boolean hasNonDefaultConversionServiceBeanName() {
    return isNotEmpty(getConversionServiceBeanName());
  }

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

  public List<FromToMapping> getFromToMappings() {
    return fromToMappings;
  }

  public ConversionServiceAdapterDescriptor fromToMappings(
      final List<FromToMapping> fromToMappings) {
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


  public boolean isGenerateConverterScan() {
    return generateConverterScan;
  }

  public ConversionServiceAdapterDescriptor generateConverterScan(final boolean generateConverterScan) {
    this.generateConverterScan = generateConverterScan;
    return this;
  }

  public ClassName getConverterScanClassName() {
    return ClassName.get(getAdapterClassName().packageName(), "ConverterScan");
  }

  public ClassName getConverterScansClassName() {
    return ClassName.get(getAdapterClassName().packageName(), "ConverterScans");
  }

  public ClassName getConverterRegistrationConfigurationClassName() {
    return ClassName.get(getAdapterClassName().packageName(), "ConverterRegistrationConfiguration");
  }
}
