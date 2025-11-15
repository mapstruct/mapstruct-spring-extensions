package org.mapstruct.extensions.spring.converter;


class ConverterScansGeneratorTest extends AbstractConversionServiceBeanNameIndependentGeneratorTest {
  ConverterScansGeneratorTest() {
    super(
        "ConverterScansJava9PlusGenerated.java",
        "ConverterScansJava9PlusGeneratedNoDate.java",
        "ConverterScansNoGenerated.java",
        new ConverterScansGenerator(FIXED_CLOCK));
  }
}
