package org.mapstruct.extensions.spring.converter;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.mapstruct.extensions.spring.SpringMapperConfig.DEFAULT_CONFIGURATION_CLASS_NAME;
import static org.mapstruct.extensions.spring.SpringMapperConfig.DEFAULT_CONVERSION_SERVICE_BEAN_NAME;

import com.squareup.javapoet.ClassName;
import java.util.List;

public class ConversionServiceAdapterDescriptor {
  public static final String DEFAULT_CONVERTER_SCAN_CLASS_NAME = "ConverterScan";
  public static final String DEFAULT_CONVERTER_SCANS_CLASS_NAME = "ConverterScans";

  private ClassName adapterClassName;
  private String conversionServiceBeanName = DEFAULT_CONVERSION_SERVICE_BEAN_NAME;
  private List<FromToMapping> fromToMappings;
  private boolean lazyAnnotatedConversionServiceBean;
  private String configurationClassName = DEFAULT_CONFIGURATION_CLASS_NAME;

  private boolean generateConverterScan;

  boolean hasNonDefaultConversionServiceBeanName() {
    return isNotEmpty(getConversionServiceBeanName())
            && !DEFAULT_CONVERSION_SERVICE_BEAN_NAME.equals(getConversionServiceBeanName());
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

  public ConversionServiceAdapterDescriptor configurationClassName(
          final String configurationClassName) {
    this.configurationClassName = configurationClassName;
    return this;
  }

  public String getConfigurationClassName() {
    return configurationClassName;
  }

  public boolean isGenerateConverterScan() {
    return generateConverterScan;
  }

  public ConversionServiceAdapterDescriptor generateConverterScan(final boolean generateConverterScan) {
    this.generateConverterScan = generateConverterScan;
    return this;
  }

  public ClassName getConverterScanClassName() {
    return ClassName.get(getAdapterClassName().packageName(), DEFAULT_CONVERTER_SCAN_CLASS_NAME);
  }

  public ClassName getConverterScansClassName() {
    return ClassName.get(getAdapterClassName().packageName(), DEFAULT_CONVERTER_SCANS_CLASS_NAME);
  }

  public ClassName getConverterRegistrationConfigurationClassName() {
    return ClassName.get(getAdapterClassName().packageName(), configurationClassName);
  }
}
