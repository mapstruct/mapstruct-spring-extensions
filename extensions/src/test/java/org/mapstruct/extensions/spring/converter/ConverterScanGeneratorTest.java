package org.mapstruct.extensions.spring.converter;


class ConverterScanGeneratorTest extends AbstractConversionServiceBeanNameIndependentGeneratorTest {
  ConverterScanGeneratorTest() {
    super(
        "ConverterScanJava9PlusGenerated.java",
        "ConverterScanJava9PlusGeneratedNoDate.java",
        "ConverterScanNoGenerated.java",
        new ConverterScanGenerator(FIXED_CLOCK));
  }
}
