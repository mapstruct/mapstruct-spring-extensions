package org.mapstruct.extensions.spring.converter;


class ConverterScansGeneratorTest extends AbstractConversionServiceBeanNameIndependentGeneratorTest {
  ConverterScansGeneratorTest() {
    super(
        "ConverterScansJava8Generated.java",
        "ConverterScansJava8GeneratedNoDate.java",
        "ConverterScansJava9PlusGenerated.java",
        "ConverterScansJava9PlusGeneratedNoDate.java",
        "ConverterScansNoGenerated.java",
        new ConverterScansGenerator(FIXED_CLOCK));
  }
}
